package session;

import configuration.inject.ConfigurationValue;
import models.Payment;
import models.PaymentReceipt;
import org.joda.time.DateTime;
import org.redisson.RedissonClient;
import org.redisson.core.RMap;
import play.mvc.Http;
import scala.concurrent.duration.Duration;
import uk.gov.dvla.domain.Offence;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Date;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Singleton
public class SessionManager {

    private static final String KEY = "id";

    @Inject
    private Provider<RedissonClient> redisClientProvider;

    @Inject
    @ConfigurationValue(key = "server.session.maxAge")
    private String timeToLive;

    /**
     * Returns {@link Session} instance linked to session ID read from query string or session cookie.
     * If session ID doesn't exists then new {@link Session} is created with randomly generated session ID.
     *
     * @return session instance
     */
    public Session session() {
        return existingSession().orElseGet(() -> new Session(createNewID(), true));
    }

    /**
     * Returns {@link Session} instance linked to session ID read from query string or session cookie if session ID exists.
     *
     * @return session instance
     */
    public Optional<Session> existingSession() {
        String id = getID();
        if (id == null) {
            return Optional.empty();
        }
        return Optional.of(new Session(id, false));
    }

    private String getID() {
        String id = context().request().getQueryString("session");
        return id != null ? id : context().session().get(KEY);
    }

    private String createNewID() {
        String id = randomUUID().toString();
        context().session().put(KEY, id);
        return id;
    }

    /**
     * Destroys session state stored on the server and removes linked session cookie stored on the client.
     * That method has an effect only if session exists (session ID can be found in query string or session coookie).
     */
    public void destroySession() {
        existingSession().ifPresent(session -> {
            session.clear();
            context().session().clear();
        });
    }

    private Http.Context context() {
        return Http.Context.current();
    }

    public class Session {

        private String id;

        private Session(String id, boolean initiateServerState) {
            this.id = id;

            if (initiateServerState) {
                RMap<String, Object> data = data();
                data.fastPut("creationTime", DateTime.now());
                data.expire(Duration.create(timeToLive).toMillis(), MILLISECONDS);
            }
        }

        /**
         * Returns session ID
         *
         * @return session ID
         */
        public String id() {
            return id;
        }

        /**
         * Reads and parses the offence from the session using {@link SessionProperty#OFFENCE} key.
         * <p>
         * Value is deserialised from JSON string using Jackson deserializer.
         *
         * @return optional offence retrieved from the session
         */
        public Optional<Offence> offence() {
            return Optional.ofNullable(get(SessionProperty.OFFENCE));
        }

        /**
         * Stores offence in the session using {@link SessionProperty#OFFENCE} key.
         * <p>
         * Value is serialised into JSON string using Jackson serializer.
         */
        public void setOffence(Offence offence) {
            checkNotNull(offence, "Offence is required");
            set(SessionProperty.OFFENCE, offence);
        }

        /**
         * Removes offence from the session using {@link SessionProperty#OFFENCE} key.
         */
        public void removeOffence() {
            remove(SessionProperty.OFFENCE);
        }

        /**
         * Reads the payment receipt from the session using {@link SessionProperty#PAYMENT_RECEIPT} key.
         *
         * @return optional payment receipt retrieved from the session
         */
        public Optional<PaymentReceipt> paymentReceipt() {
            return Optional.ofNullable(get(SessionProperty.PAYMENT_RECEIPT));
        }

        /**
         * Stores payment receipt in the session using {@link SessionProperty#PAYMENT_RECEIPT} key.
         */
        public void setPaymentReceipt(PaymentReceipt paymentReceipt) {
            checkNotNull(paymentReceipt, "Payment receipt is required");
            set(SessionProperty.PAYMENT_RECEIPT, paymentReceipt);
        }

        /**
         * Removes payment receipt from the session using {@link SessionProperty#PAYMENT_RECEIPT} key.
         */
        public void removePaymentReceipt() {
            remove(SessionProperty.PAYMENT_RECEIPT);
        }

        /**
         * Reads and parses the locked out date from the session using {@link SessionProperty#LOCKED_DATE} key.
         * <p>
         *
         * @return optional date that the penalty details have been locked, retrieved from the session
         */
        public Optional<Date> lockedDate() {
            return Optional.ofNullable(get(SessionProperty.LOCKED_DATE));
        }

        /**
         * Stores the locked date in the session using {@link SessionProperty#LOCKED_DATE} key.
         * <p>
         */
        public void setLockedDate(Date lockedDate) {
            checkNotNull(lockedDate, "Date is required");
            set(SessionProperty.LOCKED_DATE, lockedDate);
        }

        /**
         * Removes the locked date from the session using {@link SessionProperty#LOCKED_DATE} key.
         */
        public void removeLockedDate() {
            remove(SessionProperty.LOCKED_DATE);
        }

        /**
         * Reads and parses the payment from the session using {@link SessionProperty#PAYMENT} key.
         * <p>
         * Value is deserialised from JSON string using Jackson deserializer.
         *
         * @return optional payment retrieved from the session
         */
        public Optional<Payment> payment() {
            return Optional.ofNullable(get(SessionProperty.PAYMENT));
        }

        /**
         * Stores payment in the session using {@link SessionProperty#PAYMENT} key.
         * <p>
         * Value is serialised into JSON string using Jackson serializer.
         */
        public void setPayment(Payment payment) {
            checkNotNull(payment, "Payment is required");
            set(SessionProperty.PAYMENT, payment);
        }

        /**
         * Removes payment from the session using {@link SessionProperty#PAYMENT} key.
         */
        public void removePayment() {
            remove(SessionProperty.PAYMENT);
        }

        /**
         * Removes all session data associated with session ID
         */
        public void clear() {
            this.data().clear();
        }

        private <T> T get(String key) {
            return this.<T>data().get(key);
        }

        private <T> void set(String key, T value) {
            this.data().fastPut(key, value);
        }

        private void remove(String key) {
            this.data().fastRemove(key);
        }

        private <T> RMap<String, T> data() {
            return redisClientProvider.get().getMap("session:" + id);
        }
    }

    private interface SessionProperty {
        String OFFENCE = "offence";
        String PAYMENT_RECEIPT = "payment-receipt";
        String PAYMENT = "payment";
        String LOCKED_DATE = "locked-date";
    }

}
