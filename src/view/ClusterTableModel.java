package view;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import model.Cluster;

public class ClusterTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -2717180348699883974L;
	private List<Cluster> db;
	private String[] colNames = {"Cluster ID", "Cluster Name", "Status"};
	
	public void setData(List<Cluster> db){
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
		Cluster cluster = db.get(row);
		
		switch(col){
		case 0:
			return cluster.getClusterId();
		case 1:
			return cluster.getClusterName();
		case 2:
			return cluster.getStatus();
		default:
			return null;
		}
		
	}
	
	@Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
	        case 0:
	            return Integer.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            default:
                return String.class;
        }
    }
	
}
