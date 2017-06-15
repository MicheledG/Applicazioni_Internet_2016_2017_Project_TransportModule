package it.polito.ai.transportmodule.resources.geojson.route;

import it.polito.ai.transportmodule.resources.geojson.FeatureGeneric;
import it.polito.ai.transportmodule.resources.geojson.GeometryPoint;

public class FeatureRoutePoint extends FeatureGeneric{
	private GeometryPoint geometry;
	private PropertiesRoutePoint properties;
	public GeometryPoint getGeometry() {
		return geometry;
	}
	public void setGeometry(GeometryPoint geometry) {
		this.geometry = geometry;
	}
	public PropertiesRoutePoint getProperties() {
		return properties;
	}
	public void setProperties(PropertiesRoutePoint properties) {
		this.properties = properties;
	}
	
}
