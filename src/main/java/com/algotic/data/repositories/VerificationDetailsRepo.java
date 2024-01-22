package com.algotic.data.repositories;

import com.algotic.data.entities.VerificationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationDetailsRepo extends JpaRepository<VerificationDetails, Integer> {
    VerificationDetails findByVerificationCode(String verificationCode);

    @Query(
            value =
                    "Select * from VerificationDetails v where v.ResourceType='Email' and v.UserId=:userId Order by v.CreatedAt Desc limit 1",
            nativeQuery = true)
    VerificationDetails findByUserId(String userId);
}
