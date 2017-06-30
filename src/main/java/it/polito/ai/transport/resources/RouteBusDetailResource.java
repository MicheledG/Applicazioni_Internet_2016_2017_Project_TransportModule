package it.polito.ai.transport.resources;

public class RouteBusDetailResource extends RouteDetailResource {
	
	private final String type = "bus";
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
	public String getType() {
		return type;
	}
	
	
}
