package io.codemonastery.dropwizard.rabbitmq;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.base.Optional;
import com.rabbitmq.client.Connection;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * For documentation about these configurations, see {@link com.rabbitmq.client.ConnectionFactory}.
 *
 * Registers a health check, manages the rabbitmq connection, and adds metrics.
 *
 * Note that automaticRecoveryEnabled and topologyRecoveryEnabled are not exposed because they are assumed to be true.
 */
public class ConnectionFactory extends ConnectionConfiguration {

    private ConnectionMetrics metrics;

    public ConnectionFactory customMetrics(ConnectionMetrics metrics){
        this.metrics = metrics;
        return this;
    }

    /**
     * Synchronously connect to rabbitmq, will cause application to fail if initial connection is unsuccessful.
     * @param env dropwizard environment
     * @param deliveryExecutor executor
     * @param name name of rabbitmq connection
     * @return connection
     * @throws IOException if we cannot connect
     * @throws TimeoutException if we timeout while trying to connect
     */
    public Connection build(final Environment env,
                            final ExecutorService deliveryExecutor,
                            final String name) throws IOException, TimeoutException {
        final HealthCheckRegistry healthChecks = env.healthChecks();
        final LifecycleEnvironment lifecycle = env.lifecycle();
        final MetricRegistry metrics = env.metrics();
        return build(healthChecks, lifecycle, metrics, deliveryExecutor, name);
    }

    /**
     * Synchronously connect to rabbitmq, will throw exception to fail if initial connection is unsuccessful.
     * @param healthChecks health check registry, nullable
     * @param lifecycle lifcycle, nullable
     * @param metrics metric registry, nullable
     * @param deliveryExecutor the executor used by rabbitmq client to deliver messages
     * @param name name of rabbitmq connection
     * @throws IOException if we cannot connect
     * @throws TimeoutException if we timeout while trying to connect
     */
    public Connection build(@Nullable HealthCheckRegistry healthChecks,
                            @Nullable LifecycleEnvironment lifecycle,
                            @Nullable MetricRegistry metrics,
                            ExecutorService deliveryExecutor,
                            String name) throws IOException, TimeoutException {
        final com.rabbitmq.client.ConnectionFactory connectionFactory = makeConnectionFactory();
        final ConnectionMetrics connectionMetrics = Optional.fromNullable(this.metrics)
                .or(() -> {
                    return new DefaultConnectionMetrics(name, metrics);
                });
        final Connection connection = connectionFactory.newConnection(deliveryExecutor);
        registerWithEnvironment(healthChecks, lifecycle, () -> connection, name);
        return new WrappedConnectionMetrics(connectionMetrics).wrap(connection);
    }

    /**
     * Asynchronously connect to rabbitmq, and retry until successful
     * @param env dropwizard environment
     * @param deliveryExecutor the executor used by rabbitmq client to deliver messages
     * @param name name of rabbitmq connection
     * @param callback callback when done - which may be after application start
     */
    public void buildRetryInitialConnect(final Environment env,
                                         final ExecutorService deliveryExecutor,
                                         final String name,
                                         final ConnectedCallback callback) {
        final com.rabbitmq.client.ConnectionFactory connectionFactory = makeConnectionFactory();
        final ScheduledExecutorService initialConnectExecutor = env.lifecycle()
                .scheduledExecutorService(name + "-initial-connect-thread")
                .threads(1)
                .build();

        final ConnectionMetrics connectionMetrics = Optional.fromNullable(metrics)
                .or(() -> new DefaultConnectionMetrics(name, env.metrics()));
        final WrappedConnectionMetrics connectionMetricsWrapper = new WrappedConnectionMetrics(connectionMetrics);
        final ConnectedCallback callbackWithMetrics = connection -> {
            final Connection metricsConnection = connectionMetricsWrapper.wrap(connection);
            callback.connected(metricsConnection);
        };
        final ConnectAsync connectAsync = new ConnectAsync(connectionFactory, deliveryExecutor, name, initialConnectExecutor, callbackWithMetrics);
        registerWithEnvironment(env.healthChecks(), env.lifecycle(), connectAsync::getConnection, name);
        connectAsync.run();
    }

    private void registerWithEnvironment(HealthCheckRegistry healthChecks, LifecycleEnvironment lifecycle, final Supplier<Connection> connection, final String name){
        if (healthChecks != null) {
            healthChecks.register(name, new ConnectionHealthCheck(connection));
        }
        if(lifecycle != null){
            lifecycle.manage(new ManageConnection(connection));
        }
    }
}
