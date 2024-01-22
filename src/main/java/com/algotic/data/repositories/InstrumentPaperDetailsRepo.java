package com.algotic.data.repositories;

import com.algotic.data.entities.InstrumentPaperDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentPaperDetailsRepo extends JpaRepository<InstrumentPaperDetails, Integer> {}
