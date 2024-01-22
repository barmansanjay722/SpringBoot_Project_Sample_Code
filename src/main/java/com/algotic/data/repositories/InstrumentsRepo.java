package com.algotic.data.repositories;

import com.algotic.data.entities.Instruments;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentsRepo extends JpaRepository<Instruments, Integer> {

    @Query(
            value = "Select * from Instruments where TradingSymbol like %:search% or FormatedInsName like %:search%",
            nativeQuery = true)
    List<Instruments> getInstruments(String search);
}
