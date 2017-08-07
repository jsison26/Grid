package view;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import model.Job;


public class JobTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 8262103782426454728L;
	private List<Job> db;
	private String[] colNames = {"Job ID", "Job Name", "Job Type ID", "Job Type", "Status"
			, "Cluster ID", "Cluster Name", "Command Line", "Command Arguments", "Working Directory"};
	
	public JobTableModel(){
		
	}
	
	public void setData(List<Job> db){
		this.db = db;
	}
	
	@Override
	public String getColumnName(int column) {
		return colNames[column];
	}
	
	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	@Override
	public int getRowCount() {
		return db.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Job job = db.get(row);
		
		switch(col){
		case 0:
			return job.getJobId();
		case 1:
			return job.getJobName();
		case 2:
			return job.getJobTypeId();
		case 3:
			return job.getJobTypeName();
		case 4:
			return job.getStatus();
		case 5:
			return job.getClusterId();
		case 6:
			return job.getClusterName();
		case 7:
			return job.getCommandLine();
		case 8:
			return job.getCommandArguments();
		case 9:
			return job.getWorkingDirectory();
		default:
			return null;
		}
		
	}
	
	@Override
	public Class<?> getColumnClass(int column) {
		switch(column){
		case 0:
			return Integer.class;
		case 1:
			return String.class;
		case 2:
			return Integer.class;
		case 3:
			return String.class;
		case 4:
			return String.class;
		case 5:
			return Integer.class;
		case 6:
			return String.class;
		case 7:
			return String.class;
		case 8:
			return String.class;
		case 9:
			return String.class;
		default:
			return null;
		}
		
	}

}
