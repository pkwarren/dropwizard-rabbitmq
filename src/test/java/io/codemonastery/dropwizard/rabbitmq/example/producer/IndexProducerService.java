package io.codemonastery.dropwizard.rabbitmq.example.producer;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class IndexProducerService extends Application<IndexProducerServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new IndexProducerService().run(args);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void initialize(Bootstrap<IndexProducerServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new IndexProducerBundle());
    }

    @Override
    public void run(IndexProducerServiceConfiguration configuration, Environment environment) throws Exception {
        //nothing to do here, delivery logic is in the IndexProducerBundle
    }
}
