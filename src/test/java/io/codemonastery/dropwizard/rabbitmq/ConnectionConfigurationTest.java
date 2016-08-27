package io.codemonastery.dropwizard.rabbitmq;

import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class ConnectionConfigurationTest {

    @Test
    public void minimal() throws Exception {
        String configurationString = "{}";
        ConnectionConfiguration configuration = createConfiguration(configurationString);
        assertNull(configuration.getUsername());
        assertNull(configuration.getPassword());
        assertNull(configuration.getVirtualHost());
        assertNull(configuration.getHost());
        assertNull(configuration.getPort());
        assertNull(configuration.getRequestedChannelMax());
        assertNull(configuration.getRequestedFrameMax());
        assertNull(configuration.getRequestedHeartbeat());
        assertNull(configuration.getConnectionTimeout());
        assertNull(configuration.getHandshakeTimeout());
        assertNull(configuration.getShutdownTimeout());
        assertNull(configuration.getClientProperties());
        assertNull(configuration.getNetworkRecoveryInterval());

        com.rabbitmq.client.ConnectionFactory factory = configuration.makeConnectionFactory();
        assertEquals(com.rabbitmq.client.ConnectionFactory.DEFAULT_HEARTBEAT,
                factory.getRequestedHeartbeat());
        assertEquals(com.rabbitmq.client.ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT,
                factory.getConnectionTimeout());
        assertEquals(com.rabbitmq.client.ConnectionFactory.DEFAULT_HANDSHAKE_TIMEOUT,
                factory.getHandshakeTimeout());
        assertEquals(com.rabbitmq.client.ConnectionFactory.DEFAULT_SHUTDOWN_TIMEOUT,
                factory.getShutdownTimeout());
        assertEquals(5000L, factory.getNetworkRecoveryInterval());
    }

    @Test
    public void customHeartbeat() throws Exception {
        String configurationString = "{\"requestedHeartbeat\":\"10s\"}";
        com.rabbitmq.client.ConnectionFactory factory = createConfiguration(configurationString)
                .makeConnectionFactory();
        assertEquals(10, factory.getRequestedHeartbeat());
    }

    @Test
    public void customConnectionTimeout() throws Exception {
        String configurationString = "{\"connectionTimeout\":\"9s\"}";
        com.rabbitmq.client.ConnectionFactory factory = createConfiguration(configurationString)
                .makeConnectionFactory();
        assertEquals(9000, factory.getConnectionTimeout());
    }

    @Test
    public void customHandshakeTimeout() throws Exception {
        String configurationString = "{\"handshakeTimeout\":\"8s\"}";
        com.rabbitmq.client.ConnectionFactory factory = createConfiguration(configurationString)
                .makeConnectionFactory();
        assertEquals(8000, factory.getHandshakeTimeout());
    }

    @Test
    public void customShutdownTimeout() throws Exception {
        String configurationString = "{\"shutdownTimeout\":\"7s\"}";
        com.rabbitmq.client.ConnectionFactory factory = createConfiguration(configurationString)
                .makeConnectionFactory();
        assertEquals(7000, factory.getShutdownTimeout());
    }

    @Test
    public void customNetworkRecoveryTimeout() throws Exception {
        String configurationString = "{\"networkRecoveryInterval\":\"6s\"}";
        com.rabbitmq.client.ConnectionFactory factory = createConfiguration(configurationString)
                .makeConnectionFactory();
        assertEquals(6000, factory.getNetworkRecoveryInterval());
    }

    @Test
    public void maxHeartbeat() throws Exception {
        String configurationString = "{\"requestedHeartbeat\":\"2147483648s\"}";
        try {
            createConfiguration(configurationString);
            fail("Should have thrown a configuration validation exception for requestedHeartbeat");
        } catch (Exception e) {
            String message = String.format("requestedHeartbeat must be less than or equal to %d SECONDS",
                    Integer.MAX_VALUE);
            assertNotNull(e.getMessage());
            assertTrue("unexpected error message: " + e.getMessage(), e.getMessage().contains(message));
        }
    }

    @Test
    public void maxConnectionTimeout() throws Exception {
        String configurationString = "{\"connectionTimeout\":\"2147483648ms\"}";
        try {
            createConfiguration(configurationString);
            fail("Should have thrown a configuration validation exception for connectionTimeout");
        } catch (Exception e) {
            String message = String.format("connectionTimeout must be less than or equal to %d MILLISECONDS",
                    Integer.MAX_VALUE);
            assertNotNull(e.getMessage());
            assertTrue("unexpected error message: " + e.getMessage(), e.getMessage().contains(message));
        }
    }

    @Test
    public void maxHandshakeTimeout() throws Exception {
        String configurationString = "{\"handshakeTimeout\":\"2147483648ms\"}";
        try {
            createConfiguration(configurationString);
            fail("Should have thrown a configuration validation exception for handshakeTimeout");
        } catch (Exception e) {
            String message = String.format("handshakeTimeout must be less than or equal to %d MILLISECONDS",
                    Integer.MAX_VALUE);
            assertNotNull(e.getMessage());
            assertTrue("unexpected error message: " + e.getMessage(), e.getMessage().contains(message));
        }
    }

    @Test
    public void maxShutdownTimeout() throws Exception {
        String configurationString = "{\"shutdownTimeout\":\"2147483648ms\"}";
        try {
            createConfiguration(configurationString);
            fail("Should have thrown a configuration validation exception for shutdownTimeout");
        } catch (Exception e) {
            String message = String.format("shutdownTimeout must be less than or equal to %d MILLISECONDS",
                    Integer.MAX_VALUE);
            assertNotNull(e.getMessage());
            assertTrue("unexpected error message: " + e.getMessage(), e.getMessage().contains(message));
        }
    }


    // Dropwizard throws a NumberFormatException if the networkRecoveryInterval is already larger than Long.MAX_VALUE
    @Ignore
    @Test
    public void maxNetworkRecoveryTimeout() throws Exception {
        String configurationString = "{\"networkRecoveryInterval\":\"9223372036854775808ms\"}";
        try {
            createConfiguration(configurationString);
            fail("Should have thrown a configuration validation exception for networkRecoveryInterval");
        } catch (Exception e) {
            String message = String.format("networkRecoveryInterval must be less than or equal to %d MILLISECONDS",
                    Long.MAX_VALUE);
            assertNotNull(e.getMessage());
            assertTrue("unexpected error message: " + e.getMessage(), e.getMessage().contains(message));
        }
    }

    private ConnectionConfiguration createConfiguration(String configurationString) throws java.io.IOException, io.dropwizard.configuration.ConfigurationException {
        return new DefaultConfigurationFactoryFactory<ConnectionConfiguration>()
                .create(ConnectionConfiguration.class, Validators.newValidator(), Jackson.newObjectMapper(), "")
                .build(s -> new ByteArrayInputStream(s.getBytes()), configurationString);
    }
}
