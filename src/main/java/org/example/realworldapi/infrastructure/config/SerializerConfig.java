package org.example.realworldapi.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.example.realworldapi.infrastructure.web.qualifiers.NoWrapRootValueObjectMapper;

@ApplicationScoped
public class SerializerConfig {

    @Singleton
    @Produces
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        objectMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Singleton
    @Produces
    @NoWrapRootValueObjectMapper
    public ObjectMapper noWrapRootValueObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

}
