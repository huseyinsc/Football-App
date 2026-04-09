package com.huseyinsacikay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TYPES;

@TestConfiguration
public class TestJacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(REQUIRE_HANDLERS_FOR_JAVA8_TYPES, false);
        return mapper;
    }
}