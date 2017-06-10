package it.polito.ai.transportmodule.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import it.polito.ai.transportmodule.model.jpa.BusStop;

public interface BusStopRepository extends JpaRepository<BusStop, String> {

}
