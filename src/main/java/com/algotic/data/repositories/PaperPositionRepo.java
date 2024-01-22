package com.algotic.data.repositories;

import com.algotic.data.entities.PaperPositions;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperPositionRepo extends JpaRepository<PaperPositions, Integer> {
    @Procedure(value = "sp_GetPaperPositions")
    List<PaperPositions> getPaperPosition(String userId);
}
