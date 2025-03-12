
package app;

import com.rabbitmq.client.*;
import domain.Type4Event;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import app.utils.Utils;
import app.utils.LoggerUtil;

import io.github.cdimascio.dotenv.Dotenv;

public class Type4Consumer {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLOUD_AMQP_URL = dotenv.get("CLOUD_AMQP_URL");
    private static final String QUEUE_SUFFIX = "Queue";
    private static final Logger logger = LoggerUtil.getLogger(Type4Consumer.class);

    public static void main(String[] args) {
        logger.info(Type4Consumer.class.getSimpleName() + "started");
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

                Type4Consumer consumer = new Type4Consumer();

                DeliverCallback type4EventDeliverCallback = (consumerTag, delivery) -> {

                    consumer.type4EventReceived(delivery);
                    logger.info("Acknowledging message");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                };
                channel.basicConsume(Type4Event.class.getSimpleName() + QUEUE_SUFFIX, false, type4EventDeliverCallback, consumerTag -> { });
                logger.info("Waiting for messages");

                Scanner scanner = new Scanner(System.in);
                scanner.nextLine();

            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    void type4EventReceived(Delivery delivery) {
        byte[] messageBody = delivery.getBody();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(messageBody);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Type4Event event = (Type4Event) ois.readObject();
            if (event != null) {
                logger.info("Successfully decoded the event: " + event.toString());
                processType4Event(event);
            }
        } catch (Exception e) {
            logger.severe("Error deserializing event: " + e.getMessage());
        }
    }

    void processType4Event(Type4Event event) {
        logger.info("Processing " + event.getClass().getSimpleName());
        try {
            sleep(8000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info(event.getClass().getSimpleName() + " processed!");
    }
}
