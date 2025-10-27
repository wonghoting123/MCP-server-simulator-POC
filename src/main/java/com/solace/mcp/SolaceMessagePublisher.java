package com.solace.mcp;

import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Solace Message Publisher
 * Handles connection to Solace PubSub+ brokers and message publishing
 */
public class SolaceMessagePublisher {
    private static final Logger logger = LoggerFactory.getLogger(SolaceMessagePublisher.class);
    
    private JCSMPSession session;
    private XMLMessageProducer producer;

    public SolaceMessagePublisher() {
        // Constructor - connections will be created per-request
    }

    /**
     * Send a message to a Solace topic
     * 
     * @param host Solace broker host (e.g., tcp://localhost:55555)
     * @param vpn Message VPN name
     * @param username Username for authentication
     * @param password Password for authentication
     * @param topicName Topic to publish to
     * @param messageText Message content
     * @return true if message was sent successfully, false otherwise
     */
    public boolean sendMessage(String host, String vpn, String username, 
                               String password, String topicName, String messageText) {
        JCSMPSession tempSession = null;
        XMLMessageProducer tempProducer = null;
        
        try {
            // Create session properties
            final JCSMPProperties properties = new JCSMPProperties();
            properties.setProperty(JCSMPProperties.HOST, host);
            properties.setProperty(JCSMPProperties.VPN_NAME, vpn);
            properties.setProperty(JCSMPProperties.USERNAME, username);
            properties.setProperty(JCSMPProperties.PASSWORD, password);
            
            // Optional: Set additional properties for better connection handling
            properties.setProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS, true);
            
            // Create session
            logger.info("Connecting to Solace broker at {} with VPN {}", host, vpn);
            tempSession = JCSMPFactory.onlyInstance().createSession(properties);
            tempSession.connect();
            
            // Create producer
            tempProducer = tempSession.getMessageProducer(new JCSMPStreamingPublishCorrelatingEventHandler() {
                @Override
                public void responseReceivedEx(Object key) {
                    logger.debug("Message acknowledged by broker");
                }

                @Override
                public void handleErrorEx(Object key, JCSMPException cause, long timestamp) {
                    logger.error("Error publishing message: {}", cause.getMessage());
                }
            });
            
            // Create the topic
            final Topic topic = JCSMPFactory.onlyInstance().createTopic(topicName);
            
            // Create and configure the message
            TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            message.setText(messageText);
            
            // Publish the message
            logger.info("Publishing message to topic: {}", topicName);
            tempProducer.send(message, topic);
            logger.info("Message sent successfully");
            
            return true;
            
        } catch (JCSMPException e) {
            logger.error("Error occurred while sending message: {}", e.getMessage(), e);
            return false;
        } finally {
            // Clean up resources
            if (tempProducer != null) {
                tempProducer.close();
            }
            if (tempSession != null) {
                tempSession.closeSession();
            }
        }
    }

    /**
     * Close any persistent connections
     */
    public void close() {
        if (producer != null) {
            producer.close();
            producer = null;
        }
        if (session != null) {
            session.closeSession();
            session = null;
        }
        logger.info("Solace connections closed");
    }
}
