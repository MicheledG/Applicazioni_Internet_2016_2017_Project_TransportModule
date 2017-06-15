package it.polito.ai.transportmodule.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.polito.ai.transportmodule.model.Route;
import it.polito.ai.transportmodule.model.RouteBusSegment;
import it.polito.ai.transportmodule.model.RouteBusSegmentPortion;
import it.polito.ai.transportmodule.model.RouteSegment;
import it.polito.ai.transportmodule.model.TakenBus;
import it.polito.ai.transportmodule.model.jpa.BusStop;
import it.polito.ai.transportmodule.model.mongo.Edge;
import it.polito.ai.transportmodule.model.mongo.MinPath;
import it.polito.ai.transportmodule.repositories.mongo.MinPathRepository;

@Service
public class RouteServiceImpl implements RouteService {

	@Autowired
	private LinesService linesService;
	@Autowired
	private MinPathRepository minPathRepository;
	
	private static final int FOOT_COEFF = 1; //m/s
	
	@Override
	public Route findRoute(double[] startCoordinates, double[] arriveCoordinates, int radius) {

		//find the minimum paths that connect starting point to arrive point
		List<MinPath> minPaths = findMinimumPaths(startCoordinates, arriveCoordinates, radius);
		
		if(minPaths == null){
			//no path between start and arrive point
			return null;
		}
		
		//select the cheaper minimum path
		MinPath bestPath = selectBestPath(minPaths, startCoordinates, arriveCoordinates);
		
		if(bestPath == null){
			//no path between start and arrive point
			return null;
		}
		
		//determine the segments of the route based on the best path
		List<RouteSegment> routeSegments;
		routeSegments = computeRouteSegments(bestPath);
		
		Route route = new Route();
		route.setSegments(routeSegments);
		route.setStartCoordinates(startCoordinates);
		route.setArriveCoordinates(arriveCoordinates);
		return route;
		
	}

	private List<RouteSegment> computeRouteSegments(MinPath bestPath) {
		
		//define how many segment there are on the route
		List<RouteSegment> routeSegments = defineRouteSegments(bestPath);
		
		if(routeSegments == null)
			return null;
		
		//compute RouteBusSegmentPortions for each RouteBusSegment
		computeRouteBusSegmentsPortions(routeSegments);
		
		return routeSegments;
	}

	private void computeRouteBusSegmentsPortions(List<RouteSegment> routeSegments) {
		
		for (RouteSegment routeSegment : routeSegments) {
			if(routeSegment instanceof RouteBusSegment){
				//compute all the portions of a RouteBusSegment
				computeRouteBusSegmentPortions((RouteBusSegment) routeSegment);
			}
		}
		
	}

	private void computeRouteBusSegmentPortions(RouteBusSegment routeBusSegment) {
		
		//retrieve all the possible lines linking two stops
		List<List<String>> listOfLinesForPortions = new ArrayList<List<String>>();
		for (int i = 0; i < routeBusSegment.getBusStops().size() -1; i++) {
			List<String> listOfLines = linesService
					.findLinesConnectingStops(routeBusSegment.getBusStops().get(i), routeBusSegment.getBusStops().get(i+1));
			if(listOfLines == null){
				//TODO
			}
			else{
				listOfLinesForPortions.add(listOfLines);
			}
		}
		
		//find the best combination of lines to cover the entire RouteBusSegment defining 
		//a RouteBusSegmentPortion for each different line used to cover the route bus segment
		List<String> bestCombination = computeBestCombinationOfLines(listOfLinesForPortions);
		
		//from the best combination extract all the RouteBusSegmentPortions
		int edgeIndex = 0;
		for(String lineId: bestCombination){
			Edge edge = routeBusSegment.getEdges().get(edgeIndex);
			RouteBusSegmentPortion previousRouteBusSegmentPortion = null;
			if(routeBusSegment.getRouteBusSegmentPortions().size()!=0){
				previousRouteBusSegmentPortion = routeBusSegment.getRouteBusSegmentPortions().get(routeBusSegment.getRouteBusSegmentPortions().size()-1);
			}
			
			if(previousRouteBusSegmentPortion== null || !(previousRouteBusSegmentPortion.getLineId().equals(lineId))){
				//a new RouteBusSegmentPortion starts
				RouteBusSegmentPortion routeBusSegmentPortion = new RouteBusSegmentPortion();
				routeBusSegmentPortion.setLineId(lineId);
				routeBusSegmentPortion.getEdges().add(edge);
				routeBusSegmentPortion.getBusStops().add(linesService.getBusStop(edge.getIdSource()));
				routeBusSegmentPortion.getBusStops().add(linesService.getBusStop(edge.getIdDestination()));
				routeBusSegment.getRouteBusSegmentPortions().add(routeBusSegmentPortion);
			}
			else {
				//the previous RouteBusSegmentPortion is on the same LineId so update it adding a new edge to the portion
				previousRouteBusSegmentPortion.getEdges().add(edge);
				previousRouteBusSegmentPortion.getBusStops().add(linesService.getBusStop(edge.getIdDestination()));
			}
			edgeIndex++;
		}
		
	}

