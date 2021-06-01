package module6;

import java.util.List;
import java.util.ArrayList;


import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PConstants;
import processing.core.PGraphics;

/** 
 * A class to represent AirportMarkers on a world map.
 *   
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMarker extends CommonMarker implements Comparable<AirportMarker>{
	List<Marker> routes = new ArrayList<Marker>();
	

	
	public static boolean intl= false; 
	
	public static int TRI_SIZE = 5;
	
	public AirportMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	
	}
		
	
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		
		pg.pushStyle();
		
		pg.fill(255,0,0);
		this.setRadius(this.numRoutes());
		pg.ellipse(x, y, this.radius, this.radius);
		
		pg.popStyle();
		
		
	}
	


	public int compareTo(AirportMarker air) {
		int status = 0; 
		
		if (this.numRoutes() < air.numRoutes()) {
			status = 1; 
		} else if (this.numRoutes() > air.numRoutes()) {
			status = -1;
		} else {
			status = 0; 
		}
		
		
		return status;
	}
	
	

	@Override
	public void showTitle(PGraphics pg, float x, float y)
	{
		String name = getName() + " (" + getCode() + ")";
		String city = getCity() + ", " + getCountry();
		String rs = "Routes: " + numRoutes();

		pg.pushStyle();
		
		pg.fill(255, 255, 255);
		pg.textSize(12);
		pg.rectMode(PConstants.CORNER);
		pg.rect(x, y-TRI_SIZE-39, Math.max(pg.textWidth(name), pg.textWidth(city)) + 6, 48);
		pg.fill(0, 0, 0);
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.text(name, x+3, y-TRI_SIZE-40);
		pg.text(city, x+3, y - TRI_SIZE -24);
		pg.text(rs, x+3, y - TRI_SIZE -8);
		
		pg.popStyle();
	}
	
	private String getName() 
	{
		return getStringProperty("name").replace("\"", "");
	}
	
	public String getCode()
	{
		return getStringProperty("code").replace("\"", "");
	}
	
	private String getCity()
	{
		return getStringProperty("city").replace("\"", "");
	}
	
	private String getCountry()
	{
		return getStringProperty("country").replace("\"", "");
	}
	
	public String getArea()
	{
		return getStringProperty("area").replace("\"", "");
	}
	
	public int numRoutes() {
		
		int num = routes.size();
		
		return num;
	}
	
	public void setRadius(float rad) {
		
		int num = routes.size();

		if (num >= 900) {
			rad = 12;
		} else if (num >= 800) {
			rad = 10;
		} else if (num >= 600) {
			rad = 9;
		} else if (num >= 400) {
			rad = 8;
		} else if (num >= 200) { 
			rad = 7;
		} else if (num >= 60) {
			rad = 6;
		} else {
			rad = 3;
		}
		
		this.radius = rad;
	}
	
	public void setIntl(boolean i) {
		intl = i;
	}
	
	
	public void addRoute(SimpleLinesMarker route) {

		if(!routes.contains(route)) {
			routes.add(route);
		}
		
	}
	
	public List<Marker> getRoutes () {
		return routes;
	}
	
	public void removeRoute(Location loc) {

		
		for(int i=0; i<routes.size(); i++) {
			SimpleLinesMarker r = (SimpleLinesMarker) routes.get(i);
			
			if(((r.getLocations()).contains(loc)) == true) {
				routes.remove(i);
				i--;
			}
		}

	}
	
	public boolean checkLoc(Location loc) {
		for(int i=0; i<routes.size(); i++) {
			SimpleLinesMarker r = (SimpleLinesMarker) routes.get(i);
			
			if(((r.getLocations()).contains(loc)) == true) {
				return true;
			}
		}
		
		return false;
	}
	
}
