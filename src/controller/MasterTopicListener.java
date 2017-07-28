package controller;

import java.sql.SQLException;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import model.Cluster;
import model.Job;
import model.TopicMessageType;


public class MasterTopicListener implements MessageListener {
	private static final Logger logger =  LoggerFactory.getLogger(MasterTopicListener.class);
	
    public void onMessage(Message message) {
        try {
        	if(message instanceof TextMessage) {
        		Gson gson = new Gson();
        		
        		String messageType = message.getStringProperty("MessageType");
        		String machine = message.getStringProperty("Machine");
            	String object = message.getStringProperty("Object");
            	
            	//logger.info("messageType={}, cluster={}, object={}", messageType, machine, object);
            	           	
            	if(messageType.equals(TopicMessageType.UPDATE_CLUSTER)) {
            		Cluster cluster = gson.fromJson(object, Cluster.class);
            		if(cluster.getClusterId() > 0) {
            			addClusterRequestQueue(cluster.getClusterName());
            			addClusterAdvisoryQueue(cluster.getClusterName());
            			
            			boolean clusterExist = GridManagerController.getInstance().getClusterByClusterName(cluster.getClusterName()) == null ? false : true;
            			if(!clusterExist)
            				return;
            			
            			updateCluster(cluster);
            		}
            	}
            	else if(messageType.equals(TopicMessageType.UPDATE_JOB)) {
            		Job job = gson.fromJson(object, Job.class);
            		
            		if(job.getJobId() > 0)
            			updateJob(job);
            	}
        	}
        	else {
        		// to be removed. this is only here for testing
        		logger.info("Message is not an instance of TextMessage.");
        	}
        } catch (Exception e) {
            logger.error("Cannot parse message:{}", message);
            logger.error(Utilities.getStackTrace(e));
        }
    }
    
    private void addClusterRequestQueue(String topic) throws Exception {
		GridManagerController.getInstance().addClusterRequestQueue(topic);
	}
	
	private void addClusterAdvisoryQueue(String topic) throws Exception {
		GridManagerController.getInstance().addClusterAdvisoryQueue(topic);
	}
    
    private void updateCluster(Cluster cluster) throws SQLException {
    	GridManagerController.getInstance().updateCluster(cluster);
    }
    
    private void updateJob(Job job) throws SQLException {
    	GridManagerController.getInstance().updateJob(job);
    }

}
