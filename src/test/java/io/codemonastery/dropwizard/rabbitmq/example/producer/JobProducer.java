package io.codemonastery.dropwizard.rabbitmq.example.producer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class JobProducer {

    private static final Logger LOG = LoggerFactory.getLogger(JobProducer.class);

    private final AtomicLong jodIdFactory = new AtomicLong(0L);

    public void send(Channel channel) {
        try{
            final Long jobId = jodIdFactory.incrementAndGet();
            channel.basicPublish("job", "index", new AMQP.BasicProperties(), jobId.toString().getBytes());
            LOG.info(jobId + " sent");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
