package com.algotic.data.repositories;

import com.algotic.data.entities.StrategiesManagements;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategiesManagementRepo extends JpaRepository<StrategiesManagements, String> {
    @Procedure(value = "Sp_StrategiesData")
    List<StrategiesManagements> getStrategies(
            String strategyName, Date from, Date to, String strategyStatus, Integer limit, Integer offset);

    @Procedure(value = "Sp_StrategiesCount")
    Object[] getStrategiesCount(String name, Date from, Date to, String status);
}
