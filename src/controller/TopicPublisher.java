package controller;

//import java.util.ArrayList;
//import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.google.gson.Gson;

import model.TopicMessage;

public class TopicPublisher {
	private final String connectionUri = "tcp://GENEVIEV:61626";
    private Connection connection;
    private Session session;
    private MessageProducer publisher;

    
    public TopicPublisher(String topic) throws Exception {
    	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        publisher = session.createProducer(session.createTopic(topic));
    }
    
    public void dispose() throws Exception {
    	if (publisher != null) {
    		publisher.close();
    	}
    	
        if (connection != null) {
            connection.close();
        }
    }

    public Destination getDestination() throws JMSException {
    	return publisher.getDestination();
    }
    
    public void sendTopic(TopicMessage jsonMessage) throws Exception {
        Message message = (TextMessage) session.createTextMessage();
        
        message.setStringProperty("MessageType", jsonMessage.getMessageType());
        message.setStringProperty("Machine", jsonMessage.getMachine());
        message.setStringProperty("TargetMachine", jsonMessage.getTargetMachine());
        message.setStringProperty("Object", new Gson().toJson(jsonMessage.getObject()));
        
        publisher.send(message);
    }
}


















