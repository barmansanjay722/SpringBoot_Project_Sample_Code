package com.algotic.data.repositories;

import com.algotic.data.entities.TradeExecutions;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeExecutionRepo extends JpaRepository<TradeExecutions, Long> {
    @Procedure(value = "Sp_TradesExecutionReport")
    List<TradeExecutions> tradeExecution(Integer limit, Integer offset);

    @Procedure(value = "Sp_TradesExecutionCount")
    Object[] getTradesExecutionCount();
}
