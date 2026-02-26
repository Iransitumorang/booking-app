package org.acme.config;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.UUID;

@Provider
public class UuidParamConverterProvider implements ParamConverterProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType != UUID.class) {
            return null;
        }
        return (ParamConverter<T>) new ParamConverter<UUID>() {
            @Override
            public UUID fromString(String value) {
                if (value == null || value.isBlank()) {
                    return null;
                }
                return UUID.fromString(value);
            }

            @Override
            public String toString(UUID value) {
                return value != null ? value.toString() : null;
            }
        };
    }
}
