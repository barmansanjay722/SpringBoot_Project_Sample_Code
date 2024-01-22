package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.model.request.WatchList;
import com.algotic.model.response.InstrumentWatchlistResponse;
import com.algotic.model.response.WatchListRemoveResponse;
import com.algotic.services.WatchlistService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class WatchListController {
    @Autowired
    private WatchlistService watchlistService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @CrossOrigin
    @PostMapping("/watchList")
    public ResponseEntity<List<InstrumentWatchlistResponse>> saveWatchlist(@RequestBody List<WatchList> instruments) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Save instrument watchlist",
                        "start-> saving the instrument watchlist ",
                        jwtHelper.getUserId(),
                        null));
        return watchlistService.saveWatchlist(instruments);
    }

    @CrossOrigin
    @GetMapping("/watchList")
    public ResponseEntity<List<InstrumentWatchlistResponse>> getWatchlist() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("get instrument watchlist", "start-> get watchlist", jwtHelper.getUserId(), null));
        return watchlistService.getWatchlist();
    }

    @CrossOrigin
    @PostMapping("/watchList/remove")
    public ResponseEntity<WatchListRemoveResponse> deleteWatchlist(@RequestBody List<Integer> watchListIds) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("remove watchlist", "start-> remove watchlist", jwtHelper.getUserId(), null));
        return watchlistService.deleteWatchlist(watchListIds);
    }
}
