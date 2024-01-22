package com.algotic.data.repositories;

import com.algotic.data.entities.PaperHoldingDetails;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;

public interface PaperHoldingDetailsRepo extends JpaRepository<PaperHoldingDetails, Integer> {
    @Procedure(value = "sp_HandlePaperHoldings")
    void getHoldingsData(String userId);

    @Query(
            value =
                    "select DISTINCT(po.UserId ) from PaperHoldingDetails po where  po.TransactionType=:transactionType and UserId=:userId ",
            nativeQuery = true)
    List<String> findByUserIdAndPdc(String transactionType, String userId);

    @Query(
            value =
                    "select * from PaperHoldingDetails po where po.TradingSymbol=:tradingSymbol and po.Token=:token and po.UserId=:userId order by CreatedAt desc ",
            nativeQuery = true)
    PaperHoldingDetails findHoldingData(String tradingSymbol, String token, String userId);

    @Query(
            value =
                    "select * from PaperHoldingDetails po where   po.TransactionType='BUY' and UserId=:userId  group by TradingSymbol",
            nativeQuery = true)
    List<PaperHoldingDetails> findByUserIdList(String userId);
}
