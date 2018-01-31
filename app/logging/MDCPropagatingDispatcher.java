package logging;

import akka.dispatch.Dispatcher;
import akka.dispatch.ExecutorServiceFactoryProvider;
import akka.dispatch.MessageDispatcherConfigurator;
import org.slf4j.MDC;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Map;

/**
 * Dispatcher which captures caller MDC context prior delegating 'piece of work' to a worker thread and then sets
 * that MDC context on a worker thread so callee thread have access to the same MDC context as caller thread.
 * <p>
 * When worker thread finished its 'piece of work', it in turn restores original MDC context for a worker thread.
 */
class MDCPropagatingDispatcher extends Dispatcher {

    MDCPropagatingDispatcher(MessageDispatcherConfigurator configurator,
                             String id,
                             int throughput,
                             Duration throughputDeadlineTime,
                             ExecutorServiceFactoryProvider executorServiceFactoryProvider,
                             FiniteDuration shutdownTimeout) {
        super(configurator, id, throughput, throughputDeadlineTime, executorServiceFactoryProvider, shutdownTimeout);
    }

    @Override
    public ExecutionContext prepare() {
        return new ExecutionContext() {

            // capture the caller MDC context
            private final Map<String, String> callerContext = MDC.getCopyOfContextMap();

            @Override
            public void execute(Runnable runnable) {
                MDCPropagatingDispatcher.this.execute(() -> {
                    // backup the callee MDC context
                    final Map<String, String> calleeContext = MDC.getCopyOfContextMap();

                    // run the runnable with caller MDC context
                    setContextMap(callerContext);
                    try {
                        runnable.run();
                    } finally {
                        // restore the callee MDC context
                        setContextMap(calleeContext);
                    }
                });
            }

            @Override
            public void reportFailure(Throwable cause) {
                MDCPropagatingDispatcher.this.reportFailure(cause);
            }

            @Override
            public ExecutionContext prepare() {
                return this;
            }
        };
    }

    private void setContextMap(Map<String, String> context) {
        if (context == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(context);
        }
    }
}
