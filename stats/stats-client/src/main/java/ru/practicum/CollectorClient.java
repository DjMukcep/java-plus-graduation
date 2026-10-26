package ru.practicum;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;


@Component
@Slf4j
@RequiredArgsConstructor
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;


    public void collectUserAction(Long userId, Long eventId, ActionTypeProto actionType) {
        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .build();
        try {
            client.collectUserAction(request);
            log.info("Данные успешно внесены на сервис коллектор: userId={}, eventId={}, action={}",
                    request.getUserId(), request.getEventId(), request.getActionType());
        } catch (StatusRuntimeException e) {
            log.error("Ошибка gRPC при внесении новых данных на сервис коллектор: {} - {}", request, e.getMessage());
        }
    }
}
