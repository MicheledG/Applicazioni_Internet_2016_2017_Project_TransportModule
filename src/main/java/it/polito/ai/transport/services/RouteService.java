package it.polito.ai.transport.services;

import it.polito.ai.transport.model.Route;

public interface RouteService {
	
	public Route findRoute(double[] startCoordinates, double[] arriveCoordinates, int radius);
	
}
