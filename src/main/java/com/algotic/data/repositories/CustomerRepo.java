package com.algotic.data.repositories;

import com.algotic.data.entities.CustomerManagements;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepo extends JpaRepository<CustomerManagements, String> {
    @Procedure(value = "Sp_GetUserdata")
    List<CustomerManagements> getUsers(
            String name,
            String userId,
            String userType,
            Date lastActiveStartDate,
            Date lastActiveEndDate,
            Date renewalStartDate,
            Date renewalEndDate,
            String status,
            String paymentStatus,
            Integer limit,
            Integer offset);

    @Procedure(value = "Sp_GetUserCount")
    Object[] getUsersCount(
            String name,
            String userId,
            String userType,
            Date lastActiveStartDate,
            Date lastActiveEndDate,
            Date renewalStartDate,
            Date renewalEndDate,
            String status,
            String paymentStatus);
}
