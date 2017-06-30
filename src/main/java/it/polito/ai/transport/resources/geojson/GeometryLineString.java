package it.polito.ai.transport.resources.geojson;

import java.util.ArrayList;
import java.util.List;

public class GeometryLineString {
	private String type = "LineString";
	/*
	 * coordinates[i][0] => longitude
	 * coordinates[i][1] => latitude
	 */
	private List<List<Double>> coordinates = new ArrayList<>();
	public String getType() {
		return type;
	}
	public List<List<Double>> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(List<List<Double>> coordinates) {
		this.coordinates = coordinates;
	}
}
