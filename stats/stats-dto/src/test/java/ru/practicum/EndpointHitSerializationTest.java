package ru.practicum;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EndpointHitSerializationTest {

    private static ObjectMapper objectMapper;

    private static final long ID = 10;
    private static final String APP = "some-app";
    private static final String IP = "109.11.31.32";
    private static final String URI = "/api/uri";
    private static final LocalDateTime TIMESTAMP =
            LocalDateTime.of(2000, 3, 12, 13, 31, 14);
    private static final String TIMESTAMP_STRING = "2000-03-12 13:31:14";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSerialize() throws Exception {
        EndpointHit endpointHit = EndpointHit.builder()
                .id(ID)
                .app(APP)
                .uri(URI)
                .ip(IP)
                .timestamp(TIMESTAMP.format(formatter))
                .build();

        String json = objectMapper.writeValueAsString(endpointHit);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("id").asLong(), equalTo(ID));
        assertThat(node.get("app").asText(), equalTo(APP));
        assertThat(node.get("uri").asText(), equalTo(URI));
        assertThat(node.get("ip").asText(), equalTo(IP));
        assertThat(node.get("timestamp").asText(), equalTo(TIMESTAMP_STRING));
    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"id\":10," +
                "\"app\":\"some-app\"," +
                "\"ip\":\"109.11.31.32\"," +
                "\"uri\":\"/api/uri\"," +
                "\"timestamp\":\"" + TIMESTAMP_STRING + "\"}";

        EndpointHit dto = objectMapper.readValue(json, EndpointHit.class);

        assertThat(dto.getId(), equalTo(null));
        assertThat(dto.getApp(), equalTo(APP));
        assertThat(dto.getIp(), equalTo(IP));
        assertThat(dto.getUri(), equalTo(URI));
        assertThat(dto.getTimestamp(), equalTo(TIMESTAMP.format(formatter)));
    }

}
