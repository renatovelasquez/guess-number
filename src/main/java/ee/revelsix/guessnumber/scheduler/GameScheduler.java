package ee.revelsix.guessnumber.scheduler;

import ee.revelsix.guessnumber.service.GameService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class GameScheduler {

    private final GameService gameService;
    private final ThreadPoolTaskScheduler taskScheduler;

    public GameScheduler(GameService gameService, ThreadPoolTaskScheduler taskScheduler) {
        this.gameService = gameService;
        this.taskScheduler = taskScheduler;
    }

    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}", initialDelayString = "${fixedRate.in.milliseconds}")
    public void startNewRound() {
        taskScheduler.execute(gameService::newRound);
    }
}
