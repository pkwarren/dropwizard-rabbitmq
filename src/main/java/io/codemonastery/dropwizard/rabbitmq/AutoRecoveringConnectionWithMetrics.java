package io.codemonastery.dropwizard.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;

public class AutoRecoveringConnectionWithMetrics extends ConnectionWithMetrics implements Recoverable {

    AutoRecoveringConnectionWithMetrics(Connection delegate, WrappedConnectionMetrics connectionMetrics) {
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
