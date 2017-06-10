package it.polito.ai.transportmodule.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import it.polito.ai.transportmodule.model.jpa.BusLine;

public interface BusLineRepository extends JpaRepository<BusLine, String> {
}
