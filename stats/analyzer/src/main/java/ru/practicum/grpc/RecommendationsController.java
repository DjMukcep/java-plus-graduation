package ru.practicum.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.service.RecommendationService;

import java.util.HashSet;
import java.util.Set;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends
        RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;


    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            recommendationService.getRecommendations(
                            request.getUserId(),
                            request.getMaxResults()
                    )
                    .forEach(event -> responseObserver.onNext(
                            RecommendedEventProto.newBuilder()
                                    .setEventId(event.eventId())
                                    .setScore(event.score())
                                    .build()
                    ));

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Failed to get recommendations")
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            recommendationService.getSimilarEvents(
                    request.getEventId(),
                    request.getUserId(),
                    request.getMaxResults()
            ).forEach(similarity -> {
                Long similarEventId = similarity.getEventA().equals(request.getEventId())
                        ? similarity.getEventB()
                        : similarity.getEventA();

                responseObserver.onNext(
                        RecommendedEventProto.newBuilder()
                                .setEventId(similarEventId)
                                .setScore(similarity.getSimilarity())
                                .build()
                );
            });

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Failed to get similar events")
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            Set<Long> eventIds = new HashSet<>(request.getEventIdList());

            recommendationService.getInteractionsCount(eventIds)
                    .forEach((eventId, count) -> responseObserver.onNext(
                            RecommendedEventProto.newBuilder()
                                    .setEventId(eventId)
                                    .setScore(count)
                                    .build()
                    ));

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Failed to get interactions count")
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }
}
