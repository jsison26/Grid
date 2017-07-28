package controller;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class TopicSubscriber {
	private final String connectionUri = "tcp://GENEVIEV:61626";
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer subscriber;

    
    public TopicSubscriber(String clientId, String topic) throws Exception {
    	try {
	        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionUri);
	        connection = connectionFactory.createConnection();
	        connection.setClientID(clientId);
	        
	        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	        destination = session.createTopic(topic);
	        subscriber = session.createConsumer(destination);
    	}
    	catch(Exception e) {
    		System.out.println(e.getMessage());
    		System.out.println(e.getStackTrace());
    		Utilities.getStackTrace(e);
    	}
    }
    
    public void start() throws JMSException {
    	connection.start();
    }
    
    public void dispose() throws Exception {
    	if (subscriber != null) {
    		subscriber.close();
    	}
    	
        if (connection != null) {
            connection.close();
        }
    }

    public Destination getDestination() {
    	return destination;
    }
    
    public void setMessageListener(MessageListener listener) throws Exception {
        subscriber.setMessageListener(listener);
    }
}
