package controller;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import model.Cluster;
import model.ClusterStatus;
import model.Job;
import model.TopicMessageType;


public class ClusterTopicListener implements MessageListener {
	private static final Logger logger =  LoggerFactory.getLogger(ClusterTopicListener.class);
	
    public void onMessage(Message message) {
        try {
        	if(message instanceof TextMessage) {
        		Gson gson = new Gson();
        		
        		String messageType = message.getStringProperty("MessageType");
            	String targetMachine = message.getStringProperty("TargetMachine");
            	String object = message.getStringProperty("Object");
            	
            	logger.info("messageType={}, object={}", messageType, object);
            	
            	if(!targetMachine.equals(ClusterManagerController.getInstance().getCluster().getClusterName()))
            		return;
            	
            	if(messageType.equals(TopicMessageType.PING)) {
            		Cluster cluster = ClusterManagerController.getInstance().getCluster();
            		if(ClusterManagerController.getInstance().getQueuedJob(cluster.getClusterName()).size() > 0)
            			cluster.setStatus(ClusterStatus.BUSY);
            		else
            			cluster.setStatus(ClusterStatus.AVAILABLE);
            		
            		ClusterManagerController.getInstance().updateCluster(cluster);
            	}
            	else if(messageType.equals(TopicMessageType.UPDATE_CLUSTER)) {
            		Cluster cluster = gson.fromJson(object, Cluster.class);
            		
            		if(cluster.getClusterId() > 0)
            			ClusterManagerController.getInstance().updateCluster(cluster);
            	}
            	else if(messageType.equals(TopicMessageType.UPDATE_JOB)) {
            		Job job = gson.fromJson(object, Job.class);
            		
            		if(job.getJobId() > 0)
            			ClusterManagerController.getInstance().updateJob(job);
            	}
        	}
        	else {
        		logger.info("Message is not an instance of TextMessage.");
        	}
        } catch (Exception e) {
            logger.error("Cannot parse message:{}", message);
            logger.error(Utilities.getStackTrace(e));
        }
    }
}