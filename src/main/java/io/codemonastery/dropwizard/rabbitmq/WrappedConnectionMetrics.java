package io.codemonastery.dropwizard.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Recoverable;

/**
 * Internal use.
 */
class WrappedConnectionMetrics implements ConnectionMetrics {

    private final ConnectionMetrics delegate;

    public WrappedConnectionMetrics(ConnectionMetrics delegate) {

        this.delegate = delegate;
    }

    public Connection wrap(Connection connection) {
        return connection instanceof Recoverable
                ? new AutoRecoveringConnectionWithMetrics(connection, this)
                : new ConnectionWithMetrics(connection, this);
    }

    public Channel wrap(Channel channel) {
        return channel instanceof Recoverable
                ? new AutorecoveringChannelWithMetrics(channel, this)
                : new ChannelWithMetrics(channel, this);
    }

    public Consumer wrap(Consumer callback) {
        return new ConsumerWithMetrics(callback, this);
    }

    @Override
    public void delivered() {
        delegate.delivered();
    }

    @Override
    public void acked() {
        delegate.acked();
    }

    @Override
    public void nacked() {
        delegate.nacked();
    }

    @Override
    public void rejected() {
        delegate.rejected();
    }

    @Override
    public void published() {
        delegate.published();
    }

}
