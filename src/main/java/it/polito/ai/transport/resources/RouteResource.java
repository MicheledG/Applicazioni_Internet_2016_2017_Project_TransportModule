package it.polito.ai.transport.resources;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;

import it.polito.ai.transport.resources.geojson.GeoJson;

public class RouteResource extends ResourceSupport {
	
	private double[] fromCoordinates;
	private double[] toCoordinates;
	private List<RouteDetailResource> details = new ArrayList<>();
	private GeoJson geoJson;
	
	public GeoJson getGeoJson() {
		return geoJson;
	}
	public void setGeoJson(GeoJson geoJson) {
		this.geoJson = geoJson;
	}
	public List<RouteDetailResource> getDetails() {
		return details;
	}
	public void setDetails(List<RouteDetailResource> details) {
		this.details = details;
	}
	public double[] getFromCoordinates() {
		return fromCoordinates;
	}
	public void setFromCoordinates(double[] fromCoordinates) {
		this.fromCoordinates = fromCoordinates;
	}
	public double[] getToCoordinates() {
		return toCoordinates;
	}
	public void setToCoordinates(double[] toCoordinates) {
		this.toCoordinates = toCoordinates;
	}
}
