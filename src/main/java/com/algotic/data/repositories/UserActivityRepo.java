package com.algotic.data.repositories;

import com.algotic.data.entities.UserActivities;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityRepo extends JpaRepository<UserActivities, String> {
    @Procedure(value = "Sp_GetUserLoginActivity")
    List<UserActivities> getUserActivity(Integer days);
}
