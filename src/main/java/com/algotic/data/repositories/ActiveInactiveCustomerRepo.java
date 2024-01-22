package com.algotic.data.repositories;

import com.algotic.data.entities.InActiveUsers;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveInactiveCustomerRepo extends JpaRepository<InActiveUsers, String> {
    @Procedure(value = "Sp_GetInactiveUsers")
    List<InActiveUsers> getInactiveUsers();
}
