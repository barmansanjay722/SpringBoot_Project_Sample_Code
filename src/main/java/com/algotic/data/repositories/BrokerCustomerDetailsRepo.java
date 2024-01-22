package com.algotic.data.repositories;

import com.algotic.data.entities.BrokerCustomerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BrokerCustomerDetailsRepo extends JpaRepository<BrokerCustomerDetails, Integer> {
    @Query(
            value =
                    "select * from  BrokerCustomerDetails b where b.UserId =:userId and IsActive =true and Date(b.CreatedAt)=curdate() order by CreatedAt desc limit 1",
            nativeQuery = true)
    BrokerCustomerDetails findBrokerUserById(String userId);

    @Query(value = "select * from  BrokerCustomerDetails b where b.UserId =:userId  limit 1", nativeQuery = true)
    BrokerCustomerDetails findBrokerCustomer(String userId);
}
