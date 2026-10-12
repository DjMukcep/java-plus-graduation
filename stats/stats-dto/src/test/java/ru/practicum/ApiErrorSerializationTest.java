package ru.practicum;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ApiErrorSerializationTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSerialize() throws Exception {
        ApiError apiError = ApiError.builder()
                .errors(List.of("some errors"))
                .message("some message")
                .reason("some reason")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp("2000-03-12 13:31:14")
                .build();

        String json = objectMapper.writeValueAsString(apiError);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("message").asText(), equalTo("some message"));
        assertThat(node.get("reason").asText(), equalTo("some reason"));
        assertThat(node.get("status").asText(), equalTo(HttpStatus.BAD_REQUEST.name()));
        assertThat(node.get("timestamp").asText(), equalTo("2000-03-12 13:31:14"));
        assertThat(node.get("errors").get(0).asText(), equalTo("some errors"));
    }
}
