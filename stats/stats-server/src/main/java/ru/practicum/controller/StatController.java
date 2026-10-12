package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.EndpointHit;
import ru.practicum.ParamDto;
import ru.practicum.ViewStats;
import ru.practicum.service.StatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody @Valid EndpointHit endpointHit) {
        statService.saveHit(endpointHit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getViewStats(@ModelAttribute @Valid ParamDto paramDto) {
        return statService.getStats(paramDto);
    }
}
