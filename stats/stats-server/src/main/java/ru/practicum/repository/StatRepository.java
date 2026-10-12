package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ViewStats;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatRepository extends JpaRepository<EndpointHitEntity, Long> {

    @Query("select new ru.practicum.ViewStats(h.app, h.uri, count(h)) " +
            "from EndpointHitEntity h " +
            "where h.uri in :uris " +
            "and h.timestamp >= :start and h.timestamp <= :end " +
            "group by h.app, h.uri " +
            "order by count(h) desc")
    List<ViewStats> getViewStatsByUris(@Param("uris") Collection<String> uris,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.ViewStats(h.app, h.uri, count(distinct h.ip)) " +
            "from EndpointHitEntity h " +
            "where h.uri in :uris " +
            "and h.timestamp >= :start and h.timestamp <= :end " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")

    List<ViewStats> getUniqueViewStatsByUris(@Param("uris") Collection<String> uris,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);


    @Query("select new ru.practicum.ViewStats(h.app, h.uri, count(h)) " +
            "from EndpointHitEntity h " +
            "where h.timestamp >= :start and h.timestamp <= :end " +
            "group by h.app, h.uri " +
            "order by count(h) desc")
    List<ViewStats> getAllViewStats(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.ViewStats(h.app, h.uri, count(distinct h.ip)) " +
            "from EndpointHitEntity h " +
            "where h.timestamp >= :start and h.timestamp <= :end " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<ViewStats> getAllUniqueViewStats(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}