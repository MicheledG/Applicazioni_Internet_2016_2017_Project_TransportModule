package it.polito.ai.transportmodule.controllers;

import java.util.ArrayList;
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
import it.polito.ai.transportmodule.model.RouteBusSegment;
import it.polito.ai.transportmodule.model.RouteBusSegmentPortion;
import it.polito.ai.transportmodule.model.RouteSegment;
import it.polito.ai.transportmodule.model.jpa.BusStop;
import it.polito.ai.transportmodule.resources.RouteBusDetailResource;
import it.polito.ai.transportmodule.resources.RouteDetailResource;
import it.polito.ai.transportmodule.resources.RouteFootDetailResource;
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
		routeResource.setFromCoordinates(fromCoordinates);
		routeResource.setToCoordinates(toCoordinates);
		routeResource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(RouteController.class)
						.getRoute(
								fromLat,
								fromLng,
								toLat,
								toLng,
								true,
								false)).withSelfRel());
		
		//we have to compute the route for sure
		Route route = routeService.findRoute(fromCoordinates, toCoordinates, RADIUS);
		if(route == null){
			//no information requested
			return new ResponseEntity<RouteResource>(HttpStatus.NOT_FOUND);
		}
		
		if(!detailsReq && !geoJsonReq){
			//no information requested
			return new ResponseEntity<RouteResource>(routeResource, HttpStatus.OK);
		}
		
		/*
		 * WARNING: a Route describes a MinPath stored into the DB.
		 * It does not contain details on the connection between the fromPoint and the first Route stop
		 * and between the lst Route stop and the toPoint!
		 */
		
		/*
		 * WARNING: the route segments in a Route object are sorted!
		 */
		
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
		
		int numberOfSegments = route.getSegments().size();
		List<RouteSegment> routeSegments = route.getSegments();
		List<BusStop> firstSegmentStops = routeSegments.get(0).getBusStops();
		List<BusStop> lastSegmentStops = routeSegments.get(numberOfSegments-1).getBusStops();
		BusStop firstRouteStop = firstSegmentStops.get(0);
		BusStop lastRouteStop = lastSegmentStops.get(lastSegmentStops.size()-1);
		
		List<RouteDetailResource> details = new ArrayList<>();
		int detailSequenceNumber = 1;
		
		//create the first detail: fromPoint to first stop in the route
		RouteFootDetailResource firstDetail = new RouteFootDetailResource();
		String firstDetailFromField = "Lat: "+route.getStartCoordinates()[0]+" - Lng: "+route.getStartCoordinates()[1];
		String firstDetailToField = firstRouteStop.getId() + " - " + firstRouteStop.getName();
		firstDetail.setSequenceNumber(detailSequenceNumber);
		firstDetail.setFrom(firstDetailFromField);
		firstDetail.setTo(firstDetailToField);
		firstDetail.setLength(200); //TODO => RETRIEVE THE REAL LENGTH
		detailSequenceNumber++;
		details.add(firstDetail);
		
		//create all the others details
		for (RouteSegment routeSegment: routeSegments) {
			if(routeSegment instanceof RouteBusSegment){
				//it is a segment by bus!
				RouteBusSegment routeBusSegment = (RouteBusSegment) routeSegment;
				for (RouteBusSegmentPortion routeBusSegmentPortion: routeBusSegment.getRouteBusSegmentPortions()) {
					//extract the needed information from the RouteBusSegmentPortion
					String line = routeBusSegmentPortion.getLineId();
					List<BusStop> busStops = routeBusSegmentPortion.getBusStops();
					int numberOfStops = busStops.size();
					BusStop firstBusStop = busStops.get(0);
					BusStop lastBusStop = busStops.get(numberOfStops-1);
					String from = firstBusStop.getId() + " - " + firstBusStop.getName();
					String to = lastBusStop.getId() + " - " + lastBusStop.getName();
					
					//create the RouteBusDetailResource for each RouteBusSegmentPortion
					RouteBusDetailResource routeBusDetailResource = new RouteBusDetailResource();
					routeBusDetailResource.setLine(line);
					routeBusDetailResource.setFrom(from);
					routeBusDetailResource.setTo(to);
					routeBusDetailResource.setStops(numberOfStops-1); //numberOfStops - 1 == numberOfEdges!
					routeBusDetailResource.setSequenceNumber(detailSequenceNumber);
					detailSequenceNumber++;
					
					//add to the details
					details.add((RouteDetailResource) routeBusDetailResource);
				}
			}
			else{
				//it is a segment on foot!
				//extract the needed information from the RouteSegment
				List<BusStop> busStops = routeSegment.getBusStops();
				int numberOfStops = busStops.size();
				BusStop firstBusStop = busStops.get(0);
				BusStop lastBusStop = busStops.get(numberOfStops-1);
				String from = firstBusStop.getId() + " - " + firstBusStop.getName();
				String to = lastBusStop.getId() + " - " + lastBusStop.getName();
				
				//create the RouteBusDetailResource for each RouteBusSegmentPortion
				RouteFootDetailResource routeFootDetailResource = new RouteFootDetailResource();
				routeFootDetailResource.setFrom(from);
				routeFootDetailResource.setTo(to);
				routeFootDetailResource.setLength(200); //TODO => RETRIEVE REAL LENGTH
				routeFootDetailResource.setSequenceNumber(detailSequenceNumber);
				detailSequenceNumber++;
				
				//add to the details
				details.add((RouteDetailResource) routeFootDetailResource);
			}
		}
		
		//create the last detail: last stop in the route to toPoint
		RouteFootDetailResource lastDetail = new RouteFootDetailResource();
		String lastDetailFromField = lastRouteStop.getId() + " - " + lastRouteStop.getName();
		String lastDetailToField = "Lat: "+route.getArriveCoordinates()[0]+" - Lng: "+route.getArriveCoordinates()[1];
		lastDetail.setSequenceNumber(detailSequenceNumber); //TODO => RETRIEVE THE REAL SEQUENCE NUMBER
		lastDetail.setFrom(lastDetailFromField);
		lastDetail.setTo(lastDetailToField);
		lastDetail.setLength(200); //TODO => RETRIEVE THE REAL LENGTH
		details.add(lastDetail);
		
		return details;
	}
	
	private GeoJson createRouteGeoJson(Route route){
		//TODO
		return null;
	}
	
}
