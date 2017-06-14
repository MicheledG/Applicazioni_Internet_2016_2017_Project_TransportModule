package it.polito.ai.transportmodule.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.polito.ai.transportmodule.model.Route;
import it.polito.ai.transportmodule.resources.geojson.GeoJson;
import it.polito.ai.transportmodule.services.RouteService;

@RestController
@CrossOrigin(origins = "*")
public class RouteController {
	
	@Autowired
	private RouteService routeService;
	
	@RequestMapping(value="/route", method=RequestMethod.GET)
	public Route getRoute(){
		Route route = routeService.findRoute(new double[]{45.073810, 7.601631}, new double[]{45.030794, 7.666670}, 100);
		return route;
	}
	
	private GeoJson createRouteGeoJson(Route route){
		//TODO
		return null;
	}
	
}
