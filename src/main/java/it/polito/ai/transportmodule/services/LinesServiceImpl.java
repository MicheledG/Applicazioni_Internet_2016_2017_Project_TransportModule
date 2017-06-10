package it.polito.ai.transportmodule.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.polito.ai.transportmodule.model.jpa.BusLine;
import it.polito.ai.transportmodule.model.jpa.BusLineStop;
import it.polito.ai.transportmodule.model.jpa.BusStop;
import it.polito.ai.transportmodule.repositories.jpa.BusLineRepository;
import it.polito.ai.transportmodule.repositories.jpa.BusStopRepository;

@Service
@Transactional
public class LinesServiceImpl implements LinesService {
	
	@Autowired
	private BusLineRepository busLineRepository;
	@Autowired
	private BusStopRepository BusStopRepository;
	
	@Override
	public BusLine getBusLine(String lineId) {
		BusLine busLine = busLineRepository.getOne(lineId);
		if(busLine == null){
			return null;
		}
		//needs to do this because lazy evaluation
		busLine.getLineStops().size();
		return busLine;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getBusStopNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BusStop> getBusStops() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BusStop getBusStopOfBusLine(String lineId, int sequenceNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BusLineStop> getBusLineStopsOfBusLine(String lineId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BusLine> getBusLinesOfBusStop(String stopId) {
		BusStop busStop = BusStopRepository.getOne(stopId);
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

}
