package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import model.Job;


public class JobPanel extends JPanel {
	private static final long serialVersionUID = 8546701191693600202L;
	private JTable jobTable;
	private JobTableModel jobTableModel;
	private Job selectedJob = null;
	private int selectedJobAnchorIndex = 0;
	private int selectedJobLeadIndex = 0;
	private JPopupMenu popupJob;
	private JobTableListener jobTableListener;
	
	public JobPanel() {
		jobTableModel = new JobTableModel();
		jobTable = new JTable(jobTableModel);
		popupJob = new JPopupMenu();
		
		JMenuItem removeJob = new JMenuItem("Cancel");
		removeJob.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent a){
				int row = jobTable.getSelectedRow();
				
				if(jobTableListener != null && selectedJob != null) {
					jobTableListener.cancelJob(selectedJob);
					jobTableModel.fireTableRowsDeleted(row, row);
				}
			}
		});
		
		popupJob.add(removeJob);
		
		jobTable.removeColumn(jobTable.getColumn("Job ID"));
		jobTable.removeColumn(jobTable.getColumn("Job Type ID"));
		jobTable.removeColumn(jobTable.getColumn("Cluster ID"));
		
		jobTable.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				int row = jobTable.rowAtPoint(e.getPoint());
				
				selectJob(row);
				
				if(e.getButton() == MouseEvent.BUTTON1){
					selectedJobAnchorIndex = jobTable.getSelectionModel().getAnchorSelectionIndex();
					selectedJobLeadIndex = jobTable.getSelectionModel().getLeadSelectionIndex();
				}
				else if(e.getButton() == MouseEvent.BUTTON3){
					popupJob.show(jobTable, e.getX(), e.getY());
				}
			}
		});
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JScrollPane(jobTable));
	}
	
	public void setJobData(List<Job> db){
		jobTableModel.setData(db);
		jobTable.setAutoCreateRowSorter(true);
	}
	
	public Job getSelectedJob(){
		return selectedJob;
	}
	
	public void selectFirstJob() {
		restoreSelectedRows(jobTable,0,0);
	}
	
	private String getCellValue(JTable table, int row, String columnName){
		for(int col = 0; col < table.getColumnCount(); col++){
			if(table.getColumnName(col).equals(columnName))
				return table.getValueAt(row, col).toString();
		}
		return null;
	}
	
	private void selectJob(int row) {
		jobTable.getSelectionModel().setSelectionInterval(row, row);		
		selectedJob = new Job();
		selectedJob.setJobName(getCellValue(jobTable, row, "Job Name"));
		selectedJob.setJobTypeName(getCellValue(jobTable, row, "Job Type"));
		selectedJob.setStatus(getCellValue(jobTable, row, "Status"));
		selectedJob.setClusterName(getCellValue(jobTable, row, "Cluster Name"));
		selectedJob.setCommandLine(getCellValue(jobTable, row, "Command Line"));
		selectedJob.setCommandArguments(getCellValue(jobTable, row, "Command Arguments"));
		selectedJob.setWorkingDirectory(getCellValue(jobTable, row, "Working Directory"));
	}
	
	public void restoreSelectedRows(JTable jtable, int anchorIndex, int leadIndex) {
		if(jtable == null)
			return;
		
		if(jtable.getRowCount() == 0)
			return;
		
		if(anchorIndex > jtable.getRowCount() || leadIndex > jtable.getRowCount()) {
			jtable.setRowSelectionInterval(1, 1);
		}
		else {
			jtable.setRowSelectionInterval(anchorIndex, leadIndex);
		}
	}
	
	public void refreshJobs(){
		jobTableModel.fireTableDataChanged();
		restoreSelectedRows(jobTable, selectedJobAnchorIndex, selectedJobLeadIndex);
	}
	
	public void setJobTableListener(JobTableListener listener) {
		this.jobTableListener = listener;
	}

}
