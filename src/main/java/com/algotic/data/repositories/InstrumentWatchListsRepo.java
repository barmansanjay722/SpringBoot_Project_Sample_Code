package com.algotic.data.repositories;

import com.algotic.data.entities.InstrumentWatchLists;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentWatchListsRepo extends JpaRepository<InstrumentWatchLists, Integer> {
    List<InstrumentWatchLists> findByUserId(String userId);

    @Query(
            value =
                    "select * from InstrumentWatchlist i where i.UserId=:userId and (i.Expiry >= CURDATE() or i.Expiry IS NULL) and i.IsActive =true  order by i.CreatedAt desc",
            nativeQuery = true)
    List<InstrumentWatchLists> getAllInstrumentData(String userId);

    @Query(
            value =
                    "select * from InstrumentWatchlist i where i.UserId=:userId and i.Token=:token and i.IsActive =true",
            nativeQuery = true)
    InstrumentWatchLists tokenbyUserId(String userId, String token);

    @Query(
            value = "select * from InstrumentWatchlist i where i.InstrumentTradingSymbol=:tradingSymbol limit 1",
            nativeQuery = true)
    InstrumentWatchLists getInstrumentName(String tradingSymbol);
}
