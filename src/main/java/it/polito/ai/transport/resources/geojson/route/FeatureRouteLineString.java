package it.polito.ai.transport.resources.geojson.route;

import it.polito.ai.transport.resources.geojson.FeatureGeneric;
import it.polito.ai.transport.resources.geojson.GeometryLineString;

public class FeatureRouteLineString extends FeatureGeneric {
	private GeometryLineString geometry;
	private PropertiesRouteLineString properties;
	public GeometryLineString getGeometry() {
		return geometry;
	}
	public void setGeometry(GeometryLineString geometry) {
		this.geometry = geometry;
	}
	public PropertiesRouteLineString getProperties() {
		return properties;
	}
	public void setProperties(PropertiesRouteLineString properties) {
		this.properties = properties;
	}
}
