package framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import play.api.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Singleton;

public class ObjectMapperModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).asEagerSingleton();
    }

    @Singleton
    private static class ObjectMapperProvider extends play.core.ObjectMapperProvider {

        @Inject
        public ObjectMapperProvider(ApplicationLifecycle lifecycle) {
            super(lifecycle);
        }

        @Override
        public ObjectMapper get() {
            return super.get().registerModule(new JodaModule());
        }
    }
}
