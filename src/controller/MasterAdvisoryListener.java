package controller;

import java.sql.SQLException;

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


public class MasterAdvisoryListener implements MessageListener {
	private static final Logger logger =  LoggerFactory.getLogger(MasterAdvisoryListener.class);
	private String topic;
	private Destination monitored;
	
	
	public MasterAdvisoryListener(String topic, Destination monitored) {
		this.topic = topic;
		this.monitored = monitored;
	}
	
	public void onMessage(Message message) {
        try {
        	ActiveMQMessage msg = (ActiveMQMessage) message;
        	DataStructure ds = msg.getDataStructure();
        	Cluster cluster;
        	
        	Destination source = message.getJMSDestination();
            if (source.equals(AdvisorySupport.getConsumerAdvisoryTopic(monitored))) {
            	if (ds != null) {
                	switch (ds.getDataStructureType()) {
                	/*
                	case CommandTypes.CONSUMER_INFO:
                		topic = ((ConsumerInfo) ds).getClientId();
            			
            			addClusterRequestQueue(topic);
            			addClusterAdvisoryQueue(topic);
            			
            			cluster = MasterController.getInstance().getClusterByClusterName(topic);
            			if(cluster == null)
            				return;
            			
            			cluster.setStatus(ClusterStatus.AVAILABLE); 
            			updateCluster(cluster); 
            			
            			break;
            			*/
                	case CommandTypes.REMOVE_INFO:
            			
            			if(topic == null)
            				return;
            			
            			removeClusterAdvisoryQueue(topic);
            			removeClusterRequestQueue(topic);
            			
            			cluster = GridManagerController.getInstance().getClusterByClusterName(topic);
            			if(cluster == null)
            				return;
            			
            			cluster.setStatus(ClusterStatus.UNAVAILABLE); 
            			updateCluster(cluster);
            			
            			logger.info("{} disconnected.", topic);
            			break;
                	}
            	} else {
            		logger.info("No data structure provided: {}", message);
            	}
            } else if (source.equals(AdvisorySupport.getProducerAdvisoryTopic(monitored))) {
            	if (ds != null) {
            		switch (ds.getDataStructureType()) {
                	/*
                	case CommandTypes.PRODUCER_INFO:
            			if(topic == null)
            				return;
            			
            			addClusterRequestQueue(topic);
            			addClusterAdvisoryQueue(topic);
            			
            			cluster = MasterController.getInstance().getClusterByClusterName(topic);
            			if(cluster == null)
            				return;
            			
            			cluster.setStatus(ClusterStatus.AVAILABLE); 
            			updateCluster(cluster); 
            			
            			break;*/
            		
            		}
            	} else {
            		logger.info("No data structure provided: {}", message);
            	}
            }
        } catch (Exception e) {
            logger.error("Cannot process advisory message: {}", message);
            logger.error(Utilities.getStackTrace(e));
        }
    }
	
	private void updateCluster(Cluster cluster) throws SQLException {
		GridManagerController.getInstance().updateCluster(cluster);
	}
	
	private void removeClusterRequestQueue(String topic) throws Exception {
		GridManagerController.getInstance().removeClusterRequestQueue(topic);
	}
	
	private void removeClusterAdvisoryQueue(String topic) throws Exception {
		GridManagerController.getInstance().removeClusterAdvisoryQueue(topic);
	}
}
