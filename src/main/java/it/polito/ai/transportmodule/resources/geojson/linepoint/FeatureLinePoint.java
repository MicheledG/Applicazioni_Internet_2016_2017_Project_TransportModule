package it.polito.ai.transportmodule.resources.geojson.linepoint;

import it.polito.ai.transportmodule.resources.geojson.FeatureGeneric;

public class FeatureLinePoint extends FeatureGeneric {
	private PropertiesLinePoint properties;
	private GeometryLinePoint geometry;
	public GeometryLinePoint getGeometry() {
		return geometry;
	}
	public void setGeometry(GeometryLinePoint geometry) {
		this.geometry = geometry;
	}
	public PropertiesLinePoint getProperties() {
		return properties;
	}
	public void setProperties(PropertiesLinePoint properties) {
		this.properties = properties;
	}
	
}
