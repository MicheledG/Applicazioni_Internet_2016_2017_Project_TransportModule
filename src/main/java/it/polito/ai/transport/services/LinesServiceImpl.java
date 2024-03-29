package it.polito.ai.transport.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.polito.ai.transport.model.jpa.BusLine;
import it.polito.ai.transport.model.jpa.BusLineStop;
import it.polito.ai.transport.model.jpa.BusStop;
import it.polito.ai.transport.repositories.jpa.BusLineRepository;
import it.polito.ai.transport.repositories.jpa.BusStopGeoRepository;
import it.polito.ai.transport.repositories.jpa.BusStopRepository;

@Service
@Transactional
public class LinesServiceImpl implements LinesService {
	
	@Autowired
	private BusLineRepository busLineRepository;
	@Autowired
	private BusStopRepository busStopRepository;
	@Autowired
	private BusStopGeoRepository busStopGeoRepository;
	@Autowired
	private GeographyHelper geographyHelper;
	
	@Override
	public BusLine getBusLine(String lineId) {
		BusLine busLine = busLineRepository.getOne(lineId);
		try{
			//needs to do this because lazy evaluation
			busLine.getLineStops().size();
			return busLine;
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	@Override
	public List<String> getBusLineNames() {
		List<BusLine> lines = busLineRepository.findAll();
		List<String> lineNames = new ArrayList<>();
		for (BusLine busLine : lines) {
			String lineName = busLine.getLine();
			lineNames.add(lineName);
		}
		return lineNames;
	}

	@Override
	public List<BusLine> getBusLines() {
		List<BusLine> lines = busLineRepository.findAll();
		//needs to do this because lazy evaluation
		for (BusLine busLine : lines) {
			busLine.getLineStops().size();
		}
		return lines;
	}

	@Override
	public BusStop getBusStop(String stopId) {
		BusStop busStop = busStopRepository.getOne(stopId);
		try {
			//needs to do this because lazy evaluation
			busStop.getStoppingLines().size();
			return busStop;
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	@Override
	public List<BusStop> getBusStops() {
		List<BusStop> busStops = busStopRepository.findAll();
		for (BusStop busStop : busStops) {
			busStop.getStoppingLines().size();
		}
		return busStops;
	}

	@Override
	public BusStop getBusStopOfBusLine(String lineId, int sequenceNumber) {
		List<BusLineStop> busLineStops = this.getBusLineStopsOfBusLine(lineId);
		if(busLineStops == null){
			return null;
		}
		
		Collections.sort(busLineStops);
		int busStopIndex = sequenceNumber - 1;
		BusLineStop busLineStop = busLineStops.get(busStopIndex);
		if(busLineStop == null){
			return null;
		}
		
		BusStop busStop = busLineStop.getBusStop();
		return busStop;
	}

	@Override
	public List<BusLineStop> getBusLineStopsOfBusLine(String lineId) {
		BusLine busLine = this.getBusLine(lineId);
		if(busLine == null){
			return null;
		}
		
		return busLine.getLineStops();
	}

	@Override
	public List<BusLine> getBusLinesOfBusStop(String stopId) {
		BusStop busStop = this.getBusStop(stopId);
		if(busStop == null){
			return null;
		}
		//needs to filter the line names which appear more than once
		Map<String, BusLine> filteredStoppingLines = new HashMap<>();
		for (BusLineStop stoppingLine : busStop.getStoppingLines()) {
			BusLine line = stoppingLine.getBusLine();
			String lineName = line.getLine();
			if(!filteredStoppingLines.containsKey(line)){
				filteredStoppingLines.put(lineName, line);
			}
		}
		
		List<BusLine> filteredStoppingLinesList = new ArrayList<>(filteredStoppingLines.values());
		return filteredStoppingLinesList;
	}

	@Override
	public double getDistanceFromBusStop(double[] startCoordinates, String firstStop) {
		
		double lat = startCoordinates[0];
		double lng = startCoordinates[1];
		String textGeography = geographyHelper.createTextGeographyPoint(lat, lng);
		
		double distance = busStopGeoRepository.computeDistanceBetweenPointAndBusStop(textGeography, firstStop);
		
		return distance;
	
	}

	@Override
	public List<BusStop> findStopsInRadius(double[] startCoordinates, int radius) {
		
		double lat = startCoordinates[0];
		double lng = startCoordinates[1];
		String textGeography = geographyHelper.createTextGeographyPoint(lat, lng);
		
		List<String> stopsInRadius= busStopGeoRepository.findBusStopGeoInRadius(textGeography, radius);
		
		if(stopsInRadius == null || stopsInRadius.size() == 0){
			return null;
		}
		
		List<BusStop> busStops = new ArrayList<>();
		for (String busStopId : stopsInRadius) {
			BusStop busStop = busStopRepository.getOne(busStopId);
			busStops.add(busStop);
		}
		
		return busStops;
	}

	@Override
	public List<String> findLinesConnectingStops(BusStop busStopA, BusStop busStopB) {
		
		List<BusLine> stoppingLinesA = this.getBusLinesOfBusStop(busStopA.getId());
		if(stoppingLinesA == null || stoppingLinesA.size() == 0){
			return null;
		}
		
		Set<String> stoppingLinesB = new HashSet<>();
		List<BusLine> stoppingLinesBList = this.getBusLinesOfBusStop(busStopB.getId());
		for (BusLine busLine : stoppingLinesBList) {
			String lineName = busLine.getLine();
			stoppingLinesB.add(lineName);
		}
		
		List<String> connectingLines = new ArrayList<>();
		for (BusLine busLine : stoppingLinesA) {
			String lineName = busLine.getLine();
			if(stoppingLinesB.contains(lineName)){
				connectingLines.add(lineName);
			}
		}
		
		return connectingLines;
	}

}
