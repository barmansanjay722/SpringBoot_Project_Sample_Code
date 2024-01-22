package com.algotic.data.repositories;

import com.algotic.data.entities.AccessTokens;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTokensRepo extends JpaRepository<AccessTokens, Integer> {
    @Query(value = "select * from AccessTokens a where  a.UserId =:userId and IsActive =true", nativeQuery = true)
    List<AccessTokens> findByUserId(String userId);

    @Query(value = "select * from AccessTokens a where a.Token =:token and IsActive =true", nativeQuery = true)
    AccessTokens getToken(String token);

    @Query(
            value = "select count(Distinct(at.UserId)) from AccessTokens at where Date(at.CreatedAt)=current_date()",
            nativeQuery = true)
    Integer getUsersCount();
}
