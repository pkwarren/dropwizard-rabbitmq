package io.codemonastery.dropwizard.rabbitmq.example.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IndexProducerBundle implements ConfiguredBundle<IndexProducerServiceConfiguration> {

    private final IndexProducer producer = new IndexProducer();
    private volatile Channel channel;

    public IndexProducerBundle() {

    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(IndexProducerServiceConfiguration configuration, Environment environment) throws Exception {
        final ScheduledExecutorService sendExecutor = environment.lifecycle()
                .scheduledExecutorService("send-executor")
                .build();
        sendExecutor.scheduleAtFixedRate(()->{
           if(channel != null && channel.isOpen()){
               producer.send(channel);
           }
        }, 1, 1, TimeUnit.SECONDS);

        configuration.getRabbitMqProducer()
                .buildRetryInitialConnect(environment, sendExecutor, "index-producer", this::connected);
    }

    private void connected(Connection connection) throws Exception {
        this.channel = connection.createChannel();
        setupIndexJobQueue(channel);
    }
    private static void setupIndexJobQueue(Channel channel) throws IOException {
        channel.exchangeDeclare("job", "topic", true);
        channel.queueDeclare("index_job", true, false, false, new HashMap<>());
        channel.queueBind("index_job", "job", "index");
    }
}
