package controller;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Cluster;
import model.ClusterDB;
import model.ClusterStatus;
import model.DBAccess;
import model.Job;
import model.JobDB;
import model.JobStatus;
import model.JobType;
import model.JobTypeDB;
import model.TopicMessage;
import model.TopicMessageType;
import view.MasterManagerFrame;

public class MasterManagerController {
	private ClusterDB clusterDB;
	private JobDB jobDB;
	private JobTypeDB jobTypeDB;
	private static final Logger logger =  LoggerFactory.getLogger(MasterManagerController.class);
	
	private TopicSubscriber consumer;
	private AdvisorySubscriber advisoryConsumer;
	private Map<String, TopicPublisher> clusterRequestQueues;
	private Map<String, AdvisorySubscriber> clusterAdvisoryQueues;
	private SwingWorker<Void, Void> topicWorker;
	
	
	private static class GridManagerControllerHelper {
		private static final MasterManagerController Instance = new MasterManagerController();
	}
	
	public static MasterManagerController getInstance() {
		return GridManagerControllerHelper.Instance;
	}
	
	private MasterManagerController() {
		clusterDB = new ClusterDB();
		try {
			reloadClusters();
		} catch (SQLException e3) {
			logger.error("Could not load cluster records from the database");
			logger.error(Utilities.getStackTrace(e3));
		}
		
		for (Cluster c: clusterDB.getClusters()) {
			c.setStatus(ClusterStatus.UNAVAILABLE);
		}
		
		jobDB = new JobDB();
		try {
			reloadJobsAll();
		} catch (SQLException e2) {
			logger.error("Could not load job records from the database");
			logger.error(Utilities.getStackTrace(e2));
		}
		
		jobTypeDB = new JobTypeDB();
		try {
			reloadJobTypesAll();
		} catch (SQLException e1) {
			logger.error("Could not load job type records from the database");
			logger.error(Utilities.getStackTrace(e1));
		}
		
		clusterRequestQueues = new ConcurrentHashMap<String, TopicPublisher>();
		clusterAdvisoryQueues = new ConcurrentHashMap<String, AdvisorySubscriber>();
		
	}
	
	public void start() {
		startMasterManagerFrame();
		startTopic();
	}
	
	public void stop() {
		try {
			if(advisoryConsumer != null)
				advisoryConsumer.dispose();
			
			if(consumer != null)
				consumer.dispose();
			
			for(String key: clusterAdvisoryQueues.keySet()) {
				if(clusterAdvisoryQueues.containsKey(key)) {
					removeClusterAdvisoryQueue(key);
				}	
			}
			
			for(String key: clusterRequestQueues.keySet()) {
				if(clusterRequestQueues.containsKey(key)) {
					removeClusterRequestQueue(key);
				}	
			}
			
			if(topicWorker != null) {
				topicWorker.cancel(true);
			}
			
			if(DBAccess.getInstance() != null)
				DBAccess.getInstance().disconnect();
			
		} catch (Exception e) {
			logger.error("Stop error.");
			logger.error(Utilities.getStackTrace(e));
		}
	}
	
	private void startMasterManagerFrame() {
		MasterManagerFrame.getInstance();
	}

