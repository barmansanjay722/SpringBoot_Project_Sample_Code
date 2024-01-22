package com.algotic.base;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class LogHandlerConfiguration {
    public LogHandler getLogHandler() {
        return new LogHandler();
    }
}
