
package app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import domain.Type2Event;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import app.utils.Utils;
import app.utils.LoggerUtil;

import io.github.cdimascio.dotenv.Dotenv;

public class Type2Publisher {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLOUD_AMQP_URL = dotenv.get("CLOUD_AMQP_URL");
    private static final String QUEUE_SUFFIX = "Queue";
    private static final Logger logger = LoggerUtil.getLogger(Type2Publisher.class);

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

                Type2Publisher publisher = new Type2Publisher();

                while (true) {
                    publisher.publishType2Event(channel);
                    Random rand = new Random();
                    int waitSecs = rand.nextInt(5, 21);
                    sleep(waitSecs * 1000L);
                }

            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

    }


    void publishType2Event(Channel channel) {
        Type2Event event = new Type2Event(UUID.randomUUID().toString());
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
