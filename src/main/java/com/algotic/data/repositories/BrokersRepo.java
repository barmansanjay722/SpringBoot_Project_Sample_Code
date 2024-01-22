package com.algotic.data.repositories;

import com.algotic.data.entities.Brokers;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BrokersRepo extends JpaRepository<Brokers, Integer> {

    @Query(value = "select * from Brokers  order by Name limit :limit  offset :offset ", nativeQuery = true)
    List<Brokers> findAllBrokers(Integer limit, Integer offset);

    @Query(value = "select * from Brokers  where ID=:id ", nativeQuery = true)
    Brokers findBrokerName(Integer id);

    @Query(
            value =
                    "select Name from Brokers where ID = (select BrokerID from  BrokerCustomerDetails b where b.UserId = :userId and IsActive =true and Date(b.CreatedAt)=curdate() order by CreatedAt desc limit 1)",
            nativeQuery = true)
    String findBrokerNameByCustomerId(String userId);

    @Query(value = "select ID from Brokers where Name=:brokerName", nativeQuery = true)
    Integer findbrokerId(String brokerName);
}
