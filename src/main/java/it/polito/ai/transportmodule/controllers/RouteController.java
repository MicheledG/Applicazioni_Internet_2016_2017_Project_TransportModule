package it.polito.ai.transportmodule.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.polito.ai.transportmodule.model.Route;
import it.polito.ai.transportmodule.resources.RouteDetailResource;
import it.polito.ai.transportmodule.resources.RouteResource;
import it.polito.ai.transportmodule.resources.geojson.GeoJson;
import it.polito.ai.transportmodule.services.RouteService;

@RestController
@CrossOrigin(origins = "*")
public class RouteController {
	
	private static final int RADIUS = 200;
	
	@Autowired
	private RouteService routeService;
	
	@RequestMapping(value="/route", method=RequestMethod.GET)
	public HttpEntity<RouteResource> getRoute(
			@RequestParam(required=true) double fromLat,
			@RequestParam(required=true) double fromLng,
			@RequestParam(required=true) double toLat,
			@RequestParam(required=true) double toLng,
			@RequestParam(value="details", required=false, defaultValue="false") boolean detailsReq,
			@RequestParam(value="geoJson", required=false, defaultValue="false") boolean geoJsonReq){
		
		double[] fromCoordinates = new double[2];
		fromCoordinates[0] = fromLat;
		fromCoordinates[1] = fromLng;
		
		double[] toCoordinates = new double[2];
		toCoordinates[0] = toLat;
		toCoordinates[1] = toLng;
		
		RouteResource routeResource = new RouteResource();
		routeResource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(RouteController.class)
						.getRoute(
								fromLat,
								fromLng,
								toLat,
								toLng,
								true,
								false)).withSelfRel());
		
		if(!detailsReq && !geoJsonReq){
			//no information requested
			return new ResponseEntity<RouteResource>(routeResource, HttpStatus.OK);
		}
		
		//we have to compute the route for sure
		Route route = routeService.findRoute(fromCoordinates, toCoordinates, RADIUS);
		
		if(detailsReq){
			List<RouteDetailResource> details = this.createRouteDetails(route);
			routeResource.setDetails(details);
		}
		
		if(geoJsonReq){
			GeoJson geoJson = this.createRouteGeoJson(route);
			routeResource.setGeoJson(geoJson);
		}
		
		return new ResponseEntity<RouteResource>(routeResource, HttpStatus.OK);
	}
	
	private List<RouteDetailResource> createRouteDetails(Route route){
		//TODO
		return null;
	}
	
	private GeoJson createRouteGeoJson(Route route){
		//TODO
		return null;
	}
	
}
