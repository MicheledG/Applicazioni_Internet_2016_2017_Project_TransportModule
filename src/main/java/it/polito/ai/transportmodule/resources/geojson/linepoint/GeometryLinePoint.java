package it.polito.ai.transportmodule.resources.geojson.linepoint;

import java.util.ArrayList;
import java.util.List;

public class GeometryLinePoint {
	private String type = "Point";
	/*
	 * coordinates[0] => longitude
	 * coordinates[1] => latitude
	 */
	private List<Double> coordinates =  new ArrayList<>();
	public String getType() {
		return type;
	}
	public List<Double> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(List<Double> coordinates) {
		this.coordinates = coordinates;
	}
}
