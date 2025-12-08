package org.example.matket.domain.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingNotificationService implements NotificationService {

    @Override
    public void notifyOwner(String message) {
        log.info("[NOTIFY_OWNER] {}", message);
    }
}
