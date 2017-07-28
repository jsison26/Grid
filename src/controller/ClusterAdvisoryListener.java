package controller;

import java.sql.SQLException;
import java.util.List;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.CommandTypes;
import org.apache.activemq.command.DataStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Cluster;
import model.ClusterStatus;
import model.Job;


public class ClusterAdvisoryListener implements MessageListener {
	private static final Logger logger =  LoggerFactory.getLogger(ClusterAdvisoryListener.class);
	private Destination monitored;
	
	public ClusterAdvisoryListener(Destination monitored) {
		this.monitored = monitored;
	}
	
	public void onMessage(Message message) {
        try {
        	ActiveMQMessage msg = (ActiveMQMessage) message;
        	DataStructure ds = msg.getDataStructure();
        	
        	Destination source = message.getJMSDestination();
            if (source.equals(AdvisorySupport.getConsumerAdvisoryTopic(monitored))) {
            	if (ds != null) {
                	switch (ds.getDataStructureType()) {
            		case CommandTypes.CONSUMER_INFO:
            			Cluster cluster = ClusterManagerController.getInstance().getCluster();
            			
            			if(getQueuedJobs(cluster.getClusterName()).size() > 0)
            				cluster.setStatus(ClusterStatus.BUSY);
            			else
            				cluster.setStatus(ClusterStatus.AVAILABLE);
            			 
            			updateCluster(cluster); 
            			
            			logger.info("MASTER connected.");
            			break;
            		case CommandTypes.REMOVE_INFO:
            			break;
            		}
            	} else {
            		logger.info("No data structure provided: {}", message);
            	}
            } else if (source.equals(AdvisorySupport.getProducerAdvisoryTopic(monitored))) {
            	
            	
            }
            
        } catch (Exception e) {
            logger.error("Cannot process advisory message: {}", message);
            logger.error(Utilities.getStackTrace(e));
        }
    }
	
	private void updateCluster(Cluster cluster) throws SQLException {
		ClusterManagerController.getInstance().updateCluster(cluster);
	}
	
	private List<Job> getQueuedJobs(String clusterName) {
		return ClusterManagerController.getInstance().getQueuedJob(clusterName);
	}
}
