package view;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

import controller.MasterManagerController;
import model.Cluster;
import model.Job;
import model.JobStatus;
import model.JobType;



public class NewJobPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -4114126682220809556L;
	private JPanel formPanel;
	private JPanel infoPanel;
	private JComboBox<String> comboBox;
	private JTextField jobNameField;
	private JTextField commandLineField;
	private JTextField commandArgumentsField;
	private JTextField workingDirectoryField;
	private JComboBox<String> clusterBox;
	private JLabel jobTypeLabel;
	private JLabel jobNameLabel;
	private JLabel commandLineLabel;
	private JLabel commandArgumentsLabel;
	private JLabel workingDirectoryLabel;
	private JLabel clusterLabel;
	private JButton addButton;
	private JobListener jobListener;
	private List<JobType> jobTypes;
	private List<Cluster> clusters;
	private JTable jobTypeTable;
	private JobTypeTableModel jobTypeTableModel;
	private static final Insets WEST_INSETS = new Insets(5, 0, 5, 5);
	private static final Insets EAST_INSETS = new Insets(5, 5, 5, 0);
	   
	
	public NewJobPanel() {
		int y = 0; // y_axis position of a component
		
		infoPanel = new JPanel();
		
		formPanel = new JPanel(new GridBagLayout());
		
	    GridBagConstraints gbc;
	    
	    // form panel starts
	    
	    gbc = createGbc(0, y);
	    jobNameLabel = new JLabel("Job Name");
	    formPanel.add(jobNameLabel, gbc);
	    
	    gbc = createGbc(1, y);
	    jobNameField = new JTextField();
	    formPanel.add(jobNameField, gbc);
	    
	    y++; // new row

	    gbc = createGbc(0, y);
	    jobTypeLabel = new JLabel("Job Type");
	    formPanel.add(jobTypeLabel, gbc);
	    
	    gbc = createGbc(1, y);
	    comboBox = new JComboBox<String>();
	    formPanel.add(comboBox, gbc);
	    
	    y++; // new row
	    
	    gbc = createGbc(0, y);
	    commandLineLabel = new JLabel("Command Line");
	    formPanel.add(commandLineLabel, gbc);
	    
	    gbc = createGbc(1, y);
	    commandLineField = new JTextField();
	    formPanel.add(commandLineField, gbc);
	    
	    y++; // new row
	    
	    gbc = createGbc(0, y);
	    commandArgumentsLabel = new JLabel("Command Argument");
	    formPanel.add(commandArgumentsLabel, gbc);
	    
	    gbc = createGbc(1, y);
	    commandArgumentsField = new JTextField();
	    formPanel.add(commandArgumentsField, gbc);
	    
	    y++; // new row
	    
	    gbc = createGbc(0, y);
	    workingDirectoryLabel = new JLabel("Working Directory");
	    formPanel.add(workingDirectoryLabel, gbc);
	    
	    gbc = createGbc(1, y);
	    workingDirectoryField = new JTextField();
	    formPanel.add(workingDirectoryField, gbc);
	    
	    y++; // new row
	    
	    gbc = createGbc(0, y);
	    clusterLabel = new JLabel("Cluster");
	    formPanel.add(clusterLabel, gbc);
	    
	    gbc = createGbc(1, y);
	    clusterBox = new JComboBox<String>();
	    formPanel.add(clusterBox, gbc);
	    
	    y++; // new row
	    
	    gbc = createGbc(1, y);
	    gbc.weighty = 10.0;
	    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
	    
	    addButton = new JButton("Add");
		addButton.addActionListener(this);
		
		formPanel.add(addButton, gbc);
		// form panel ends
		
		// info panel starts
		
		jobTypeTableModel = new JobTypeTableModel();
		jobTypeTable = new JTable(jobTypeTableModel);
		TableColumn removeColumn0 = jobTypeTable.getColumnModel().getColumn(0);
		jobTypeTable.removeColumn(removeColumn0);
		
		// info panel ends
		
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
	    formPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("New Job Form"),
				BorderFactory.createEmptyBorder(5,5,5,5)));
	    add(formPanel);
	    
	    add(new JScrollPane(jobTypeTable));
	    
		add(infoPanel);
	}
	
	private GridBagConstraints createGbc(int x, int y) {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;

      gbc.anchor = (x == 0) ? GridBagConstraints.EAST : GridBagConstraints.WEST;
      if(x == 1)
    	  gbc.fill = GridBagConstraints.HORIZONTAL; 

      gbc.insets = (x == 0) ? WEST_INSETS : EAST_INSETS;
      gbc.weightx = (x == 0) ? 0.1 : 1.0;
      gbc.weighty = 1.0;
      return gbc;
    }
	
	public void setJobTypeData(List<JobType> db){
		jobTypeTableModel.setData(db);
		jobTypes = db;
		comboBox.removeAllItems();
		for(JobType jt: jobTypes){
			comboBox.addItem(jt.getJobTypeName());
		}
		jobTypeTable.setAutoCreateRowSorter(true);
		System.out.println("setJobTypeData");
	}
	
	public void setClusterData(List<Cluster> db){
		clusters = db;
		clusterBox.removeAllItems();
		for(Cluster c: clusters){
			clusterBox.addItem(c.getClusterName());
		}
	}
	
	public void setJobListener(JobListener jobListener){
		this.jobListener = jobListener;
	}
	
	public void refresh(){
		setJobTypeData(MasterManagerController.getInstance().getJobTypes());
		setClusterData(MasterManagerController.getInstance().getClusters());
		jobTypeTableModel.fireTableDataChanged();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton clicked = (JButton) e.getSource();
		
		if(clicked == addButton){
			int jobTypeId = -1;
			String jobTypeName = "";
			int clusterId = -1;
			String clusterName = "";
			
			for(JobType jt: jobTypes){
				if(jt.getJobTypeName().equals(comboBox.getSelectedItem().toString())){
					jobTypeId = jt.getJobTypeId();
					jobTypeName = jt.getJobTypeName();
					
					break;
				}	
			}
			
			for(Cluster c: clusters){
				if(c.getClusterName().equals(clusterBox.getSelectedItem().toString())){
					clusterId = c.getClusterId();
					clusterName = c.getClusterName();					
					break;
				}	
			}
		
			if(jobListener != null){			
				Job job = new Job();
				job.setJobName(jobNameField.getText());
				job.setJobTypeId(jobTypeId);
				job.setJobTypeName(jobTypeName);
				job.setStatus(JobStatus.QUEUED);
				job.setCommandLine(commandLineField.getText());
				job.setCommandArguments(commandArgumentsField.getText());
				job.setWorkingDirectory(workingDirectoryField.getText());
				job.setClusterId(clusterId);
				job.setClusterName(clusterName);

				jobListener.jobEventOccurred(job);
			}
		}
	}

}