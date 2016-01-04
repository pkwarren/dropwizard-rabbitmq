Dropwizard RabbitMQ
===================
*Why doesn't this exist already...*

[![Build Status](https://travis-ci.org/code-monastery/dropwizard-rabbitmq.svg?branch=master)](https://travis-ci.org/code-monastery/dropwizard-rabbitmq)

Rabbitmq configuration, metrics, health-checks and lifecycle management integrated with dropwizard, focused on common use cases. Inspired by dropwizard-core and [dropwizard-extra](//github.com/datasift/dropwizard-extra).

Configuration
-----
Configurations are mapped to the [ConnectionConfiguration](/src/main/java/io/codemonastery/dropwizard/rabbitmq/ConnectionConfiguration.java) class.
Below is an example configuration for [JobConsumerService](src/test/java/io/codemonastery/dropwizard/rabbitmq/example/consumer/JobConsumerService.java) which is configured by [JobConsumerServiceConfiguration](src/test/java/io/codemonastery/dropwizard/rabbitmq/example/consumer/JobConsumerServiceConfiguration.java).
Naturally you can have multiple configurations per applicatiion.
``` java
rabbitMqConsumer:
  username: guest
  password: guest
  virtualHost: /
  host: localhost
  port: 5672
  requestedChannelMax: 0
  requestedFrameMax: 0
  requestedHeartbeat: 0
  connectionTimeout: 0
  handshakeTimeout: 10000
  shutdownTimeout: 10000
  networkRecoveryInterval: 5000
```

Simple Usage
-----
Easy to use, but if initial connection fails no retry will be performed. This **may** be preferable if your application is running in a container manager which will reset your application on failure.
``` java
final ExecutorService deliveryExecutor = environment.lifecycle()
    .executorService("index-consumer-delivery-thread-pool")
    .maxThreads(configuration.getNumIndexingThreads())
    .build();
final Connection connection = configuration.getRabbitMq()
    .build(environment, deliveryExecutor, "index-consumer");

//give connection to some consumer/publisher
```

Better Usage
------------
Will retry the initial connection, asynchronously calling the callback when it succeeds. The RabbitMQ connection class has reconnect/topology recreate features which are turned on by default in this library, which is why we only need to retry initial connect.
``` java
final ExecutorService deliveryExecutor = environment.lifecycle()
    .executorService("index-consumer-delivery-thread-pool")
    .maxThreads(configuration.getNumIndexingThreads())
    .build();

//this::connected is a callback
configuration.getRabbitMq()
    .buildRetryInitialConnect(environment, deliveryExecutor, "index-consumer", this::connected);
```
