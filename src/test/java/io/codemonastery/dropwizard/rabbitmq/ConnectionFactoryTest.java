package io.codemonastery.dropwizard.rabbitmq;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Recoverable;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.lifecycle.setup.ScheduledExecutorServiceBuilder;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.ConnectException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

public class ConnectionFactoryTest {

    private ExecutorService deliveryExecutor;
    private ScheduledExecutorService connectExecutor;

    @Mock
    private ScheduledExecutorServiceBuilder scheduledExecutorServiceBuilder;

    @Mock
    private LifecycleEnvironment lifecycle;

    @Mock
    private HealthCheckRegistry healthCheck;

    @Mock
    private MetricRegistry metrics;

    @Mock
    private Environment environment;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        deliveryExecutor = Executors.newSingleThreadExecutor();
        when(environment.lifecycle()).thenReturn(lifecycle);
        when(environment.healthChecks()).thenReturn(healthCheck);
        when(environment.metrics()).thenReturn(metrics);
        when(lifecycle.scheduledExecutorService(anyString())).thenReturn(scheduledExecutorServiceBuilder);
        when(scheduledExecutorServiceBuilder.threads(anyInt())).thenReturn(scheduledExecutorServiceBuilder);
        when(scheduledExecutorServiceBuilder.build()).thenAnswer(invocationOnMock -> {
            if (connectExecutor != null) {
                throw new AssertionError("Should not be called more than once per test");
            }
            connectExecutor = Executors.newSingleThreadScheduledExecutor();
            return connectExecutor;
        });
    }

    @After
    public void tearDown() throws Exception {
        deliveryExecutor.shutdownNow();
        deliveryExecutor = null;
        if (connectExecutor != null) {
            connectExecutor.shutdownNow();
            connectExecutor = null;
        }
    }

    @Test
    public void synchronousStartAndDeclareQueue() throws Exception {
        Connection connection = null;
        try {
            connection = new ConnectionFactory().build(environment, deliveryExecutor, "ConnectionFactoryTest");
            Channel channel = null;
            try {
                channel = connection.createChannel();
                //noinspection unused
                final AMQP.Queue.DeclareOk declareOk = channel.queueDeclare();
            } finally {
                if (channel != null) {
                    channel.close();
                }
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Test
    public void synchronousStartFailure() throws Exception {
        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setPort(5671);
        connectionFactory.setConnectionTimeout(Duration.milliseconds(100));
        Connection connection = null;
        try {
            connection = connectionFactory.build(environment, deliveryExecutor, "ConnectionFactoryTest");
            fail("expected connection failure");
        } catch (ConnectException e) {
            //expected
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Test
    public void asynchronousStartAndDeclareQueue() throws Exception {
        boolean[] called = {false};
        new ConnectionFactory().buildRetryInitialConnect(environment, deliveryExecutor, "ConnectionFactoryTest", connection -> {
            Channel channel = null;
            try {
                channel = connection.createChannel();
                //noinspection unused
                final AMQP.Queue.DeclareOk declareOk = channel.queueDeclare();
            } finally {
                if (channel != null) {
                    channel.close();
                }
            }
            called[0] = true;
        });
        assertTrue(called[0]);
    }

    @Test
    public void asynchronousStartFailureWillRetry() throws Exception {
        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setPort(5671);
        connectionFactory.setConnectionTimeout(Duration.milliseconds(10));

        boolean[] called = {false};
        connectionFactory.buildRetryInitialConnect(
                environment,
                deliveryExecutor,
                "ConnectionFactoryTest",
                connection -> called[0] = true
        );
        Thread.sleep(1000);
        assertFalse(called[0]);
    }

    @Test
     public void testRegisterRecoverWithNetworkRecovery() throws Exception {
        Connection connection = null;
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.build(environment, deliveryExecutor, "ConnectionFactoryTest");
            assertTrue(connection instanceof Recoverable);

            Channel channel = null;
            try {
                channel = connection.createChannel();
                assertTrue(channel instanceof Recoverable);

            } finally {
                if (channel != null) {
                    channel.close();
                }
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /*
        Must disable automaticRecovery in ConnectionConfiguration for this to work.
        Since that is always enabled, this test will never pass unless we change our minds
        about what is configurable.
     */
    @Ignore
    @Test
    public void testRegisterRecoverWithNoNetworkRecovery() throws Exception {
        Connection connection = null;
        try {
            connection = new ConnectionFactory().build(environment, deliveryExecutor, "ConnectionFactoryTest");
            assertFalse(connection instanceof Recoverable);

            Channel channel = null;
            try {
                channel = connection.createChannel();
                assertFalse(channel instanceof Recoverable);

            } finally {
                if (channel != null) {
                    channel.close();
                }
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

}
