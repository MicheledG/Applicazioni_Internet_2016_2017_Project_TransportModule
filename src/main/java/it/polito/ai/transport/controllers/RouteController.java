package it.polito.ai.transport.controllers;

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

import it.polito.ai.transport.model.Route;
import it.polito.ai.transport.model.RouteBusSegment;
import it.polito.ai.transport.model.RouteBusSegmentPortion;
import it.polito.ai.transport.model.RouteSegment;
import it.polito.ai.transport.model.jpa.BusStop;
import it.polito.ai.transport.resources.RouteBusDetailResource;
import it.polito.ai.transport.resources.RouteDetailResource;
import it.polito.ai.transport.resources.RouteFootDetailResource;
import it.polito.ai.transport.resources.RouteResource;
import it.polito.ai.transport.resources.geojson.FeatureGeneric;
import it.polito.ai.transport.resources.geojson.GeoJson;
import it.polito.ai.transport.resources.geojson.GeometryLineString;
import it.polito.ai.transport.resources.geojson.GeometryPoint;
import it.polito.ai.transport.resources.geojson.route.FeatureRouteLineString;
import it.polito.ai.transport.resources.geojson.route.FeatureRoutePoint;
import it.polito.ai.transport.resources.geojson.route.PropertiesRouteLineStringBus;
import it.polito.ai.transport.resources.geojson.route.PropertiesRouteLineStringFoot;
import it.polito.ai.transport.resources.geojson.route.PropertiesRoutePoint;
import it.polito.ai.transport.services.LinesService;
import it.polito.ai.transport.services.RouteService;

@RestController
@CrossOrigin(origins = "*")
public class RouteController {
	
	private static final int RADIUS = 200;
	
	@Autowired
	private RouteService routeService;
	@Autowired
	private LinesService linesService;
	
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
				
		/*
		 * WARNING: a Route describes a MinPath stored into the DB.
		 * It does not contain details on the connection between the fromPoint and the first Route stop
		 * and between the last Route stop and the toPoint!
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
		
		/*
		 * WARNING: the route segments in a Route object are sorted!
		 */
		
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
		int firstDetailLengthField = ((Double) linesService.getDistanceFromBusStop(route.getStartCoordinates(), firstRouteStop.getId())).intValue();
		firstDetail.setLength(firstDetailLengthField); 
		detailSequenceNumber++;
		details.add(firstDetail);
		
		//create all the others details
		for (RouteSegment routeSegment: routeSegments) {
			if(routeSegment instanceof RouteBusSegment){
				//it is a segment by bus!
				RouteBusSegment routeBusSegment = (RouteBusSegment) routeSegment;
				for (RouteBusSegmentPortion routeBusSegmentPortion: routeBusSegment.getRouteBusSegmentPortions()) {
					
					//create the RouteBusDetailResource for each RouteBusSegmentPortion
					RouteBusDetailResource routeBusDetailResource = createRouteBusDetailResource(routeBusSegmentPortion, detailSequenceNumber);
					
					//add to the details
					details.add((RouteDetailResource) routeBusDetailResource);
					detailSequenceNumber++;
				}
			}
			else{
				//it is a segment on foot!	
				//create the RouteBusDetailResource for each RouteBusSegmentPortion
				RouteFootDetailResource routeFootDetailResource = createRouteFootDetailResource(routeSegment, detailSequenceNumber);
				
				//add to the details
				details.add((RouteDetailResource) routeFootDetailResource);
				detailSequenceNumber++;
			}
		}
		
		//create the last detail: last stop in the route to toPoint
		RouteFootDetailResource lastDetail = new RouteFootDetailResource();
		String lastDetailFromField = lastRouteStop.getId() + " - " + lastRouteStop.getName();
		String lastDetailToField = "Lat: "+route.getArriveCoordinates()[0]+" - Lng: "+route.getArriveCoordinates()[1];
		lastDetail.setSequenceNumber(detailSequenceNumber);
		lastDetail.setFrom(lastDetailFromField);
		lastDetail.setTo(lastDetailToField);
		int lastDetailLengthField = ((Double) linesService.getDistanceFromBusStop(route.getArriveCoordinates(), lastRouteStop.getId())).intValue(); 
		lastDetail.setLength(lastDetailLengthField);
		details.add(lastDetail);
		
