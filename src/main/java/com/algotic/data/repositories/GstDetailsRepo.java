package com.algotic.data.repositories;

import com.algotic.data.entities.GSTDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GstDetailsRepo extends JpaRepository<GSTDetails, String> {
    @Query(value = "select * from GSTDetails g where g.UserId=:userId limit 1", nativeQuery = true)
    GSTDetails findByUserId(String userId);
}
