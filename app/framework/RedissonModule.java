package framework;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import play.Configuration;
import play.Environment;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public class RedissonModule extends AbstractModule {

    private static final String ADDRESS_CONFIGURATION_KEY = "redisDatabase.address";
    private static final String PASSWORD_CONFIGURATION_KEY = "redisDatabase.password";

    private final Config config;

    public RedissonModule(Environment environment, Configuration configuration) {
        validate(configuration);

        config = new Config();
        config.useSingleServer()
                .setAddress(configuration.getString(ADDRESS_CONFIGURATION_KEY))
                .setPassword(configuration.getString(PASSWORD_CONFIGURATION_KEY));
        config.setCodec(new JsonJacksonCodec() {

            @Override
            protected ObjectMapper initObjectMapper() {
                ObjectMapper objectMapper = super.initObjectMapper();
                objectMapper.registerModule(new Jdk8Module());
                objectMapper.registerModule(new JodaModule());
                return objectMapper;
            }
        });
    }

    private void validate(Configuration configuration) {
        checkArgument(!isNullOrEmpty(configuration.getString(ADDRESS_CONFIGURATION_KEY)), "Database address is required");
        checkArgument(!isNullOrEmpty(configuration.getString(PASSWORD_CONFIGURATION_KEY)), "Database password is required");
    }

    @Override
    protected void configure() {
        bind(RedissonClient.class).toProvider(() -> Redisson.create(config)).in(Scopes.SINGLETON);
    }

}
