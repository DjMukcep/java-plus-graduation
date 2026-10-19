package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.event.EventRequestsCountDto;
import ru.practicum.entity.Request;
import ru.practicum.dto.request.RequestStatus;

import java.util.List;
import java.util.Set;

public interface RequestRepository extends JpaRepository<Request, Long> {

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByRequesterId(Long userId);

    List<Request> findByIdIn(Set<Long> ids);

    List<Request> findByIdInAndEventId(Set<Long> ids, Long eventId);

    List<Request> findByEventId(Long eventId);


    @Query("SELECT new ru.practicum.dto.event.EventRequestsCountDto(r.eventId, COUNT(r.id)) " +
            "FROM Request r " +
            "WHERE r.eventId IN :ids AND r.status = :status " +
            "GROUP BY r.eventId")
    List<EventRequestsCountDto> countByEventIdInAndStatus(@Param("ids") Set<Long> ids,
                                                          @Param("status") RequestStatus status);

    @Modifying
    @Query("UPDATE Request r SET r.status = :status WHERE r.id IN :ids")
    void updateStatusByIds(@Param("ids") Set<Long> ids,
                           @Param("status") RequestStatus status);
}
