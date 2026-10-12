package ru.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class EndpointHitTest {

    private static Validator validator;
    private EndpointHit endpointHit;


    @BeforeAll
    static void init() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        validator = factory;
    }

    @BeforeEach
    void setUp() {
        endpointHit = EndpointHit.builder()
                .id(1L)
                .app("testApp")
                .uri("/test/uri")
                .ip("192.168.0.1")
                .timestamp("2026-01-01 10:20:30")
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"app", "uri", "ip", "timestamp"})
    void whenFieldIsBlank_thenViolation(String field) {
        setField(field, " ");

        ConstraintViolation<EndpointHit> violation = getSingleViolation();

        assertThat(violation.getPropertyPath().toString(), is(field));
    }

    @ParameterizedTest
    @ValueSource(strings = {"app", "uri", "ip", "timestamp"})
    void whenFieldIsNull_thenViolation(String field) {
        setField(field, null);

        ConstraintViolation<EndpointHit> violation = getSingleViolation();

        assertThat(violation.getPropertyPath().toString(), is(field));
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, empty());
    }

    private ConstraintViolation<EndpointHit> getSingleViolation() {
        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, hasSize(1));

        return violations.iterator().next();
    }

    private void setField(String field, String value) {
        switch (field) {
            case "app" -> endpointHit.setApp(value);
            case "uri" -> endpointHit.setUri(value);
            case "ip" -> endpointHit.setIp(value);
            case "timestamp" -> endpointHit.setTimestamp(value);
        }
    }
}