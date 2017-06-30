package it.polito.ai.transport.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import it.polito.ai.transport.model.jpa.BusLine;

public interface BusLineRepository extends JpaRepository<BusLine, String> {
}
