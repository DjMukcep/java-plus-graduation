package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.ParamDto;
import ru.practicum.ViewStats;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EndpointHitEntity;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {

    private final StatRepository statRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHit dto) {
        EndpointHitEntity entity = StatMapper.toHitEntity(dto);
        entity = statRepository.save(entity);
        log.info("New hit saved: {}", entity);
    }

    @Override
    public List<ViewStats> getStats(ParamDto paramDto) {
        LocalDateTime start = paramDto.getStart();
        LocalDateTime end = paramDto.getEnd();
        Collection<String> uris = (paramDto.getUris() != null && !paramDto.getUris().isEmpty())
                ? paramDto.getUris()
                : null;

        if (end.isBefore(start)) {
            throw new ValidationException("End time is before start time");
        }

        if (uris == null) {
            return paramDto.isUnique()
                    ? statRepository.getAllUniqueViewStats(start, end)
                    : statRepository.getAllViewStats(start, end);
        }

        return paramDto.isUnique()
                ? statRepository.getUniqueViewStatsByUris(uris, start, end)
                : statRepository.getViewStatsByUris(uris, start, end);
    }
}