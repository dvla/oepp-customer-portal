package logging;

import akka.dispatch.Dispatcher;
import akka.dispatch.DispatcherPrerequisites;
import akka.dispatch.MessageDispatcher;
import akka.dispatch.MessageDispatcherConfigurator;
import com.typesafe.config.Config;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Configurator for a MDC propagating dispatcher.
 *
 * To use it, configure play framework like this:
 * {{{
 *   akka {
 *     actor {
 *       default-dispatcher = {
 *         type = "logging.MDCPropagatingDispatcherConfigurator"
 *       }
 *     }
 *   }
 * }}}
 */
public class MDCPropagatingDispatcherConfigurator extends MessageDispatcherConfigurator {

    private final Dispatcher instance;

    public MDCPropagatingDispatcherConfigurator(Config config, DispatcherPrerequisites prerequisites) {
        super(config, prerequisites);

        instance = new MDCPropagatingDispatcher(
                this,
                config.getString("id"),
                config.getInt("throughput"),
                Duration.create(config.getDuration("throughput-deadline-time", NANOSECONDS), NANOSECONDS),
                configureExecutor(),
                FiniteDuration.create(config.getDuration("shutdown-timeout", MILLISECONDS), MILLISECONDS)
        );
    }

    @Override
    public MessageDispatcher dispatcher() {
        return instance;
    }
}