	private void startTopic() {
		topicWorker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				// Queue to update master of clusters' status
				consumer = new TopicSubscriber("MASTER", "MASTER");
				consumer.setMessageListener(new MasterTopicListener());
				consumer.start();
				logger.info("Topic consumer listener for MASTER started.");
				return null;
			}
		};
		topicWorker.execute();
	}
		
	public int addJob(Job job) throws SQLException {
		int jobId = -1;
		
		synchronized (jobDB) {
			jobId = jobDB.addJob(job);
			if (jobId > 0) {
				job.setJobId(jobId);
				MasterManagerFrame.getInstance().setJobData(jobDB.getJobs());
				MasterManagerFrame.getInstance().refreshJobData();
			}
		}
		
		return jobId;
	}
	
	public void reloadJobsAll() throws SQLException {
		synchronized (jobDB) {
			jobDB.reloadJobsAll();
		}
	}
	
	public List<Job> getJobs() {
		return jobDB.getJobs();
	}
	
	public List<Job> getQueuedJobs() {
		return jobDB.getQueuedJobs();
	}
	
	public Job getJobByJobName(String jobName) {
		return jobDB.getJobByJobName(jobName);
	}
	
	public void updateJob(Job job) throws SQLException {
		synchronized (jobDB) {
			jobDB.updateJob(job);

			List<Job> queuedJobs = getQueuedJobs();
			MasterManagerFrame.getInstance().setJobData(queuedJobs);
			MasterManagerFrame.getInstance().refreshJobData();
		}
	}
	
	public void sendJobUpdate(Job job, String clusterName) {
		if(clusterRequestQueues.containsKey(clusterName)) {
			TopicMessage topic = new TopicMessage();
			topic.setMessageType(TopicMessageType.UPDATE_JOB);
			topic.setMachine("MASTER");
			topic.setTargetMachine(clusterName);
			topic.setObject(job);
			
			try {
				clusterRequestQueues.get(clusterName).sendTopic(topic);
			} catch (Exception e) {
				logger.error("Failed to send job update to {} for job", clusterName, job.getJobName());
				Utilities.getStackTrace(e);
			}
		}
	}
	
	public void sendJobCancel(Job job) throws SQLException {
		job.setStatus(JobStatus.CANCELLED);
		updateJob(job);			
	}
	
	public void reloadJobTypesAll() throws SQLException {
		jobTypeDB.reloadJobTypesAll();
	}
	
	public List<JobType> getJobTypes() {
		return jobTypeDB.getJobTypes();
	}
	
	public void reloadClusters() throws SQLException {
		clusterDB.reloadClusters();
	}
	
	public void updateCluster(Cluster cluster) throws SQLException {
		synchronized (clusterDB) {
			Cluster c = clusterDB.getClusterByClusterName(cluster.getClusterName());
			if (c == null)
				clusterDB.insertCluster(cluster);
			else
				clusterDB.updateCluster(cluster);
			
			MasterManagerFrame.getInstance().setClusterData(clusterDB.getClusters());
			MasterManagerFrame.getInstance().refreshClusterData();
		}
	}
	
	public void sendClusterUpdate(Cluster cluster, String clusterName) {
		if(clusterRequestQueues.containsKey(clusterName)) {
			TopicMessage topic = new TopicMessage();
			topic.setMessageType(TopicMessageType.UPDATE_CLUSTER);
			topic.setMachine("MASTER");
			topic.setTargetMachine(clusterName);
			topic.setObject(cluster);
			
			try {
				clusterRequestQueues.get(clusterName).sendTopic(topic);
			} catch (Exception e) {
				logger.error("Failed to send cluster update to {}  for cluster {}", clusterName, cluster.getClusterName());
				Utilities.getStackTrace(e);
			}
		}
	}
	
	public List<Cluster> getClusters() {
		return clusterDB.getClusters();
	}
	
	public Cluster getClusterByClusterName(String clusterName) {
		return clusterDB.getClusterByClusterName(clusterName);
	}
	
	public List<Cluster> getAvailableClusters() {
		return clusterDB.getAvailableClusters();
	}
	
	
	public void addClusterRequestQueue(String topic) throws Exception {
		if(!clusterRequestQueues.containsKey(topic)) {
			TopicPublisher queue = new TopicPublisher(topic);
			clusterRequestQueues.put(topic, queue);
			
			logger.info("Request Queue for {} has been added.", topic);
		}
	}
	
	public void addClusterAdvisoryQueue(String topic) throws Exception {
		TopicPublisher qp = clusterRequestQueues.get(topic);
		if(qp != null && !clusterAdvisoryQueues.containsKey(topic)) {
			AdvisorySubscriber queue = new AdvisorySubscriber(qp.getDestination());
			queue.setListener(new MasterAdvisoryListener(topic, qp.getDestination()));
			queue.start();
			clusterAdvisoryQueues.put(topic, queue);
			
			logger.info("Advisory Queue for {} has been added.", topic);
		}
	}
	
	public void removeClusterRequestQueue(String topic) throws Exception {
		if(clusterRequestQueues.containsKey(topic)) {
			TopicPublisher qp = clusterRequestQueues.get(topic);
			if(qp != null)
				qp.dispose();
			clusterRequestQueues.remove(topic);
			
			logger.info("Request Queue for {} has been removed.", topic);
		}
	}
	
	public void removeClusterAdvisoryQueue(String topic) throws Exception {
		if(clusterAdvisoryQueues.containsKey(topic)) {
			AdvisorySubscriber as = clusterAdvisoryQueues.get(topic);
			if(as != null)
				as.dispose();
			clusterAdvisoryQueues.remove(topic);
			
			logger.info("Advisory Queue for {} has been removed.", topic);
		}
	}
	
	public void truncateJobs() throws SQLException {
		jobDB.truncateJobs();
	}

}
