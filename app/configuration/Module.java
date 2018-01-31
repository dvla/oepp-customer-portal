package configuration;

import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.typesafe.config.ConfigValue;
import configuration.inject.ConfigurationValue;
import configuration.inject.ConfigurationValueImpl;
import play.Configuration;
import play.Environment;
import play.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import static com.google.inject.multibindings.OptionalBinder.newOptionalBinder;
import static java.nio.charset.Charset.defaultCharset;

public class Module extends AbstractModule {

    private final Environment environment;
    private final Configuration configuration;

    public Module(Environment environment, Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        addDefaultConfigurationValueBindings();

        for (Map.Entry<String, ConfigValue> configurationEntry : configuration.entrySet()) {
            String key = configurationEntry.getKey();
            Object value = configurationEntry.getValue().unwrapped();

            addConfigurationValueBinding(key, value);

            if (value instanceof String) {
                readResource((String) value).ifPresent(resource -> addConfigurationValueAsResourceBinding(key, resource));
            }
        }
    }

    private void addDefaultConfigurationValueBindings() {
        newOptionalBinder(binder(), Key.get(String.class, ConfigurationValue.class)).setDefault().toProvider(() -> null);
        newOptionalBinder(binder(), Key.get(Integer.class, ConfigurationValue.class)).setDefault().toProvider(() -> null);
        newOptionalBinder(binder(), Key.get(Boolean.class, ConfigurationValue.class)).setDefault().toProvider(() -> null);
    }

    @SuppressWarnings("unchecked")
    private <T> void addConfigurationValueBinding(String name, T value) {
        newOptionalBinder(binder(), Key.get((Class<T>) value.getClass(), new ConfigurationValueImpl(name, false))).setBinding().toInstance(value);
    }

    private Optional<String> readResource(String path) {
        if (path.startsWith("classpath:")) {
            try {
                URL resource = environment.resource(path.substring(10));
                return Optional.of(Resources.toString(resource, defaultCharset()));
            } catch (IOException ex) {
                Logger.error("Cannot read resource {}", path, ex);
            }
        }
        return Optional.empty();
    }

    private void addConfigurationValueAsResourceBinding(String name, String value) {
        newOptionalBinder(binder(), Key.get(String.class, new ConfigurationValueImpl(name, true))).setBinding().toInstance(value);
    }
}
