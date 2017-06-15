package it.polito.ai.transportmodule.resources.geojson.line;

import it.polito.ai.transportmodule.resources.geojson.FeatureGeneric;
import it.polito.ai.transportmodule.resources.geojson.GeometryLineString;

public class FeatureLineLineString extends FeatureGeneric {
	private GeometryLineString geometry;
	public GeometryLineString getGeometry() {
		return geometry;
	}
	public void setGeometry(GeometryLineString geometry) {
		this.geometry = geometry;
	}
}
