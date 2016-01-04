package io.codemonastery.dropwizard.rabbitmq.example.consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.codemonastery.dropwizard.rabbitmq.ConnectionFactory;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class JobConsumerServiceConfiguration extends Configuration {

    @Valid
    @NotNull
    private ConnectionFactory rabbitMqConsumer = new ConnectionFactory();

    @JsonProperty
    public ConnectionFactory getRabbitMqConsumer() {
        return rabbitMqConsumer;
    }

    @JsonProperty
    public void setRabbitMqConsumer(ConnectionFactory rabbitMqConsumer) {
        this.rabbitMqConsumer = rabbitMqConsumer;
    }
}
