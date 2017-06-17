package it.polito.ai.transportmodule.resources.geojson.route;

public class PropertiesRouteLineStringBus extends PropertiesRouteLineString {

	private final String type= "bus";
	private String line;
	private int stops;
	public String getType() {
		return type;
	}
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public int getStops() {
		return stops;
	}
	public void setStops(int stops) {
		this.stops = stops;
	}
	
}
