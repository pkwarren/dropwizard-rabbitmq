package io.codemonastery.dropwizard.rabbitmq.example.consumer;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class IndexConsumerService extends Application<IndexConsumerServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new IndexConsumerService().run(args);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void initialize(Bootstrap<IndexConsumerServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new IndexConsumerBundle());
    }

    @Override
    public void run(IndexConsumerServiceConfiguration configuration, Environment environment) throws Exception {
        //nothing to do here, consuming logic is in the IndexConsumerBundle
    }
}
