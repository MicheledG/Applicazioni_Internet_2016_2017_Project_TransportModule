package it.polito.ai.transport.resources;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;

import it.polito.ai.transport.resources.geojson.GeoJson;

public class LineResource extends ResourceSupport {
	private String line;
	private String description;
	private List<StopResource> stops = new ArrayList<>();
	private GeoJson geoJson;
	
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<StopResource> getStops() {
		return stops;
	}
	public void setStops(List<StopResource> stops) {
		this.stops = stops;
	}
	public GeoJson getGeoJson() {
		return geoJson;
	}
	public void setGeoJson(GeoJson geoJson) {
		this.geoJson = geoJson;
	}
}
