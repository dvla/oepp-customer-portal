package configuration.inject;

import org.apache.commons.lang3.AnnotationUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConfigurationValueImpl implements ConfigurationValue, Serializable {

    private final String key;
    private final boolean asResource;

    public ConfigurationValueImpl(String key) {
        this(key, false);
    }

    public ConfigurationValueImpl(String key, boolean asResource) {
        this.key = checkNotNull(key, "key");
        this.asResource = asResource;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public boolean asResource() {
        return asResource;
    }

    @Override
    public int hashCode() {
        return AnnotationUtils.hashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationValueImpl that = (ConfigurationValueImpl) o;
        return AnnotationUtils.equals(this, that);
    }

    public String toString() {
        return "@" + ConfigurationValue.class.getName() + "(key=" + key + ", asResource=" + asResource + ")";
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ConfigurationValue.class;
    }

    private static final long serialVersionUID = 0;
}
