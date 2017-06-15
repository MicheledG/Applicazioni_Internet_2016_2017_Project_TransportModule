package it.polito.ai.transportmodule.resources.geojson;

import java.util.ArrayList;
import java.util.List;

public class GeoJson {
	
	private String type="FeatureCollection";
	private List<FeatureGeneric> features = new ArrayList<>();
	public String getType() {
		return type;
	}
	public List<FeatureGeneric> getFeatures() {
		return features;
	}
	public void setFeatures(List<FeatureGeneric> features) {
		this.features = features;
	}
	
}
