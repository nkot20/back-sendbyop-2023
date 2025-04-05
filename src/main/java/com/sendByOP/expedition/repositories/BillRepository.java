package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends JpaRepository<Invoice, Integer> {
}
