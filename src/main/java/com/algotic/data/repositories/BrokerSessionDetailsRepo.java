package com.algotic.data.repositories;

import com.algotic.data.entities.BrokerSessionDetails;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BrokerSessionDetailsRepo extends JpaRepository<BrokerSessionDetails, Integer> {

    @Query(
            value =
                    "select * from BrokerSessionDetails b where b.UserId=:userId and IsActive =true and Date(b.CreatedAt)=curdate() limit 1",
            nativeQuery = true)
    BrokerSessionDetails getSessionId(String userId);

    @Query(
            value =
                    "select * from BrokerSessionDetails b where b.BrokerCustomerDetailID=:brokerCusId and IsActive =true",
            nativeQuery = true)
    Optional<BrokerSessionDetails> getSessionIdByBrokerCusId(Integer brokerCusId);
}
