package com.algotic.utils;

import com.algotic.data.entities.PaperOrders;
import com.algotic.data.repositories.PaperOrderRepo;
import com.algotic.services.PaperOrderService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SchedulerUtils {
    @Autowired
    private PaperOrderService paperOrderService;

    @Autowired
    private PaperOrderRepo paperOrderRepo;

    @Scheduled(cron = "0 10 15 * * MON-FRI")
    public void scheduler() throws InterruptedException {
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDateTime = currentDate.format(formatter);
        List<String> userData = paperOrderRepo.findUserid();
        if (!userData.isEmpty()) {
            for (String userId : userData) {
                try {
                    paperOrderService.paperOrderScheduler(userId);
                    log.info("Scheduler time" + formattedDateTime);
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }

    @Scheduled(cron = "0 00 22 * * MON-FRI")
    public void schedulerHoldings() throws InterruptedException {
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDateTime = currentDate.format(formatter);
        List<String> userData = paperOrderRepo.findHoldingUserid();
        if (!userData.isEmpty()) {
            for (String userId : userData) {
                paperOrderService.paperOrderHoldingScheduler(userId);
                log.info("Scheduler time" + formattedDateTime);
            }
        }
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void schedulerExpiry() throws InterruptedException {
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDateTime = currentDate.format(formatter);
        List<PaperOrders> paperData = paperOrderRepo.instrumentExpiryDate();
        if (!paperData.isEmpty()) {
            for (PaperOrders orders : paperData) {
                try {
                    paperOrderService.paperOrderInstrumentScheduler(orders);
                    log.info("Scheduler time" + formattedDateTime);
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }
}
