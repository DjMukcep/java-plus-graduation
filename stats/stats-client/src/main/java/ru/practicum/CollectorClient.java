package ru.practicum;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;

import static ru.practicum.ewm.stats.proto.UserActionControllerGrpc.UserActionControllerBlockingStub;

@Component
@Slf4j
public class CollectorClient {

    private final UserActionControllerBlockingStub client;

    public CollectorClient(@GrpcClient("collector") UserActionControllerBlockingStub client) {
        this.client = client;
    }

    public void collectUserAction(Long userId, Long eventId, String action) {
        if (userId == null || eventId == null || action == null) {
            log.error("Неверные аргументы в: userId - {} или eventId - {} или action  - {}", userId, eventId, action);
            return;
        }

        ActionTypeProto actionType = ActionTypeProto.valueOf(action.toUpperCase());

        if (actionType != ActionTypeProto.ACTION_LIKE
                && actionType != ActionTypeProto.ACTION_REGISTER
                && actionType != ActionTypeProto.ACTION_VIEW) {
            log.error("Невозможно сопоставить команду: action - {} для внесения данных на сервис коллектор.", action);
            return;
        }
        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .build();
        try {
            client.collectUserAction(request);
            log.info("Данные успешно внесены на сервис коллектор: {}", request);
        } catch (StatusRuntimeException e) {
            log.error("Ошибка gRPC при внесении новых данных на сервис коллектор: {} - {}", request, e.getMessage());
        }
    }
}
