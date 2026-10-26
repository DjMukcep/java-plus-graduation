package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.practicum.service.EventsSimilarityProcessor;
import ru.practicum.service.UserActionProcessor;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements ApplicationRunner {
    final EventsSimilarityProcessor similarityProcessor;
    final UserActionProcessor userActionProcessor;

    @Override
    public void run(ApplicationArguments args) {
        Thread userActionProcessorThread = new Thread(userActionProcessor);
        userActionProcessorThread.setName("userActionProcessorThread");
        userActionProcessorThread.start();
        similarityProcessor.start();
    }
}
