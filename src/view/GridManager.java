package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controller.GridManagerController;
import controller.Utilities;
import model.Cluster;
import model.Job;
import model.JobStatus;
import model.JobType;


public class GridManager extends JFrame {
	private static final long serialVersionUID = -8973534341923708528L;
	private ClusterPanel clusterPanel;
	private JobPanel jobPanel;
	private LogPanel logPanel;
	private NewJobPanel newJobPanel;
	private JTabbedPane tabbedPane;
	
	private Properties prop;
	private InputStream input;
	private Path logFileDirectory;
	private static final Logger logger = LoggerFactory.getLogger(GridManager.class);
	
	private static class GridManagerHelper {
		private static final GridManager Instance = new GridManager("Master Manager");
	}
	
	public static GridManager getInstance(){
		return GridManagerHelper.Instance;
	}
	
	private GridManager (String title) {
		super(title);
		
		try {
			prop = new Properties();
			input = new FileInputStream("app.properties");
			prop.load(input);
		} catch (Exception e) {
			logger.error("{}", e.getStackTrace().toString());
		}
		logFileDirectory =  Paths.get(prop.getProperty("logsDirectory").trim());
		
		initClusterPanel();
		initJobPanel();
		initLogPanel();
		initNewJobPanel();
		initMenuBar();
		

		tabbedPane = new JTabbedPane();
		tabbedPane.add("Clusters", clusterPanel);
		tabbedPane.add("Jobs", jobPanel);
		tabbedPane.add("Log", logPanel);
		tabbedPane.add("New Job", newJobPanel);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent w){
				GridManagerController.getInstance().stop();
				dispose();
				System.gc();
			}
		});

		this.setMinimumSize(new Dimension(500, 400));
		this.setSize(800, 600);
		this.setLayout(new BorderLayout());
		this.add(tabbedPane, BorderLayout.CENTER);
		this.setVisible(true);

		onExit();
	}
	
	private void onExit() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void initClusterPanel() {
		clusterPanel = new ClusterPanel();
		setClusterData(GridManagerController.getInstance().getClusters());
	}
	
	private void initJobPanel() {
		jobPanel = new JobPanel();
		
		List<Job> queuedJobs = GridManagerController.getInstance().getQueuedJobs();
		setJobData(queuedJobs);
		
		jobPanel.setJobTableListener(new JobTableListener(){
			@Override
			public void cancelJob(Job job) {
				if(job.getStatus().equals(JobStatus.COMPLETED) || job.getStatus().equals(JobStatus.ERROR))
					return;
				try {
					sendJobCancel(job);
				}
				catch (SQLException e) {
					logger.error("Could not cancel job {}.", job.getJobName());
					Utilities.getStackTrace(e);
				}
			}
		});
		
		jobPanel.selectFirstJob();
	}
	
	private void initLogPanel() {
		logPanel = new LogPanel();
		
	}
	
	private void initNewJobPanel() {
		newJobPanel = new NewJobPanel();
		setJobTypeData(GridManagerController.getInstance().getJobTypes());
		
		newJobPanel.setJobListener(new JobListener(){
			public void jobEventOccurred(Job job) {
				try {
					GridManagerController.getInstance().addJob(job);
					logger.info("Job added: [{}]", job.getJobName());
				}
				catch (SQLException e) {
					logger.error("Failed to create job {}", job.getJobName());
					Utilities.getStackTrace(e);
				}
			}
		});
	}
	
	private void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		setJMenuBar(menuBar);
		
		// File Menu
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenu logsMenu = new JMenu("Logs");
		JMenuItem deleteOldLogsDataItem = new JMenuItem("Delete Old");
		logsMenu.add(deleteOldLogsDataItem);
		fileMenu.add(logsMenu);
		
		JMenuItem deleteAllLogsDataItem = new JMenuItem("Delete All");
		logsMenu.add(deleteAllLogsDataItem);
		fileMenu.add(logsMenu);
		
		
		fileMenu.addSeparator();
		
		JMenuItem exitDataItem = new JMenuItem("Exit");
		fileMenu.add(exitDataItem);
		
		menuBar.add(fileMenu);
		
		// Database Menu
		
		JMenu databaseMenu = new JMenu("Database");
		databaseMenu.setMnemonic(KeyEvent.VK_D);
		
		JMenuItem deleteJobsDataItem = new JMenuItem("Clear Jobs");
		deleteJobsDataItem.setMnemonic(KeyEvent.VK_J);
		databaseMenu.add(deleteJobsDataItem);
	
		menuBar.add(databaseMenu);
				
		// Windows Menu
		
		JMenu windowMenu = new JMenu("Window");
		windowMenu.setMnemonic(KeyEvent.VK_W);
		
		JMenuItem refreshDataItem = new JMenuItem("Refresh");
		refreshDataItem.setMnemonic(KeyEvent.VK_R);
		refreshDataItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		windowMenu.add(refreshDataItem);
		
		menuBar.add(windowMenu);
		
		
		/*** event handlers ***/
		
		deleteOldLogsDataItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Utilities.deleteFiles(logFileDirectory, ".+[.]log_.+");
			}		
		});
		
		deleteAllLogsDataItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Utilities.deleteFiles(logFileDirectory, ".+[.].+");
			}		
		});
			
		deleteJobsDataItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					truncateJobs();
				}
				catch (SQLException e1) {
					logger.error("Could not truncate jobs.");
					Utilities.getStackTrace(e1);
				}
				
				try {
					reloadJobs();
				} catch (SQLException e2) {
					logger.error("Could not reload job records from the database after delete.");
				}
				List<Job> queuedJobs = GridManagerController.getInstance().getQueuedJobs();
				setJobData(queuedJobs);
				refreshJobData();
				
				try {
					reloadJobTypes();
				} catch (SQLException e1) {
					logger.error("Could not reload job type records from the database after delete.");
				}
				setJobTypeData(GridManagerController.getInstance().getJobTypes());
				refreshJobTypeData();
				
				logger.info("All data reloaded.");
			}
			
		});
				
		refreshDataItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					reloadClusters();
				} catch (SQLException e3) {
					logger.error("Could not reload cluster records from the database.");
					logger.error(Utilities.getStackTrace(e3));
				}
				setClusterData(GridManagerController.getInstance().getClusters());
				refreshClusterData();
				
				try {
					reloadJobs();
				} catch (SQLException e2) {
					logger.error("Could not reload job records from the database.");
					logger.error(Utilities.getStackTrace(e2));
				}
				List<Job> queuedJobs = GridManagerController.getInstance().getQueuedJobs();
				setJobData(queuedJobs);
				refreshJobData();
				
				try {
					reloadJobTypes();
				} catch (SQLException e1) {
					logger.error("Could not reload job type records from the database.");
					logger.error(Utilities.getStackTrace(e1));
				}
				setJobTypeData(GridManagerController.getInstance().getJobTypes());
				refreshJobTypeData();
				
				logger.info("All data reloaded.");
			}
		});
		
		exitDataItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}
	
	private void sendJobCancel(Job job) throws SQLException {
		GridManagerController.getInstance().sendJobCancel(job);
	}
	
	public void reloadClusters() throws SQLException {
		GridManagerController.getInstance().reloadClusters();
	}
	
	public void setClusterData(List<Cluster> clusterDB) {
		clusterPanel.setClusterData(clusterDB);
	}
	
	public void refreshClusterData() {
		clusterPanel.refresh();
	}
	
	public void reloadJobs() throws SQLException {
		GridManagerController.getInstance().reloadJobsAll();
	}
		
	public void setJobData(List<Job> jobDB) {
		jobPanel.setJobData(jobDB);
	}
	
	public void refreshJobData() {
		jobPanel.refreshJobs();
	}
	
	public void reloadJobTypes() throws SQLException {
		GridManagerController.getInstance().reloadJobTypesAll();
	}
	
	public void setJobTypeData(List<JobType> jobTypeDB) {
		newJobPanel.setJobTypeData(jobTypeDB);
	}
	
	public void refreshJobTypeData() {
		newJobPanel.refresh();
	}
	
	public void truncateJobs() throws SQLException {
		GridManagerController.getInstance().truncateJobs();
	}
	
}
