package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(components = StatClient.class, properties = "client.url=http://localhost:9090")
@Import(RestConfig.class)
class StatClientTest {

    @SpringBootApplication
    @EnableAutoConfiguration
    static class TestConfig {
    }

    @Autowired
    private StatClient statClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockRestServiceServer mockServer;

    private static final String BASE_URL = "http://localhost:9090";

    @Test
    @SneakyThrows
    @DisplayName("Успешная отправка запроса на сервер.")
    void hit_shouldSendPostRequest() {
        EndpointHit endpointHit = new EndpointHit(
                1L,
                "ewm-main",
                "/events/1",
                "192.168.1.1",
                "2026-06-18 10:00:00");

        mockServer.expect(requestTo(BASE_URL + "/hit"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(endpointHit)))
                .andRespond(withStatus(HttpStatus.CREATED));

        statClient.hit(endpointHit);

        mockServer.verify();
    }

    @Test
    @SneakyThrows
    @DisplayName("Успешный запрос статистики с сервера.")
    void getViewStats_shouldReturnStatsList() {
        StatsRequest request = new StatsRequest(
                "2020-05-05 00:00:00",
                "2035-05-05 00:00:00",
                List.of("/events/1"),
                true
        );

        List<ViewStats> expectedStats = List.of(new ViewStats("ewm-main","/events/1",5L));

        mockServer.expect(requestTo(startsWith(BASE_URL + "/stats")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("start", request.getStartTime().replace(" ", "%20")))
                .andExpect(queryParam("end", request.getEndTime().replace(" ", "%20")))
                .andExpect(queryParam("uris",request.getUris().getFirst()))
                .andExpect(queryParam("unique","true"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedStats), MediaType.APPLICATION_JSON));

        List<ViewStats> response = statClient.getViewStats(request);

        assertThat(response, hasSize(1));
        assertThat(response.getFirst().getApp(), equalTo("ewm-main"));
        assertThat(response.getFirst().getHits(), equalTo(5L));

        mockServer.verify();
    }

    @Test
    @DisplayName("Вернуть пустой список в случае срабатывания исключения.")
    void getViewStats_shouldReturnEmptyListWhenExceptionIsThrown() {
        StatsRequest request = new StatsRequest(
                "2020-05-05 00:00:00",
                "2035-05-05 00:00:00",
                List.of("/events/1"),
                true
        );

        mockServer.expect(requestTo(startsWith(BASE_URL + "/stats")))
                .andRespond(withServerError());

        List<ViewStats> response = statClient.getViewStats(request);

        assertThat(response, empty());

        mockServer.verify();
    }
}