package it.polito.ai.transportmodule.resources;

public class RouteFootDetailResource extends RouteDetailResource {
	
	private static final String type = "foot";
	private int length; //in meter
	public static String getType() {
		return type;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	
}
