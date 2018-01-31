package configuration;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import configuration.inject.ConfigurationValue;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import javax.annotation.Nullable;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ConfigurationValueInjectorTest extends WithApplication {

    private final GuiceApplicationBuilder builder;

    public ConfigurationValueInjectorTest() {
        this.builder = new GuiceApplicationBuilder()
                .configure(ImmutableMap.<String, Object>builder()
                        .put("optional.string", "abc")
                        .put("optional.integer", 123)
                        .put("optional.boolean", true)
                        .build());
    }

    @Override
    protected Application provideApplication() {
        return builder.build();
    }

    // STRING CONFIGURATION VALUES

    @Test
    public void shouldInjectNonEmptyOptionalWhenStringValueDoesExist() {
        ContainerForNonEmptyStringOptionalInjection instance = builder.injector().instanceOf(ContainerForNonEmptyStringOptionalInjection.class);
        assertThat(instance.value, is(Optional.of("abc")));
    }

    private static class ContainerForNonEmptyStringOptionalInjection {

        private Optional<String> value;

        @Inject
        public ContainerForNonEmptyStringOptionalInjection(@ConfigurationValue(key = "optional.string") Optional<String> value) {
            this.value = value;
        }
    }

    @Test
    public void shouldInjectEmptyOptionalWhenStringValueDoesNotExist() {
        ContainerForEmptyStringOptionalInjection instance = builder.injector().instanceOf(ContainerForEmptyStringOptionalInjection.class);
        assertThat(instance.value, is(Optional.empty()));
    }

    private static class ContainerForEmptyStringOptionalInjection {

        private Optional<String> value;

        @Inject
        public ContainerForEmptyStringOptionalInjection(@ConfigurationValue(key = "unknown-value") Optional<String> value) {
            this.value = value;
        }
    }

    @Test
    public void shouldInjectStringWhenStringValueDoesExist() {
        ContainerForNonNullableStringInjection instance = builder.injector().instanceOf(ContainerForNonNullableStringInjection.class);
        assertThat(instance.value, is("abc"));
    }

    private static class ContainerForNonNullableStringInjection {

        private String value;

        @Inject
        public ContainerForNonNullableStringInjection(@ConfigurationValue(key = "optional.string") String value) {
            this.value = value;
        }
    }

    @Test
    public void shouldInjectNullWhenStringValueDoesNotExist() {
        ContainerForNullableStringInjection instance = builder.injector().instanceOf(ContainerForNullableStringInjection.class);
        assertThat(instance.value, is(nullValue()));
    }

    private static class ContainerForNullableStringInjection {

        private String value;

        @Inject
        public ContainerForNullableStringInjection(@ConfigurationValue(key = "unknown-value") @Nullable String value) {
            this.value = value;
        }
    }

    @Test(expected = Exception.class)
    public void shouldFailWhenStringValueDoesNotExist() {
        builder.injector().instanceOf(ContainerForInvalidStringInjection.class);
    }

    @SuppressWarnings("unused")
    private static class ContainerForInvalidStringInjection {

        @Inject
        public ContainerForInvalidStringInjection(@ConfigurationValue(key = "unknown-value") String value) {}
    }

    // INTEGER CONFIGURATION VALUES

    @Test
    public void shouldInjectNonEmptyOptionalWhenIntegerValueDoesExist() {

        ContainerForNonEmptyOptionalIntegerInjection instance = builder.injector().instanceOf(ContainerForNonEmptyOptionalIntegerInjection.class);
        assertThat(instance.value, is(Optional.of(123)));
    }

    private static class ContainerForNonEmptyOptionalIntegerInjection {

        private Optional<Integer> value;

        @Inject
        public ContainerForNonEmptyOptionalIntegerInjection(@ConfigurationValue(key = "optional.integer") Optional<Integer> value) {
            this.value = value;
        }
    }

    @Test
    public void shouldInjectEmptyOptionalWhenIntegerValueDoesNotExist() {

        ContainerForEmptyOptionalIntegerInjection instance = builder.injector().instanceOf(ContainerForEmptyOptionalIntegerInjection.class);
        assertThat(instance.value, is(Optional.empty()));
    }

    private static class ContainerForEmptyOptionalIntegerInjection {

        private Optional<Integer> value;

        @Inject
        public ContainerForEmptyOptionalIntegerInjection(@ConfigurationValue(key = "unknown-value") Optional<Integer> value) {
            this.value = value;
        }
    }

    @Test
    public void shouldInjectIntegerWhenIntegerValueDoesExist() {
        ContainerForNonNullableIntegerInjection instance = builder.injector().instanceOf(ContainerForNonNullableIntegerInjection.class);
        assertThat(instance.value, is(123));
    }

    private static class ContainerForNonNullableIntegerInjection {

        private Integer value;

        @Inject
        public ContainerForNonNullableIntegerInjection(@ConfigurationValue(key = "optional.integer") Integer value) {
            this.value = value;
        }
    }

    @Test
    public void shouldInjectNullWhenIntegerValueDoesNotExist() {
        ContainerForNullableIntegerInjection instance = builder.injector().instanceOf(ContainerForNullableIntegerInjection.class);
        assertThat(instance.value, is(nullValue()));
    }

    private static class ContainerForNullableIntegerInjection {

        private Integer value;

        @Inject
        public ContainerForNullableIntegerInjection(@ConfigurationValue(key = "unknown-value") @Nullable Integer value) {
            this.value = value;
        }
    }

    @Test(expected = Exception.class)
    public void shouldFailWhenIntegerValueDoesNotExist() {
        builder.injector().instanceOf(ContainerForInvalidIntegerInjection.class);
    }

    @SuppressWarnings("unused")
    private static class ContainerForInvalidIntegerInjection {

        @Inject
        public ContainerForInvalidIntegerInjection(@ConfigurationValue(key = "unknown-value") Integer value) {}
    }

    // BOOLEAN CONFIGURATION VALUES

    @Test
    public void shouldInjectNonEmptyOptionalWhenBooleanValueDoesExist() {

        ContainerForNonEmptyOptionalBooleanInjection instance = builder.injector().instanceOf(ContainerForNonEmptyOptionalBooleanInjection.class);
        assertThat(instance.value, is(Optional.of(true)));
    }

    private static class ContainerForNonEmptyOptionalBooleanInjection {

        private Optional<Boolean> value;

        @Inject
        public ContainerForNonEmptyOptionalBooleanInjection(@ConfigurationValue(key = "optional.boolean") Optional<Boolean> value) {
            this.value = value;
        }
    }

    @Test
    public void shouldInjectEmptyOptionalWhenBooleanValueDoesNotExist() {

        ContainerForEmptyOptionalBooleanInjection instance = builder.injector().instanceOf(ContainerForEmptyOptionalBooleanInjection.class);
        assertThat(instance.value, is(Optional.empty()));
    }

    private static class ContainerForEmptyOptionalBooleanInjection {

        private Optional<Boolean> value;

        @Inject
        public ContainerForEmptyOptionalBooleanInjection(@ConfigurationValue(key = "unknown-value") Optional<Boolean> value) {
            this.value = value;
        }
    }

    @Test
    public void shouldInjectBooleanWhenBooleanValueDoesExist() {
        ContainerForNonNullableBooleanInjection instance = builder.injector().instanceOf(ContainerForNonNullableBooleanInjection.class);
        assertThat(instance.value, is(true));
    }

    private static class ContainerForNonNullableBooleanInjection {

        private Boolean value;

        @Inject
        public ContainerForNonNullableBooleanInjection(@ConfigurationValue(key = "optional.boolean") Boolean value) {
            this.value = value;
        }
    }

    @Test
    public void shouldInjectNullWhenBooleanValueDoesNotExist() {
        ContainerForNullableBooleanInjection instance = builder.injector().instanceOf(ContainerForNullableBooleanInjection.class);
        assertThat(instance.value, is(nullValue()));
    }

    private static class ContainerForNullableBooleanInjection {

        private Boolean value;

        @Inject
        public ContainerForNullableBooleanInjection(@ConfigurationValue(key = "unknown-value") @Nullable Boolean value) {
            this.value = value;
        }
    }

    @Test(expected = Exception.class)
    public void shouldFailWhenBooleanValueDoesNotExist() {
        builder.injector().instanceOf(ContainerForInvalidBooleanInjection.class);
    }

    @SuppressWarnings("unused")
    private static class ContainerForInvalidBooleanInjection {

        @Inject
        public ContainerForInvalidBooleanInjection(@ConfigurationValue(key = "unknown-value") Boolean value) {}
    }

}