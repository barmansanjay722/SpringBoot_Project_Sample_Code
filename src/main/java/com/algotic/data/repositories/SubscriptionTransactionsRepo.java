package com.algotic.data.repositories;

import com.algotic.data.entities.SubscriptionTransactions;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionTransactionsRepo extends JpaRepository<SubscriptionTransactions, Integer> {
    @Query(
            value = "Select * from SubscriptionTransaction s where s.UserId =:id  limit :limit  offset :offset",
            nativeQuery = true)
    List<SubscriptionTransactions> findByUserID(String id, Integer limit, Integer offset);

    @Query(
            value = "Select * from SubscriptionTransaction s where s.UserId =:userId and "
                    + "s.Status='Success' Order By s.CreatedAt Desc limit 1",
            nativeQuery = true)
    SubscriptionTransactions getSubscription(String userId);

    @Query(value = "select * from SubscriptionTransaction s where s.ID =:id and  s.UserId =:userId", nativeQuery = true)
    SubscriptionTransactions getTransactionYear(String userId, Integer id);

    @Query(value = "SELECT SUM(Amount) from SubscriptionTransaction s where s.Status='Success'", nativeQuery = true)
    Double totalRevenue();

    @Query(value = "SELECT count(*) from SubscriptionTransaction s where s.Status='Success'", nativeQuery = true)
    Integer countOfAllSubscribers();

    @Query(
            value = "Select * from SubscriptionTransaction s where s.UserId =:userId and "
                    + "s.Status='Success' Order By s.CreatedAt Desc",
            nativeQuery = true)
    List<SubscriptionTransactions> getHistory(String userId);

    @Query(
            value = "Select * from SubscriptionTransaction s where s.UserId =:userId and " + "s.Status='CREATED'",
            nativeQuery = true)
    SubscriptionTransactions paymentSubscriptionTransactionId(String userId);

    @Query(
            value = "Select * from SubscriptionTransaction s where s.Status='Created' and UserId=:userId ",
            nativeQuery = true)
    SubscriptionTransactions findByStatus(String userId);
}
