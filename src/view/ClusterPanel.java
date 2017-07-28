package view;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import model.Cluster;


public class ClusterPanel extends JPanel {
	private static final long serialVersionUID = -2047663308036100228L;
	private JTable clusterTable;
	private ClusterTableModel clusterTableModel;
	
	public ClusterPanel(){
		clusterTableModel = new ClusterTableModel();
		clusterTable = new JTable(clusterTableModel);
		
		clusterTable.removeColumn(clusterTable.getColumnModel().getColumn(0));
		
		setLayout(new BorderLayout());
		add(new JScrollPane(clusterTable), BorderLayout.CENTER);
	}
	
	public void setClusterData(List<Cluster> db){
		clusterTableModel.setData(db);
	}
	
	public void refresh(){
		clusterTableModel.fireTableDataChanged();
	}
}
