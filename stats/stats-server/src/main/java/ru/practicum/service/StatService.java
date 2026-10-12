package ru.practicum.service;

import ru.practicum.EndpointHit;
import ru.practicum.ParamDto;
import ru.practicum.ViewStats;

import java.util.List;

public interface StatService {

    void saveHit(EndpointHit dto);

    List<ViewStats> getStats(ParamDto paramDto);
}