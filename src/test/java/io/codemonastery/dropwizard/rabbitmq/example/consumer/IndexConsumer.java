package io.codemonastery.dropwizard.rabbitmq.example.consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class IndexConsumer extends DefaultConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(IndexConsumer.class);
    public static final AMQP.BasicProperties PROPS = new AMQP.BasicProperties();

    public IndexConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        if (getChannel().isOpen()) {
            //channels are meant for use within one thread
            //so we can use the consumers channel to publish
            final long job = Long.parseLong(new String(body));
            sendStatus("STARTED", job);
            try {
                //fake index work
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOG.error("Could not process index job", e);
            }
            sendStatus("FINISHED", job);
            getChannel().basicAck(envelope.getDeliveryTag(), false);
        }
    }

    private void sendStatus(String status, long job) throws IOException {
        final String message = (job + " " + status);
        getChannel().basicPublish("job_status", "index", true, PROPS, message.getBytes());
        LOG.info(message);
    }
}
