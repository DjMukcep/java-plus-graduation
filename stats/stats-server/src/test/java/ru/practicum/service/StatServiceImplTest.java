package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.EndpointHit;
import ru.practicum.ParamDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EndpointHitEntity;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatServiceImplTest {

    @Mock
    private StatRepository statRepository;

    @InjectMocks
    private StatServiceImpl statService;

    private ParamDto paramDto;

    @BeforeEach
    void setUp() {
        paramDto = new ParamDto(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 2, 10, 0),
                List.of("/test"),
                false
        );
    }

    @Test
    void saveHit_shouldSaveMappedEntity() {

        EndpointHit dto = EndpointHit.builder()
                .app("app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp("2026-01-01 10:00:00")
                .build();

        when(statRepository.save(any(EndpointHitEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        statService.saveHit(dto);

        verify(statRepository).save(any(EndpointHitEntity.class));
    }

    @Test
    void getStats_shouldThrowException_whenEndBeforeStart() {

        paramDto.setEnd(paramDto.getStart().minusHours(1));

        assertThatThrownBy(() -> statService.getStats(paramDto))
                .isInstanceOf(ValidationException.class)
                .hasMessage("End time is before start time");

        verifyNoInteractions(statRepository);
    }

    @Test
    void getStats_shouldCallGetAllViewStats() {

        paramDto.setUris(null);
        paramDto.setUnique(false);

        when(statRepository.getAllViewStats(any(), any()))
                .thenReturn(List.of());

        statService.getStats(paramDto);

        verify(statRepository).getAllViewStats(
                paramDto.getStart(),
                paramDto.getEnd());

        verifyNoMoreInteractions(statRepository);
    }

    @Test
    void getStats_shouldCallGetAllUniqueViewStats() {

        paramDto.setUris(null);
        paramDto.setUnique(true);

        when(statRepository.getAllUniqueViewStats(any(), any()))
                .thenReturn(List.of());

        statService.getStats(paramDto);

        verify(statRepository).getAllUniqueViewStats(
                paramDto.getStart(),
                paramDto.getEnd());

        verifyNoMoreInteractions(statRepository);
    }

    @Test
    void getStats_shouldCallGetViewStatsByUris() {

        paramDto.setUnique(false);

        when(statRepository.getViewStatsByUris(any(), any(), any()))
                .thenReturn(List.of());

        statService.getStats(paramDto);

        verify(statRepository).getViewStatsByUris(
                paramDto.getUris(),
                paramDto.getStart(),
                paramDto.getEnd());

        verifyNoMoreInteractions(statRepository);
    }

    @Test
    void getStats_shouldCallGetUniqueViewStatsByUris() {

        paramDto.setUnique(true);

        when(statRepository.getUniqueViewStatsByUris(any(), any(), any()))
                .thenReturn(List.of());

        statService.getStats(paramDto);

        verify(statRepository).getUniqueViewStatsByUris(
                paramDto.getUris(),
                paramDto.getStart(),
                paramDto.getEnd());

        verifyNoMoreInteractions(statRepository);
    }

    @Test
    void getStats_shouldTreatEmptyUrisAsNull() {

        paramDto.setUris(List.of());

        when(statRepository.getAllViewStats(any(), any()))
                .thenReturn(List.of());

        statService.getStats(paramDto);

        verify(statRepository).getAllViewStats(
                paramDto.getStart(),
                paramDto.getEnd());

        verifyNoMoreInteractions(statRepository);
    }

}