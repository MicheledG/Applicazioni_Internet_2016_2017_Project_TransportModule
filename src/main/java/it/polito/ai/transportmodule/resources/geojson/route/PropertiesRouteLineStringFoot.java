package it.polito.ai.transportmodule.resources.geojson.route;

public class PropertiesRouteLineStringFoot extends PropertiesRouteLineString {
	
	private static final String type = "foot";
	private int length;
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
