package it.polito.ai.transportmodule.services;

import it.polito.ai.transportmodule.model.Route;

public interface RouteService {
	
	public Route findRoute(double[] startCoordinates, double[] arriveCoordinates, int radius);
	
}
