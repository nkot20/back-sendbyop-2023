package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Integer> {
}
