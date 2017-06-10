package it.polito.ai.transportmodule.services;

import java.util.List;

import it.polito.ai.transportmodule.model.jpa.BusLine;
import it.polito.ai.transportmodule.model.jpa.BusLineStop;
import it.polito.ai.transportmodule.model.jpa.BusStop;

public interface LinesService {
	
	public List<String> getBusLineNames();
	public List<BusLine> getBusLines();
	public BusLine getBusLine(String lineId);
	public List<String> getBusStopNames();
	public List<BusStop> getBusStops();
	public BusStop getBusStop(String stopId);
	public BusStop getBusStopOfBusLine(String lineId, int sequenceNumber);
	public List<BusLineStop> getBusLineStopsOfBusLine(String lineId);
	public List<BusLine> getBusLinesOfBusStop(String stopId);
	
}