	private List<String> computeBestCombinationOfLines(List<List<String>> listOfLinesForPortions) {
		//idea: take a bus and stay on it for the major number of stops!
		List<String> bestCombination = new ArrayList<String>();
		
		int portionIndex = 0;
		int numberOfPortions = listOfLinesForPortions.size();
		while(portionIndex < numberOfPortions){
			TakenBus takenBus = takeBus(listOfLinesForPortions, portionIndex);
			String lineId = takenBus.getLineId();
			int coveredPortions = takenBus.getNumberOfCoveredPortions();
			for(int j = portionIndex; j < portionIndex + coveredPortions; j++){
				bestCombination.add(lineId);
			}
			portionIndex += coveredPortions;
		}
		
		return bestCombination;
	}
	
	private TakenBus takeBus(List<List<String>> listOfLinesForPortions, int portionIndex) {
		//idea: take a bus and stay on it for the major number of stops!
		List<TakenBus> takenBuses = new ArrayList<TakenBus>();
		
		for(String lineId: listOfLinesForPortions.get(portionIndex)){
			//each line considered right here covers the first portion!
			TakenBus takenBus = new TakenBus();
			takenBus.setLineId(lineId);
			takenBus.setNumberOfCoveredPortions(1);
			for(int i=portionIndex+1; i < listOfLinesForPortions.size(); i++){
				if(listOfLinesForPortions.get(i).contains(lineId)){
					//in the list of the lines covering this portion there is also the line covering the preceding portion
					int coveredPortions = takenBus.getNumberOfCoveredPortions() + 1;
					takenBus.setNumberOfCoveredPortions(coveredPortions);
				}
				else{
					//this line has no other portions to cover!
					break;
				}
			}
			takenBuses.add(takenBus);
		}
		
		//chose the bus which covers the major number of portions
		Collections.sort(takenBuses, Collections.reverseOrder());
		return takenBuses.get(0);
	}

	void generateCombinations(List<List<String>> listOfLinesForPortions, List<List<String>> combinations, int depth, List<String> currentCombination)
	{
	    if(depth == listOfLinesForPortions.size())
	    {
	       combinations.add(currentCombination);
	       return;
	     }

	    for(int i = 0; i < listOfLinesForPortions.get(depth).size(); ++i)
	    {
	        if(i > 0)
	        	continue;
	    	
	    	
	    	List<String> updatedCombination;
	    	if(currentCombination != null){
	    		updatedCombination = new ArrayList<String>(currentCombination);
	    	}
	    	else{
	    		updatedCombination = new ArrayList<String>();
	    	}
	    	String lineId = listOfLinesForPortions.get(depth).get(i);
	        updatedCombination.add(lineId);
	    	generateCombinations(listOfLinesForPortions, combinations, depth + 1, updatedCombination);
	    }
	}
	
