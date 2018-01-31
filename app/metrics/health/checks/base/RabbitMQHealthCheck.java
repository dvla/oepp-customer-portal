package metrics.health.checks.base;

import com.codahale.metrics.health.HealthCheck;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static java.lang.System.currentTimeMillis;

public class RabbitMQHealthCheck extends HealthCheck {

    private static final String EXCHANGE_NAME = "validation-exchange";

    private final ConnectionFactory connectionFactory;
    private long timeoutInMilliseconds = 2000;

    protected RabbitMQHealthCheck(String host, int port, String username, String password) {
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(host);
        this.connectionFactory.setPort(port);
        this.connectionFactory.setUsername(username);
        this.connectionFactory.setPassword(password);
    }

    protected void setTimeout(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    @Override
    protected Result check() {
        String messageId = UUID.randomUUID().toString();

        return withChannel(channel -> {
            ReturnMailbox returnMailbox = new ReturnMailbox();
            channel.addReturnListener(returnMailbox);

            long publishTime = currentTimeMillis();
            try {
                channel.exchangeDeclare(EXCHANGE_NAME, "direct");
                channel.basicPublish(EXCHANGE_NAME, "", true, messageProperties(messageId), "ping".getBytes());
            } catch (IOException ex) {
                return Result.unhealthy(ex);
            }

            boolean receivedReturn = false;
            while (!(receivedReturn = returnMailbox.hasReceivedReturn(messageId)) && !hasTimedOut(publishTime)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // ignore exception
                }
            }
            return receivedReturn ? Result.healthy() : Result.unhealthy("Published PING message has not been received back in expected " + timeoutInMilliseconds + " ms");
        });
    }

    private Result withChannel(Function<Channel, Result> function) {
        Connection connection = null;
        Channel channel = null;
        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            return function.apply(channel);
        } catch (IOException | TimeoutException ex) {
            return Result.unhealthy(ex); // Unable to create connection or channel
        } finally {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException | TimeoutException ex) { // Unable to close channel
                    // ignore
                }
            }
            if (connection != null && connection.isOpen()) {
                try {
                    connection.close();
                } catch (IOException ex) { // Unable to close connection
                    // ignore
                }
            }
        }
    }

    private AMQP.BasicProperties messageProperties(String messageId) {
        return new AMQP.BasicProperties.Builder()
                .messageId(messageId)
                .contentType("plain/text")
                .deliveryMode(1) // non-persistent mode
                .build();
    }

    private boolean hasTimedOut(long publishTime) {
        return currentTimeMillis() - publishTime > timeoutInMilliseconds;
    }

    private class ReturnMailbox implements ReturnListener {

        private Map<String, String> receivedReturns;

        private ReturnMailbox() {
            this.receivedReturns = new HashMap<>();
        }

        @Override
        public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
            receivedReturns.put(properties.getMessageId(), new String(body));
        }

        private boolean hasReceivedReturn(String messageId) {
            return this.receivedReturns.containsKey(messageId);
        }
    }

}
