package com.algotic.data.repositories;

import com.algotic.data.entities.Otps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpsRepo extends JpaRepository<Otps, Integer> {

    @Query(value = "select * from Otps o where o.ReferenceCode =:refCode", nativeQuery = true)
    Otps findOtpByRefCodeWithoutOtp(String refCode);

    @Query(value = "select * from Otps o where o.ReferenceCode=:refCode limit 1", nativeQuery = true)
    Otps findByRefCode(String refCode);
}
