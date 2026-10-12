package ru.practicum;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ViewStatsSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerialization() throws Exception {
        ViewStats dto = new ViewStats("app", "/api/uri", 10L);

        JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(dto));

        assertThat(node.get("app").asText(), equalTo("app"));
        assertThat(node.get("uri").asText(), equalTo("/api/uri"));
        assertThat(node.get("hits").asLong(), equalTo(10L));
    }
}
