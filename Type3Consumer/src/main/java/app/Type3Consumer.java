
package app;

import com.rabbitmq.client.*;
import domain.Type3Event;
import domain.Type4Event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import app.utils.Utils;
import app.utils.LoggerUtil;

import io.github.cdimascio.dotenv.Dotenv;

public class Type3Consumer {
    private static final Random RANDOM = new Random();
    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLOUD_AMQP_URL = dotenv.get("CLOUD_AMQP_URL");
    private static final String QUEUE_SUFFIX = "Queue";
    private static final Logger logger = LoggerUtil.getLogger(Type3Consumer.class);

    public static void main(String[] args) {
        logger.info(Type3Consumer.class.getSimpleName() + "started");
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

                Type3Consumer consumer = new Type3Consumer();

                DeliverCallback type3EventDeliverCallback = (consumerTag, delivery) -> {

                    consumer.type3EventReceived(delivery);
                    logger.info("Acknowledging message");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    consumer.publishType4Event(channel);

                };
                channel.basicConsume(Type3Event.class.getSimpleName() + QUEUE_SUFFIX, false, type3EventDeliverCallback, consumerTag -> { });
                logger.info("Waiting for messages");

                Scanner scanner = new Scanner(System.in);
                scanner.nextLine();

            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    void type3EventReceived(Delivery delivery) {
        byte[] messageBody = delivery.getBody();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(messageBody);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Type3Event event = (Type3Event) ois.readObject();
            if (event != null) {
                logger.info("Successfully decoded the event: " + event.toString());
                processType3Event(event);
            }
        } catch (Exception e) {
            logger.severe("Error deserializing event: " + e.getMessage());
        }
    }

    void processType3Event(Type3Event event) {
        logger.info("Processing " + event.getClass().getSimpleName());
        try {
            int waitSecs = RANDOM.nextInt(3, 16);
            sleep(waitSecs * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info(event.getClass().getSimpleName() + " processed!");
    }

    void publishType4Event(Channel channel) {
        Type4Event event = new Type4Event(UUID.randomUUID().toString());
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
