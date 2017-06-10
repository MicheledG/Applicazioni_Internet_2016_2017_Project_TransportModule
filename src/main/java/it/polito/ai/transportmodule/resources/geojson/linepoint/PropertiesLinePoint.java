package it.polito.ai.transportmodule.resources.geojson.linepoint;

import java.util.ArrayList;
import java.util.List;

public class PropertiesLinePoint {
	private String stopId;
	private String stopName;
	private int sequenceNumber;
	private List<String> lines = new ArrayList<>();
	public String getStopId() {
		return stopId;
	}
	public void setStopId(String stopId) {
		this.stopId = stopId;
	}
	public String getStopName() {
		return stopName;
	}
	public void setStopName(String stopName) {
		this.stopName = stopName;
	}
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public List<String> getLines() {
		return lines;
	}
	public void setLines(List<String> lines) {
		this.lines = lines;
	}
}
