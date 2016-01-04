package io.codemonastery.dropwizard.rabbitmq.example.consumer;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class JobConsumerService extends Application<JobConsumerServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new JobConsumerService().run(args);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void initialize(Bootstrap<JobConsumerServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new JobConsumerBundle());
    }

    @Override
    public void run(JobConsumerServiceConfiguration configuration, Environment environment) throws Exception {
        //nothing to do here, consuming logic is in the IndexConsumerBundle
    }
}
