package it.polito.ai.transport.resources.geojson.route;

public class PropertiesRouteLineStringFoot extends PropertiesRouteLineString {
	
	private final String type = "foot";
	private int length;
	public String getType() {
		return type;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	
}