	/*
	 * A RouteSegment is an homogeneous segment of the MinPath
	 * that is a set of contiguous edges all of the same type (foot or bus)
	 */
	private List<RouteSegment> defineRouteSegments(MinPath path) {
		
		List<RouteSegment> routeSegments = new ArrayList<RouteSegment>();
		Collections.sort(path.getEdges());
		
		for (Edge edge : path.getEdges()) {
			
			RouteSegment previousRouteSegment = null;
			if(routeSegments.size()!=0){
				previousRouteSegment = routeSegments.get(routeSegments.size()-1);
			}
			
			if(edge.isMode()){
				//on foot edge
				if(previousRouteSegment == null || previousRouteSegment instanceof RouteBusSegment){
					//a new RouteSegment starts
					RouteSegment routeSegment = new RouteSegment();
					routeSegment.getEdges().add(edge);
					routeSegment.getBusStops().add(linesService.getBusStop(edge.getIdSource()));
					routeSegment.getBusStops().add(linesService.getBusStop(edge.getIdDestination()));
					routeSegments.add(routeSegment);
				}
				else {
					//the previous segment is a RouteSegment so simply update it adding this new edge
					previousRouteSegment.getEdges().add(edge);
					previousRouteSegment.getBusStops().add(linesService.getBusStop(edge.getIdDestination()));
				}
			}
			else{
				//by bus edge
				if(previousRouteSegment == null || !(previousRouteSegment instanceof RouteBusSegment)){
					//a new RouteBusSegment starts
					RouteBusSegment routeBusSegment = new RouteBusSegment();
					routeBusSegment.getEdges().add(edge);
					routeBusSegment.getBusStops().add(linesService.getBusStop(edge.getIdSource()));
					routeBusSegment.getBusStops().add(linesService.getBusStop(edge.getIdDestination()));
					routeSegments.add(routeBusSegment);
				}
				else {
					//the previous segment is a RouteBusSegment so simply update it adding this new edge
					previousRouteSegment.getEdges().add(edge);
					previousRouteSegment.getBusStops().add(linesService.getBusStop(edge.getIdDestination()));
				}
			}
		}
		
		return routeSegments;
	}

	private MinPath selectBestPath(List<MinPath> minPaths, double[] startCoordinates, double[] arriveCoordinates) {
		
		for (MinPath minPath : minPaths) {
			String firstStop = minPath.getIdSource();
			String lastStop = minPath.getIdDestination();
			
			double startPointToMinPathDistance = linesService.getDistanceFromBusStop(startCoordinates, firstStop);
			double stopPointtoMinPathDistance = linesService.getDistanceFromBusStop(arriveCoordinates, lastStop);
			
			int overheadCost = ((Double) ((startPointToMinPathDistance + stopPointtoMinPathDistance) / FOOT_COEFF)).intValue();
			 
			int initialPathCost = minPath.getTotalCost();
			minPath.setTotalCost(initialPathCost + overheadCost);
		}
		
		
		Collections.sort(minPaths);
		return minPaths.get(0);
	}

	private List<MinPath> findMinimumPaths(double[] startCoordinates, double[] arriveCoordinates, int radius) {
		
		//find bus stop within the radius of the start point
		List<BusStop> startStops = linesService.findStopsInRadius(startCoordinates, radius);
		if(startStops == null){
			return null;
		}
		List<BusStop> arriveStops = linesService.findStopsInRadius(arriveCoordinates, radius);
		if(arriveStops == null){
			return null;
		}
		
		//find the minimum path linking the start and arrive stops
		List<MinPath> minPaths = new ArrayList<MinPath>();
		for (BusStop startStop : startStops) {
			for (BusStop arriveStop : arriveStops) {
				MinPath minPath = minPathRepository.findOneByIdSourceAndIdDestination(startStop.getId(), arriveStop.getId());
				if(minPath != null){
					minPaths.add(minPath);
				}
			}
		}
		if(minPaths.size() == 0)
			return null;
		else
			return minPaths;
	}

}
