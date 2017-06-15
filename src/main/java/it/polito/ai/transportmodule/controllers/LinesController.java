package it.polito.ai.transportmodule.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.polito.ai.transportmodule.model.jpa.BusLine;
import it.polito.ai.transportmodule.model.jpa.BusLineStop;
import it.polito.ai.transportmodule.resources.LineResource;
import it.polito.ai.transportmodule.resources.LineSnippetResource;
import it.polito.ai.transportmodule.resources.StopResource;
import it.polito.ai.transportmodule.resources.geojson.FeatureGeneric;
import it.polito.ai.transportmodule.resources.geojson.GeoJson;
import it.polito.ai.transportmodule.resources.geojson.GeometryLineString;
import it.polito.ai.transportmodule.resources.geojson.GeometryPoint;
import it.polito.ai.transportmodule.resources.geojson.line.FeatureLineLineString;
import it.polito.ai.transportmodule.resources.geojson.line.FeatureLinePoint;
import it.polito.ai.transportmodule.resources.geojson.line.PropertiesLinePoint;
import it.polito.ai.transportmodule.services.LinesService;

@RestController
@CrossOrigin(origins = "*")
public class LinesController {
	
	@Autowired
	private LinesService lineService;
	
	@RequestMapping(value="/lines", method=RequestMethod.GET)
	public HttpEntity<List<LineSnippetResource>> getLines(){
		List<String> lineNames = lineService.getBusLineNames();
		List<LineSnippetResource> lineSnippetResources = new ArrayList<>();
		for (String lineName: lineNames) {
			LineSnippetResource lineSnippetResource = new LineSnippetResource(lineName);
			lineSnippetResource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LinesController.class)
					.getLine(
							lineName,
							true,
							false,
							false
							)).withSelfRel());
			lineSnippetResources.add(lineSnippetResource);
		}
		
		ResponseEntity<List<LineSnippetResource>> responseEntity = new ResponseEntity<List<LineSnippetResource>>(lineSnippetResources, HttpStatus.OK);
		return responseEntity;
	}
	
	@RequestMapping(value="/lines/{lineName}", method=RequestMethod.GET)
	public HttpEntity<LineResource> getLine(
			@PathVariable String lineName,
			@RequestParam(value="description", required=false, defaultValue="false") boolean descriptionReq,
			@RequestParam(value="stops", required=false, defaultValue="false") boolean stopsReq,
			@RequestParam(value="geoJson", required=false, defaultValue="false") boolean geoJsonReq){
		
		ResponseEntity<LineResource> responseEntity ;
		
		BusLine busLine = lineService.getBusLine(lineName);
		
		if(busLine == null){
			//no line with the provided name was found
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return responseEntity;
		}
		
		//create and fill the LineResource
		LineResource lineResource = new LineResource();
		//mandatory fields
		lineResource.setLine(busLine.getLine());
		lineResource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LinesController.class)
						.getLine(
								lineName,
								true,
								false,
								false
								)).withSelfRel());
		
		if(descriptionReq){
			//add the "description" field of LineResource
			lineResource.setDescription(busLine.getDescription());
		}
		
		if(stopsReq){
			//add the "stops" field of LineResource
			for (BusLineStop busLineStop: busLine.getLineStops()) {
				StopResource stopResource = new StopResource();
				stopResource.setSequenceNumber(busLineStop.getSequenceNumber());
				stopResource.setId(busLineStop.getBusStop().getId());
				stopResource.setName(busLineStop.getBusStop().getName());
				
				//retrieve the list of the lines stopping at each stop
				List<BusLine> stoppingLines = lineService.getBusLinesOfBusStop(stopResource.getId());
				for (BusLine stoppingLine : stoppingLines) {
					String stoppingLineName = stoppingLine.getLine();
					stopResource.getLines().add(stoppingLineName);
				}
				
				lineResource.getStops().add(stopResource);
			}
		}
		
		if(geoJsonReq){
			//add the "geoJson" field of LineResource
			GeoJson geoJson = this.createLineGeoJson(busLine);
			lineResource.setGeoJson(geoJson);
		}
		
		responseEntity = new ResponseEntity<LineResource>(lineResource, HttpStatus.OK);
		return responseEntity;
	}
	
	
	private GeoJson createLineGeoJson(BusLine busLine){
		
		GeoJson geoJson = new GeoJson();
		
		List<BusLineStop> busLineStops = busLine.getLineStops();
		//be sure that the stops are sorted in ascending sequence number
		Collections.sort(busLineStops);
		for (BusLineStop busLineStop : busLineStops) {
			//extract busLineStop information
			String stopName = busLineStop.getBusStop().getName();
			String stopId = busLineStop.getBusStop().getId();
			int sequenceNumber = busLineStop.getSequenceNumber();
			double latitude = busLineStop.getBusStop().getLat();
			double longitude = busLineStop.getBusStop().getLng();
			
			//create and fill the FeatureLinePoint
			FeatureLinePoint featureLinePoint = new FeatureLinePoint();
			
			PropertiesLinePoint propertiesLinePoint = new PropertiesLinePoint();
			propertiesLinePoint.setStopName(stopName);
			propertiesLinePoint.setStopId(stopId);
			propertiesLinePoint.setSequenceNumber(sequenceNumber);
			//retrieve the list of the lines stopping at each stop
			List<BusLine> stoppingLines = lineService.getBusLinesOfBusStop(stopId);
			for (BusLine stoppingLine : stoppingLines) {
				String stoppingLineName = stoppingLine.getLine();
				propertiesLinePoint.getLines().add(stoppingLineName);
			}
			featureLinePoint.setProperties(propertiesLinePoint);
			
			GeometryPoint geometryLinePoint = new GeometryPoint();
			geometryLinePoint.getCoordinates().add(longitude);
			geometryLinePoint.getCoordinates().add(latitude);
			featureLinePoint.setGeometry(geometryLinePoint);
			
			//add each point feature to the features list
			geoJson.getFeatures().add((FeatureGeneric)featureLinePoint);
		}
		
		//create and fill the FeatureLineLineString
		FeatureLineLineString featureLineLineString = new FeatureLineLineString();
		
		GeometryLineString geometryLineLineString = new GeometryLineString();
		for (Object object : geoJson.getFeatures()) {
			//till now we have only FeatureLinePoint in the features list
			FeatureLinePoint featureLinePoint = (FeatureLinePoint) object;
			List<Double> pointCoordinates = featureLinePoint.getGeometry().getCoordinates(); 
			geometryLineLineString.getCoordinates().add(pointCoordinates);
		}
		
		featureLineLineString.setGeometry(geometryLineLineString);
		
		//add the LineString feature to the features list
		geoJson.getFeatures().add((FeatureGeneric)featureLineLineString);
		return geoJson;
	}
}
