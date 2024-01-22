package com.algotic.data.repositories;

import com.algotic.data.entities.Strategies;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategiesRepo extends JpaRepository<Strategies, Integer> {
    @Query(
            value = "Select * from Strategies s where s.ID =:id and s.IsActive =true and s.IsDeleted= false ",
            nativeQuery = true)
    Strategies findByIdAndIsActive(Integer id);

    Strategies findByNameAndIsActiveAndIsDeleted(String name, boolean isActive, boolean isDeleted);

    @Query(
            value =
                    "select * from Strategies s where s.IsActive=true and s.IsDeleted=false order by s.CreatedAt ASC limit :limit offset :offset",
            nativeQuery = true)
    List<Strategies> getStrategy(Integer limit, Integer offset);

    @Query(value = "select count(*) from Strategies s where s.IsActive=true and s.IsDeleted=false", nativeQuery = true)
    Integer allCount();

    @Query(
            value = "select * from Strategies s where s.IsActive=true and s.IsDeleted=false order by Name",
            nativeQuery = true)
    List<Strategies> getStrategyNames();

    Strategies findByIdAndIsDeleted(Integer id, boolean isDeleted);
}