		return details;
	}
	
	private GeoJson createRouteGeoJson(Route route){
		
		/*
		 * WARNING: the route segments in a Route object are sorted!
		 */
		
		
		GeoJson geoJson = new GeoJson();
		
		/*
		 * 1) create the Point Features
		 */
		
		//fromPoint feature
		PropertiesRoutePoint fromPointProperties = new PropertiesRoutePoint();
		fromPointProperties.setSequenceNumber(1);
		GeometryPoint fromPointGemetry = new GeometryPoint();
		fromPointGemetry.getCoordinates().add(route.getStartCoordinates()[1]); //WARNING: LNG <-> LAT!
		fromPointGemetry.getCoordinates().add(route.getStartCoordinates()[0]); //WARNING: LNG <-> LAT!
		FeatureRoutePoint fromPointFeature = new FeatureRoutePoint();
		fromPointFeature.setGeometry(fromPointGemetry);
		fromPointFeature.setProperties(fromPointProperties);
		geoJson.getFeatures().add((FeatureGeneric) fromPointFeature);
		
		//toPoint feature
		PropertiesRoutePoint toPointProperties = new PropertiesRoutePoint();
		toPointProperties.setSequenceNumber(2);
		GeometryPoint toPointGemetry = new GeometryPoint();
		toPointGemetry.getCoordinates().add(route.getArriveCoordinates()[1]); //WARNING: LNG <-> LAT!
		toPointGemetry.getCoordinates().add(route.getArriveCoordinates()[0]); //WARNING: LNG <-> LAT!
		FeatureRoutePoint toPointFeature = new FeatureRoutePoint();
		toPointFeature.setGeometry(toPointGemetry);
		toPointFeature.setProperties(toPointProperties);
		geoJson.getFeatures().add((FeatureGeneric) toPointFeature);
		
		/*
		 * 2) create the LineString Features
		 */
		
		//create the fromPoint to first stop linestring
		FeatureRouteLineString firstFeatureRouteLineString = createFirstFootFeatureRouteLineString(route);
		//add to the GeoJson features
		geoJson.getFeatures().add((FeatureGeneric) firstFeatureRouteLineString);
		
		//create the lastStop to the toPoint linestring
		FeatureRouteLineString lastFeatureRouteLineString = createLastFootFeatureRouteLineString(route);
		//add to the GeoJson features
		geoJson.getFeatures().add((FeatureGeneric) lastFeatureRouteLineString);
		
		//create all the other linestrings
		for (RouteSegment routeSegment: route.getSegments()) {
			if(routeSegment instanceof RouteBusSegment){
				//it is a segment by bus!
				RouteBusSegment routeBusSegment = (RouteBusSegment) routeSegment;
				for (RouteBusSegmentPortion routeBusSegmentPortion: routeBusSegment.getRouteBusSegmentPortions()) {
					//create the route line string feature
					FeatureRouteLineString featureRouteLineString = createBusFeatureRouteLineString(routeBusSegmentPortion);
					//add to the GeoJson features
					geoJson.getFeatures().add((FeatureGeneric) featureRouteLineString);
				}
			}
			else{
				//it is a segment on foot!
				//create the route line string feature
				FeatureRouteLineString featureRouteLineString = createFootFeatureRouteLineString(routeSegment);
				
				//add to the GeoJson features
				geoJson.getFeatures().add((FeatureGeneric) featureRouteLineString);
			}
		}
		
		
		return geoJson;
	
	}

	private RouteBusDetailResource createRouteBusDetailResource(RouteBusSegmentPortion routeBusSegmentPortion, int detailSequenceNumber){
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
		routeBusDetailResource.setStops(numberOfStops); //numberOfStops - 1 == numberOfEdges!
		routeBusDetailResource.setSequenceNumber(detailSequenceNumber);
	
		return routeBusDetailResource;
	}
	
	private RouteFootDetailResource createRouteFootDetailResource(RouteSegment routeSegment, int detailSequenceNumber){
		//extract the needed information from the RouteSegment
		List<BusStop> busStops = routeSegment.getBusStops();
		int numberOfStops = busStops.size();
		BusStop firstBusStop = busStops.get(0);
		BusStop lastBusStop = busStops.get(numberOfStops-1);
		String from = firstBusStop.getId() + " - " + firstBusStop.getName();
		String to = lastBusStop.getId() + " - " + lastBusStop.getName();
		double[] firstBusStopCoordinates = new double[]{firstBusStop.getLat(), firstBusStop.getLng()};
		int distance = ((Double) linesService.getDistanceFromBusStop(firstBusStopCoordinates, lastBusStop.getId())).intValue();
		
		//create the RouteBusDetailResource for each RouteBusSegmentPortion
		RouteFootDetailResource routeFootDetailResource = new RouteFootDetailResource();
		routeFootDetailResource.setFrom(from);
		routeFootDetailResource.setTo(to);
		routeFootDetailResource.setLength(distance);
		routeFootDetailResource.setSequenceNumber(detailSequenceNumber);
		
		return routeFootDetailResource;
	}
	
	private FeatureRouteLineString createBusFeatureRouteLineString(RouteBusSegmentPortion routeBusSegmentPortion){
		//extract the needed information from the RouteBusSegmentPortion
		String line = routeBusSegmentPortion.getLineId();
		List<BusStop> busStops = routeBusSegmentPortion.getBusStops();
		int numberOfStops = busStops.size();
		BusStop firstBusStop = busStops.get(0);
		BusStop lastBusStop = busStops.get(numberOfStops-1);
		String from = firstBusStop.getId() + " - " + firstBusStop.getName();
		String to = lastBusStop.getId() + " - " + lastBusStop.getName();
		
		//create the PropertiesRouteLineString
		PropertiesRouteLineStringBus propertiesRouteLineStringBus = new PropertiesRouteLineStringBus();
		propertiesRouteLineStringBus.setFrom(from);
		propertiesRouteLineStringBus.setTo(to);
		propertiesRouteLineStringBus.setLine(line);
		propertiesRouteLineStringBus.setStops(numberOfStops);
		
		//create the GeometryLineString
		GeometryLineString geometryLineString = new GeometryLineString();
		/*
		 * WARNING: THE BUS STOPS ARE SORTED INTO THE ARRAY LIST!
		 */
		for (BusStop busStop : busStops) {
			List<Double> coordinate = new ArrayList<>();
			coordinate.add(busStop.getLng());
			coordinate.add(busStop.getLat());
			geometryLineString.getCoordinates().add(coordinate);
		}
		
		//create the FeatureRouteLineString
		FeatureRouteLineString featureRouteLineString = new FeatureRouteLineString();
		featureRouteLineString.setGeometry(geometryLineString);
		featureRouteLineString.setProperties(propertiesRouteLineStringBus);
		
		return featureRouteLineString;
	}
	
	private FeatureRouteLineString createFootFeatureRouteLineString(RouteSegment routeSegment){
		//extract the needed information from the RouteBusSegmentPortion
		List<BusStop> busStops = routeSegment.getBusStops();
		int numberOfStops = busStops.size();
		BusStop firstBusStop = busStops.get(0);
		BusStop lastBusStop = busStops.get(numberOfStops-1);
		String from = firstBusStop.getId() + " - " + firstBusStop.getName();
		String to = lastBusStop.getId() + " - " + lastBusStop.getName();
		double[] firstBusStopCoordinates = new double[]{firstBusStop.getLat(), firstBusStop.getLng()};
		int distance = ((Double) linesService.getDistanceFromBusStop(firstBusStopCoordinates, lastBusStop.getId())).intValue();
		
		//create the PropertiesRouteLineStringFoot
		PropertiesRouteLineStringFoot propertiesRouteLineStringFoot = new PropertiesRouteLineStringFoot();
		propertiesRouteLineStringFoot.setFrom(from);
		propertiesRouteLineStringFoot.setTo(to);
		propertiesRouteLineStringFoot.setLength(distance);
		
		//create the GeometryLineString
		GeometryLineString geometryLineString = new GeometryLineString();
		/*
		 * WARNING: THE BUS STOPS ARE SORTED INTO THE ARRAY LIST!
		 */
		for (BusStop busStop : busStops) {
			List<Double> coordinate = new ArrayList<>();
			coordinate.add(busStop.getLng());
			coordinate.add(busStop.getLat());
			geometryLineString.getCoordinates().add(coordinate);
		}
		
		//create the FeatureRouteLineString
		FeatureRouteLineString featureRouteLineString = new FeatureRouteLineString();
		featureRouteLineString.setGeometry(geometryLineString);
		featureRouteLineString.setProperties(propertiesRouteLineStringFoot);
		
		return featureRouteLineString;
	}
	
	private FeatureRouteLineString createFirstFootFeatureRouteLineString(Route route) {
		//extract the needed information from the Route
		double fromLat = route.getStartCoordinates()[0];
		double fromLng = route.getStartCoordinates()[1];
		BusStop firstBusStop = route.getSegments().get(0).getBusStops().get(0);
		double firstBusStopLat = firstBusStop.getLat();
		double firstBusStopLng = firstBusStop.getLng();
		
		String from = "Lat: "+fromLat+ " - Lng: "+ fromLng;
		String to = firstBusStop.getId() + " - " + firstBusStop.getName();
		double[] fromCoordinates = new double[]{fromLat, fromLng};
		int distance = ((Double) linesService.getDistanceFromBusStop(fromCoordinates, firstBusStop.getId())).intValue();
		
		//create the PropertiesRouteLineStringFoot
		PropertiesRouteLineStringFoot propertiesRouteLineStringFoot = new PropertiesRouteLineStringFoot();
		propertiesRouteLineStringFoot.setFrom(from);
		propertiesRouteLineStringFoot.setTo(to);
		propertiesRouteLineStringFoot.setLength(distance);
		
		//create the GeometryLineString
		GeometryLineString geometryLineString = new GeometryLineString();
		//from point coordinates
		List<Double> fromCoordinateList = new ArrayList<>();
		fromCoordinateList.add(fromLng);	//WARNING: LNG <-> LAT!
		fromCoordinateList.add(fromLat);	//WARNING: LNG <-> LAT!
		geometryLineString.getCoordinates().add(fromCoordinateList);
		//first stop coordinates
		List<Double> firstBusStopCoordinateList = new ArrayList<>();
		firstBusStopCoordinateList.add(firstBusStopLng);	//WARNING: LNG <-> LAT!
		firstBusStopCoordinateList.add(firstBusStopLat);	//WARNING: LNG <-> LAT!
		geometryLineString.getCoordinates().add(firstBusStopCoordinateList);
		
		//create the FeatureRouteLineString
		FeatureRouteLineString featureRouteLineString = new FeatureRouteLineString();
		featureRouteLineString.setGeometry(geometryLineString);
		featureRouteLineString.setProperties(propertiesRouteLineStringFoot);
		
		return featureRouteLineString;
	}
	
	private FeatureRouteLineString createLastFootFeatureRouteLineString(Route route) {
		//extract the needed information from the Route
		double toLat = route.getArriveCoordinates()[0];
		double toLng = route.getArriveCoordinates()[1];
		int numberOfSegments = route.getSegments().size();
		RouteSegment lastRouteSegment = route.getSegments().get(numberOfSegments-1);
		int numberOfStopsInLastRouteSegment = lastRouteSegment.getBusStops().size();
		BusStop lastBusStop = lastRouteSegment.getBusStops().get(numberOfStopsInLastRouteSegment-1);
		double lastBusStopLat = lastBusStop.getLat();
		double lastBusStopLng = lastBusStop.getLng();
		
		String from = "Lat: "+toLat+ " - Lng: "+ toLng;
		String to = lastBusStop.getId() + " - " + lastBusStop.getName();
		double[] toCoordinates = new double[]{toLat, toLng};
		int distance = ((Double) linesService.getDistanceFromBusStop(toCoordinates, lastBusStop.getId())).intValue();
		
		//create the PropertiesRouteLineStringFoot
		PropertiesRouteLineStringFoot propertiesRouteLineStringFoot = new PropertiesRouteLineStringFoot();
		propertiesRouteLineStringFoot.setFrom(from);
		propertiesRouteLineStringFoot.setTo(to);
		propertiesRouteLineStringFoot.setLength(distance);
		
		//create the GeometryLineString
		GeometryLineString geometryLineString = new GeometryLineString();
		//to point coordinates
		List<Double> toCoordinateList = new ArrayList<>();
		toCoordinateList.add(toLng);	//WARNING: LNG <-> LAT!
		toCoordinateList.add(toLat);	//WARNING: LNG <-> LAT!
		geometryLineString.getCoordinates().add(toCoordinateList);
		//last stop coordinates
		List<Double> lastBusStopCoordinateList = new ArrayList<>();
		lastBusStopCoordinateList.add(lastBusStopLng);	//WARNING: LNG <-> LAT!
		lastBusStopCoordinateList.add(lastBusStopLat);	//WARNING: LNG <-> LAT!
		geometryLineString.getCoordinates().add(lastBusStopCoordinateList);
		
		//create the FeatureRouteLineString
		FeatureRouteLineString featureRouteLineString = new FeatureRouteLineString();
		featureRouteLineString.setGeometry(geometryLineString);
		featureRouteLineString.setProperties(propertiesRouteLineStringFoot);
		
		return featureRouteLineString;
	}
	
}
