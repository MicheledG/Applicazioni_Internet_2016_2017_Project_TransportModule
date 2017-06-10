package it.polito.ai.transportmodule.resources.geojson;

import java.util.ArrayList;
import java.util.List;

public class GeoJson {
	
	private String type="FeatureCollection";
	private List<Object> features = new ArrayList<>();
	public String getType() {
		return type;
	}
	public List<Object> getFeatures() {
		return features;
	}
	public void setFeatures(List<Object> features) {
		this.features = features;
	}
	
}
