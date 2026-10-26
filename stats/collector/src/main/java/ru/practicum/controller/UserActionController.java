package ru.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.config.KafkaTopicsProperties;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc.UserActionControllerImplBase;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.service.KafkaActionSender;

import java.time.Instant;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerImplBase {

    private final KafkaActionSender actionSender;
    private final KafkaTopicsProperties topics;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            UserActionAvro message = UserActionAvro.newBuilder()
                    .setUserId(request.getUserId())
                    .setEventId(request.getEventId())
                    .setActionType(ActionTypeAvro.valueOf(request.getActionType().name()))
                    .setTimestamp(Instant.ofEpochSecond(
                            request.getTimestamp().getSeconds(),
                            request.getTimestamp().getNanos()))
                    .build();
            actionSender.sendMessage(
                    topics.userActions(),
                    request.getUserId(),
                    Instant.ofEpochSecond(
                            request.getTimestamp().getSeconds(),
                            request.getTimestamp().getNanos()),
                    message
            );

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Failed to collect user action")
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

}
