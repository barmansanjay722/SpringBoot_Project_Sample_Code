package com.algotic.data.repositories;

import com.algotic.data.entities.Subscriptions;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionsRepo extends JpaRepository<Subscriptions, Integer> {
    @Query(
            value = "Select * from Subscriptions s where s.IsActive=true order by Name limit :limit  offset :offset ",
            nativeQuery = true)
    List<Subscriptions> findAllSubscription(Integer limit, Integer offset);

    Subscriptions findByIdAndIsActive(Integer id, boolean isActive);
}
