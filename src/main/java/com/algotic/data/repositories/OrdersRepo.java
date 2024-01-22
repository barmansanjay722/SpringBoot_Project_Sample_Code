package com.algotic.data.repositories;

import com.algotic.data.entities.Orders;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepo extends JpaRepository<Orders, Integer> {
    @Query(value = "select * from Orders o where o.TradeId =:tradeId", nativeQuery = true)
    List<Orders> getOrderDetails(String tradeId);
}
