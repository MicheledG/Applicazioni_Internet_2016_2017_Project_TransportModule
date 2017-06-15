package it.polito.ai.transportmodule.resources;

public class RouteBusDetailResource extends RouteDetailResource {
	
	private static final String type = "bus";
	private String line;
	private int stops;
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
	public static String getType() {
		return type;
	}
	
	
}
