package controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Cluster;
import model.ClusterDB;
import model.ClusterStatus;
import model.DBAccess;
import model.Job;
import model.JobDB;
import model.TopicMessage;
import model.TopicMessageType;
import view.ClusterManagerFrame;



public class ClusterManagerController {
	private String clusterName;
	private Cluster cluster; 
	private ClusterDB clusterDB;
	private JobDB jobDB;
	private Properties prop = new Properties();
	private InputStream input = null;
	private static final Logger logger =  LoggerFactory.getLogger(ClusterManagerController.class);
	
	private TopicSubscriber consumer;
	private TopicPublisher producer;
	private AdvisorySubscriber advisoryConsumer;
	private SwingWorker<Void, Void> topicWorker;
	
	private static class ClusterManagerControllerHelper {
		private static final ClusterManagerController Instance = new ClusterManagerController();
	}
	
	public static ClusterManagerController getInstance() {
		return ClusterManagerControllerHelper.Instance;
	}
	
	private ClusterManagerController() {
		//load app configs
		try{
			input = new FileInputStream("app.properties");
			prop.load(input);
		}
		catch(Exception e){
			logger.error("Failed to load config file: app.properties");
			logger.error(Utilities.getStackTrace(e));
		}

		clusterName = prop.getProperty("clusterName").trim();
		
		// DBs
		this.clusterDB = new ClusterDB();
		try {
			reloadCluster(clusterName);
		} catch (SQLException e1) {
			logger.error("Could not load clusters list from the database.");
			logger.error(Utilities.getStackTrace(e1));
		}
		
		this.cluster = clusterDB.getClusterByClusterName(clusterName);
		if(this.cluster == null) {
			try {
				this.cluster = new Cluster();
				this.cluster.setClusterName(clusterName);
				this.cluster.setStatus(ClusterStatus.UNAVAILABLE);
				clusterDB.addCluster(this.cluster);
			}
			catch (SQLException e) {
				logger.error("This cluster {} is not enabled. Please manually add this cluster to the database. ", clusterName);
				logger.error(Utilities.getStackTrace(e));
			}
		}
		
		this.jobDB = new JobDB();
		try {
			reloadJobsByClusterName(this.cluster.getClusterName());
		} catch (SQLException e) {
			logger.error("Could not load cluster jobs from the database.");
			logger.error(Utilities.getStackTrace(e));
		}
	}

	public void start() {
		startClusterFrame();
		startTopic();
	}
	
	public void stop() {
		try {
			if(advisoryConsumer != null)
				advisoryConsumer.dispose();
				
			if(consumer != null)
				consumer.dispose();
			
			if(producer != null)
				producer.dispose();
			
			if(DBAccess.getInstance() != null)
				DBAccess.getInstance().disconnect();
		} catch (Exception e) {
			logger.error("Failed to stop services gracefully.");
			logger.error(Utilities.getStackTrace(e));
		}
	}
	
	private void startClusterFrame() {
		ClusterManagerFrame.getInstance();
	}
	
	
	public void startTopic() {
		topicWorker = new SwingWorker<Void, Void>(){
			@Override
			protected Void doInBackground() throws Exception {				
				try {				
					producer = new TopicPublisher("MASTER");
					
					advisoryConsumer = new AdvisorySubscriber(producer.getDestination());
		        	advisoryConsumer.setListener(new ClusterAdvisoryListener(producer.getDestination()));
					advisoryConsumer.start();
					logger.info("Advisory support listener for MASTER started.");

					consumer = new TopicSubscriber(clusterName, clusterName);
					//consumer = new QueueConsumer(clusterName, clusterName);
					consumer.setMessageListener(new ClusterTopicListener());
					consumer.start();
					logger.info("Topic subscriber for {} started.", clusterName);
					
		        } catch (Exception e) {
		            logger.error("TopicSubscriber failed to subscribed.");
		            logger.error(Utilities.getStackTrace(e));
		        }

		        return null;
			}
			
		};
		
		topicWorker.execute();
	}
	
	public void reloadCluster(String clusterName) throws SQLException {
		clusterDB.reloadCluster(clusterName);
	}
	
	public Cluster getCluster(){
		return cluster;
	}
	
	public void updateCluster(Cluster cluster) throws SQLException {
		synchronized(clusterDB){
			clusterDB.updateCluster(cluster);
			sendClusterUpdate(cluster);
		}
	}
	
	public void sendClusterUpdate(Cluster cluster) {
		if(producer != null) {
			TopicMessage topic = new TopicMessage();
			topic.setMessageType(TopicMessageType.UPDATE_CLUSTER);
			topic.setMachine(clusterName);
			topic.setTargetMachine("MASTER");
			topic.setObject(cluster);
			
			try {
				producer.sendTopic(topic);
			} catch (Exception e) {
				logger.error("Failed to send cluster update to MASTER");
				logger.error(Utilities.getStackTrace(e));
			}
		}
	}
	
	public void reloadJobsByClusterName(String clusterName) throws SQLException {
		jobDB.reloadJobsByClusterName(clusterName);
	}
	
	public List<Job> getJobs() {
		return jobDB.getJobs();
	}
	
	public Job getJobByJobName(String jobName) {
		return jobDB.getJobByJobName(jobName);
	}
	
	public void updateJob(Job job) throws SQLException {
		synchronized(jobDB){
			if(getJobByJobName(job.getJobName()) == null)
				jobDB.insertJob(job);
			else
				jobDB.updateJob(job);
		}
	}

	public List<Job> getQueuedJob(String clusterName2) {
		return jobDB.getQueuedJobs();
	}
	
}
