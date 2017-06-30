package it.polito.ai.transport.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import it.polito.ai.transport.model.jpa.BusStop;

public interface BusStopRepository extends JpaRepository<BusStop, String> {

}
