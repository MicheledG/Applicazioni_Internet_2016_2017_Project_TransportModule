package it.polito.ai.transportmodule.resources.geojson.linelinestring;

import it.polito.ai.transportmodule.resources.geojson.FeatureGeneric;

public class FeatureLineLineString extends FeatureGeneric {
	private GeometryLineLineString geometry;
	public GeometryLineLineString getGeometry() {
		return geometry;
	}
	public void setGeometry(GeometryLineLineString geometry) {
		this.geometry = geometry;
	}
}
