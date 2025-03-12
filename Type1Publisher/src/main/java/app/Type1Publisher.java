
package app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import domain.Type1Event;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import app.utils.Utils;
import app.utils.LoggerUtil;

import io.github.cdimascio.dotenv.Dotenv;

public class Type1Publisher {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLOUD_AMQP_URL = dotenv.get("CLOUD_AMQP_URL");
    private static final String QUEUE_SUFFIX = "Queue";
    private static final Logger logger = LoggerUtil.getLogger(Type1Publisher.class);

    public static void main(String[] args) {
        logger.info("Type1Publisher started");
        List<String> eventTypes = Utils.getEventTypes();
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(CLOUD_AMQP_URL);
            try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
                logger.info("Channel created");

                for (String eventType : eventTypes) {
                    channel.queueDeclare(eventType + QUEUE_SUFFIX, false, false, false, null);
                    logger.info("Created queue for " + eventType + " with the name of" + eventType + QUEUE_SUFFIX);
                }

                Type1Publisher publisher = new Type1Publisher();

                while (true) {
                    publisher.publishType1Event(channel);
                    sleep(10000);
                }

            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

    }

    void publishType1Event(Channel channel) {
        Type1Event event = new Type1Event(UUID.randomUUID().toString());
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(event);
            oos.flush();
            byte[] eventBytes = bos.toByteArray();
            channel.basicPublish("", event.getClass().getSimpleName() + QUEUE_SUFFIX, null, eventBytes);
            logger.info("Published event " + event.getClass().getSimpleName());
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }
}
