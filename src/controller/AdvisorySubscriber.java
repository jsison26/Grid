package controller;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;

public class AdvisorySubscriber {
	private final String connectionUri = "tcp://GENEVIEV:61626";
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer advisoryConsumer;
    
    public AdvisorySubscriber(Destination monitored) throws JMSException {
    	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        destination = session.createTopic(
            AdvisorySupport.getConsumerAdvisoryTopic(monitored).getPhysicalName() + "," +
            AdvisorySupport.getProducerAdvisoryTopic(monitored).getPhysicalName());
        advisoryConsumer = session.createConsumer(destination);
    }
    
    public void start() throws JMSException {
    	connection.start();
    }
    
    public void dispose() throws Exception {
    	if (advisoryConsumer != null) {
    		advisoryConsumer.close();
    	}
    	
        if (connection != null) {
            connection.close();
        }
    }
    
    public Destination getDestination() {
    	return destination;
    }
    
    public void setListener(MessageListener listener) throws JMSException {
    	advisoryConsumer.setMessageListener(listener);
    }
}
