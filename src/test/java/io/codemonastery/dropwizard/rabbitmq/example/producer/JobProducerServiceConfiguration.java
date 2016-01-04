package io.codemonastery.dropwizard.rabbitmq.example.producer;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.codemonastery.dropwizard.rabbitmq.ConnectionFactory;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class JobProducerServiceConfiguration extends Configuration {

    @Valid
    @NotNull
    private ConnectionFactory rabbitMqProducer = new ConnectionFactory();


    @JsonProperty
    public ConnectionFactory getRabbitMqProducer() {
        return rabbitMqProducer;
    }

    @JsonProperty
    public void setRabbitMqProducer(ConnectionFactory rabbitMqProducer) {
        this.rabbitMqProducer = rabbitMqProducer;
    }
}
