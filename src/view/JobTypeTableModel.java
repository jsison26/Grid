package view;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import model.JobType;


public class JobTypeTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -6258029141067935187L;
	private List<JobType> db;
	private String[] colNames = {"Job Type ID", "Job Type"};
	
	public JobTypeTableModel(){
		
	}
	
	public void setData(List<JobType> db){
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
		JobType jobType = db.get(row);
		
		switch(col){
		case 0:
			return jobType.getJobTypeId();
		case 1:
			return jobType.getJobTypeName();
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
		default:
			return null;
		}
		
	}
}
