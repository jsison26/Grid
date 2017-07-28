package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controller.ClusterManagerController;
import controller.Utilities;


public class ClusterManagerFrame  extends JFrame{
	private static final long serialVersionUID = -4581428471679187057L;
	private LogPanel logPanel;
	private JTabbedPane tabbedPane;
	
	
	private static final Logger logger =  LoggerFactory.getLogger(ClusterManagerFrame.class);
	private static Properties prop = new Properties();
	private static InputStream input = null;
	private Path logFileDirectory;
	
	private static class ClusterManagerFrameHelper {
		private static final ClusterManagerFrame Instance = new ClusterManagerFrame();
	}
	
	public static ClusterManagerFrame getInstance() {
		return ClusterManagerFrameHelper.Instance;
	}
	
	private ClusterManagerFrame() {
		super("Cluster Manager");
		
		try{
			input = new FileInputStream(Utilities.getAppConfigArg(new String[]{}));
			prop.load(input);
		}
		catch(FileNotFoundException e){
			logger.info("File not found: {}", Utilities.getStackTrace(e));
		}
		catch(Exception e){
			logger.info("Could not load UIManager: {}", Utilities.getStackTrace(e));
		}
		
		logFileDirectory = Paths.get(prop.getProperty("logsDirectory"));
		
		initMenuBar();
		
		tabbedPane = new JTabbedPane();
		logPanel = new LogPanel();		
		
		//reloadDB();
		
		
		tabbedPane.add("Log", logPanel);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent w){
				ClusterManagerController.getInstance().stop();
				dispose();
				System.gc();
			}
		});
		
		this.setMinimumSize(new Dimension(500, 400));
		this.setSize(800, 600);
		this.setLayout(new BorderLayout());
		this.add(tabbedPane, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setVisible(true);
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
		JMenuItem deleteJobsDataItem = new JMenuItem("Delete Jobs");
		deleteJobsDataItem.setMnemonic(KeyEvent.VK_J);
		databaseMenu.add(deleteJobsDataItem);
		
		menuBar.add(databaseMenu);
				
		// Window Menu
		
		JMenu windowMenu = new JMenu("Window");
		windowMenu.setMnemonic(KeyEvent.VK_W);
		JMenuItem refreshDataItem = new JMenuItem("Refresh");
		refreshDataItem.setMnemonic(KeyEvent.VK_R);
		refreshDataItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		windowMenu.add(refreshDataItem);
		
		menuBar.add(windowMenu);
		
		
		
		// Event handlers
		
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
			public void actionPerformed(ActionEvent arg0) {
				
			}		
		});
		
		refreshDataItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					reloadDB();
				} catch (SQLException e1) {
					logger.error("Could not reload database records.");
					logger.error(Utilities.getStackTrace(e1));
				}
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
	
	public void reloadDB() throws SQLException {
		String clusterName = ClusterManagerController.getInstance().getCluster().getClusterName();
		ClusterManagerController.getInstance().reloadCluster(clusterName);
		ClusterManagerController.getInstance().reloadJobsByClusterName(clusterName);
	}

}
