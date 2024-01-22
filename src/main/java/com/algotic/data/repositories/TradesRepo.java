package com.algotic.data.repositories;

import com.algotic.data.entities.Trades;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TradesRepo extends JpaRepository<Trades, Integer> {
    @Query(
            value =
                    "select * from Trades where UserId=:userId and IsDeleted=false order by CreatedAt desc limit :limit offset :offset ",
            nativeQuery = true)
    List<Trades> findAllTrades(String userId, Integer limit, Integer offset);

    @Query(value = "select Count(1) from Trades where UserId=:userId and IsDeleted=false ", nativeQuery = true)
    Integer getAllTradesCount(String userId);

    @Query(value = "select * from Trades t where t.ID=:tradeId and t.IsDeleted= false", nativeQuery = true)
    Trades findTradeById(String tradeId);

    @Query(
            value = "select * from Trades t where t.ID=:tradeId and t.IsActive=true and t.IsDeleted= false",
            nativeQuery = true)
    Trades findTradePresentAndActive(String tradeId);

    @Query(value = "select Count(1) from Trades where StrategyID=:strategyId", nativeQuery = true)
    Integer findCountForStrategy(Integer strategyId);

    @Query(value = " select count(Distinct(UserId)) from Trades where StrategyID=:strategyId", nativeQuery = true)
    Integer findCount(Integer strategyId);
}
