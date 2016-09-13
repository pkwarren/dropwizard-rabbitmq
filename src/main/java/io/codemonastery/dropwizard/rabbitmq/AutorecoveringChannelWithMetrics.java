package io.codemonastery.dropwizard.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;

public class AutorecoveringChannelWithMetrics extends ChannelWithMetrics implements Recoverable {

    public AutorecoveringChannelWithMetrics(Channel delegate, WrappedConnectionMetrics connectionMetrics) {
        super(delegate, connectionMetrics);
        if (!(delegate instanceof Recoverable)) {
            throw new IllegalArgumentException("Was expecting delegate to implement recoverable");
        }
    }

    @Override
    public void addRecoveryListener(RecoveryListener recoveryListener) {
        ((Recoverable) delegate).addRecoveryListener(recoveryListener);
    }

    @Override
    public void removeRecoveryListener(RecoveryListener recoveryListener) {
        ((Recoverable) delegate).removeRecoveryListener(recoveryListener);
    }
}
