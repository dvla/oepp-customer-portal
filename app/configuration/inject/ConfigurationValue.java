package configuration.inject;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@BindingAnnotation
@Target({FIELD, PARAMETER, CONSTRUCTOR, METHOD})
@Retention(RUNTIME)
public @interface ConfigurationValue {

    String key();

    boolean asResource() default false;

}