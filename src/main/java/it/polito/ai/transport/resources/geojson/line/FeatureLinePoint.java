package it.polito.ai.transport.resources.geojson.line;

import it.polito.ai.transport.resources.geojson.FeatureGeneric;
import it.polito.ai.transport.resources.geojson.GeometryPoint;

public class FeatureLinePoint extends FeatureGeneric {
	private PropertiesLinePoint properties;
	private GeometryPoint geometry;
	public GeometryPoint getGeometry() {
		return geometry;
	}
	public void setGeometry(GeometryPoint geometry) {
		this.geometry = geometry;
	}
	public PropertiesLinePoint getProperties() {
		return properties;
	}
	public void setProperties(PropertiesLinePoint properties) {
		this.properties = properties;
	}
	
}
