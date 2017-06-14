package it.polito.ai.transportmodule.services;

import org.springframework.stereotype.Service;

@Service
public class GeographyHelperImpl implements GeographyHelper {

	@Override
	public String createTextGeographyPoint(double lat, double lng) {
		
		String textGeometryPoint = "SRID=4326;POINT("+lng+" "+lat+")";
		return textGeometryPoint;
	}

}
