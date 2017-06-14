package it.polito.ai.transportmodule.repositories.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import it.polito.ai.transportmodule.model.jpa.BusStopGeo;

public interface BusStopGeoRepository extends JpaRepository<BusStopGeo, String> {
	
	@Query(value
			= "select id "
			+ "from busstopgeo "
			+ "where ST_DWithin("
			+ "position, "
			+ "ST_GeographyFromText(?1), "
			+ "?2)", 
			nativeQuery=true
			)
	public List<String> findBusStopGeoInRadius(String textGeography, int radius);
	
}
