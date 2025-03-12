
package app;

import com.rabbitmq.client.*;
import domain.Type1Event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import app.utils.Utils;
import app.utils.LoggerUtil;

import io.github.cdimascio.dotenv.Dotenv;

public class Type1Consumer {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLOUD_AMQP_URL = dotenv.get("CLOUD_AMQP_URL");
    private static final String QUEUE_SUFFIX = "Queue";
    private static final Logger logger = LoggerUtil.getLogger(Type1Consumer.class);

    public static void main(String[] args) {
        logger.info(Type1Consumer.class.getSimpleName() + "started");
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

                Type1Consumer consumer = new Type1Consumer();

                DeliverCallback type1EventDeliverCallback = (consumerTag, delivery) -> {

                    consumer.type1EventReceived(delivery);
                    logger.info("Acknowledging message");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                };
                channel.basicConsume(Type1Event.class.getSimpleName() + QUEUE_SUFFIX, false, type1EventDeliverCallback, consumerTag -> { });
                logger.info("Waiting for messages");

                Scanner scanner = new Scanner(System.in);
                scanner.nextLine();

            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    void type1EventReceived(Delivery delivery) {
        byte[] messageBody = delivery.getBody();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(messageBody);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Type1Event event = (Type1Event) ois.readObject();
            if (event != null) {
                logger.info("Successfully decoded the event: " + event.toString());
                processType1Event(event);
            }
        } catch (Exception e) {
            logger.severe("Error deserializing event: " + e.getMessage());
        }
    }

    void processType1Event(Type1Event event) {
        logger.info("Processing " + event.getClass().getSimpleName());
        try {
            sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info(event.getClass().getSimpleName() + " processed!");
    }
}
