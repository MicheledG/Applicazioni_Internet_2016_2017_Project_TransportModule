package it.polito.ai.transportmodule.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.polito.ai.transportmodule.services.LinesService;
import it.polito.ai.transportmodule.services.RouteService;

@RestController
@CrossOrigin(origins = "*")
public class RouteController {
	
	@Autowired
	private RouteService routeService;
	@Autowired
	private LinesService linesService;
	
	@RequestMapping(value="/route", method=RequestMethod.GET)
	public String getRoute(){
		//routeService.findRoute(new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, 100);
		linesService.findStopsInRadius(new double[]{45.073810, 7.601631}, 100);
		return "hellone";
	}
}
