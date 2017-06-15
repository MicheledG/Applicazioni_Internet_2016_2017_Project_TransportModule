package it.polito.ai.transportmodule.resources;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;

import it.polito.ai.transportmodule.resources.geojson.GeoJson;

public class RouteResource extends ResourceSupport {
	
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
}
