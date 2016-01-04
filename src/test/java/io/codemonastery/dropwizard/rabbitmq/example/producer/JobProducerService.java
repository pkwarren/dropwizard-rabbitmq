package io.codemonastery.dropwizard.rabbitmq.example.producer;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class JobProducerService extends Application<JobProducerServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new JobProducerService().run(args);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void initialize(Bootstrap<JobProducerServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new JobProducerBundle());
    }

    @Override
    public void run(JobProducerServiceConfiguration configuration, Environment environment) throws Exception {
        //nothing to do here, delivery logic is in the IndexProducerBundle
    }
}
