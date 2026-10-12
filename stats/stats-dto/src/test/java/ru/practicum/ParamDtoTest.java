package ru.practicum;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ParamDtoTest {

    private static Validator validator;

    private ParamDto paramDto;

    private final LocalDateTime start =
            LocalDateTime.of(2026, 1, 1, 10, 0, 0);

    private final LocalDateTime end =
            LocalDateTime.of(2026, 1, 2, 10, 0, 0);

    @BeforeAll
    static void init() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        validator = factory;
    }

    @BeforeEach
    void setUp() {
        paramDto = new ParamDto(
                start,
                end,
                List.of("/events"),
                true
        );
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        Set<ConstraintViolation<ParamDto>> violations = validator.validate(paramDto);

        assertThat(violations, empty());
    }

    @Test
    void whenStartIsNull_thenHasViolation() {
        paramDto.setStart(null);

        Set<ConstraintViolation<ParamDto>> violations = validator.validate(paramDto);

        assertThat(violations, hasSize(1));

        ConstraintViolation<ParamDto> violation = violations.iterator().next();

        assertThat(violation.getPropertyPath().toString(), is("start"));
    }

    @Test
    void whenEndIsNull_thenHasViolation() {
        paramDto.setEnd(null);

        Set<ConstraintViolation<ParamDto>> violations = validator.validate(paramDto);

        assertThat(violations, hasSize(1));

        ConstraintViolation<ParamDto> violation = violations.iterator().next();

        assertThat(violation.getPropertyPath().toString(), is("end"));
    }

    @Test
    void whenUniqueIsTrue_thenReturnsTrue() {
        paramDto.setUnique(true);

        assertThat(paramDto.isUnique(), is(true));
    }

    @Test
    void whenUniqueIsFalse_thenReturnsFalse() {
        paramDto.setUnique(false);

        assertThat(paramDto.isUnique(), is(false));
    }

    @Test
    void whenUniqueIsNull_thenReturnsFalse() {
        paramDto.setUnique(null);

        assertThat(paramDto.isUnique(), is(false));
    }

}