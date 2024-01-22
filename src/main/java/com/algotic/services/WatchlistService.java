package com.algotic.services;

import com.algotic.model.request.WatchList;
import com.algotic.model.response.InstrumentWatchlistResponse;
import com.algotic.model.response.WatchListRemoveResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface WatchlistService {
    ResponseEntity<List<InstrumentWatchlistResponse>> saveWatchlist(List<WatchList> instrumentWatchlistRequest);

    ResponseEntity<List<InstrumentWatchlistResponse>> getWatchlist();

    ResponseEntity<WatchListRemoveResponse> deleteWatchlist(List<Integer> watchlist);
}
