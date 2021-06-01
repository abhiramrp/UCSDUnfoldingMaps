package module6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.jogamp.newt.event.KeyEvent;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Google.GoogleSimplifiedProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	private static final boolean offline = false;
	
	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	

	public void setup() {
		// setting up PAppler
		size(900, 700);
		if (offline) {
		    map = new UnfoldingMap(this, 0, 0, 650, 600, new MBTilesMapProvider(mbTilesString));
		}
		else {
			map = new UnfoldingMap(this, 0, 0, 650, 600, new GoogleSimplifiedProvider());

		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// setting up map and default events


		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airportLoc = new HashMap<Integer, Location>();
		HashMap<Integer, AirportMarker> airMarker = new HashMap<Integer, AirportMarker>();
		
		// create markers from features
		for(PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature);
			

	
			//m.setRadius(5);
			//airportList.add(m);
			

			// put airport in hashmap with OpenFlights unique id for key
			airportLoc.put(Integer.parseInt(feature.getId()), feature.getLocation());
			/*
			// airports.put(Integer.parseInt(feature.getId()), feature.getProperties());
			*/
			airMarker.put(Integer.parseInt(feature.getId()), m);
			
			
			

		}
		


		
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		
		

		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			

			
			// get locations for airports on route
			if(airportLoc.containsKey(source) && airportLoc.containsKey(dest)) {
				/*
				
				AirportMarker airSource = new AirportMarker(airMarker.get(source), airportLoc.get(source), airports.get(source));
				AirportMarker airDest = new AirportMarker(airMarker.get(dest), airportLoc.get(dest), airports.get(dest));
				
				*/
				
				AirportMarker airSource = airMarker.get(source);
				AirportMarker airDest = airMarker.get(dest);
				
				if((airSource.getArea().equals(airDest.getArea())) == false) {

					route.addLocation(airportLoc.get(source));
					route.addLocation(airportLoc.get(dest));
					
					airSource.setIntl(true);
					airDest.setIntl(true);
					
					if(!airportList.contains(airSource)) {
						airportList.add(airSource);
					}
					
					if(!airportList.contains(airDest)) {
						airportList.add(airDest);
					}
					
			

					SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());

		
				
					//routeList.add(sl);
					
					
					airSource.addRoute(sl);
					airDest.addRoute(sl);
				}

			}
				
		}
		
		delMarkers();
		
		addAllRoutes();
		

		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		map.addMarkers(routeList);
		
		map.addMarkers(airportList);
		
		
		
	}
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	private void delMarkers() {

		List<Location> delLocs = new ArrayList<Location>();
		
		for(int i=0; i<airportList.size(); i++) {
			AirportMarker a = (AirportMarker) airportList.get(i);
			
			
			if (a.numRoutes() <= 4) {
				airportList.remove(i);
				
				delLocs.add(a.getLocation());
				
				i--;
			}
		}
		
		
		for(int i=0; i<airportList.size(); i++) {
			AirportMarker a = (AirportMarker) airportList.get(i);
			
			for (Location loc : delLocs) {
				a.removeRoute(loc);
			}
		}
		
		
	}
	
	private void addAllRoutes() {
		for (Marker m : airportList ) {
			AirportMarker a = (AirportMarker) m;
			
			List <Marker> airRoutes = a.getRoutes();
			
			routeHideStatus(airRoutes, true);
			
			routeList.addAll(airRoutes);
			
		}
	}
	
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(airportList);

	}
	
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker m : markers) 
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}
	
	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) {
			unhideMarkers(false);
			lastClicked = null;
		}
		else if (lastClicked == null) 
		{
			checkAirportsForClick();
		}
	
	}
	
	private void unhideMarkers(boolean key) {
		for(Marker marker : airportList) {
			marker.setHidden(false);
			AirportMarker a = (AirportMarker) marker;
		
			
			routeHideStatus(a.getRoutes(), key);
			
		}
	}
		

	private void routeHideStatus(List<Marker> markers, boolean hide) {
		for (Marker m : markers) {
			SimpleLinesMarker r = (SimpleLinesMarker) m;
			
			r.setHidden(!hide);
			
		}
	}
	
	private void checkAirportsForClick() {
		if (lastClicked != null) return;
		for (Marker marker : airportList) {
			
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker)marker;

				routeHideStatus(((AirportMarker) lastClicked).getRoutes(), false);


				// Hide all the other earthquakes and hide
				for (Marker mhide : airportList) {
					AirportMarker otherAirport = (AirportMarker) mhide;
					
					boolean isLoc = ((AirportMarker) lastClicked).checkLoc(otherAirport.getLocation());
					
					if(isLoc == true) {
						otherAirport.setHidden(false);
					} else {
						otherAirport.setHidden(true);
					}
				}
			}
		}	
	}
	
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		
		int xbase = 725;
		int ybase = 50;
		
		rect(xbase, ybase, 175, 200);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("International Airports", xbase+10, ybase+25);
		
		
		text("Size ~ Number of Routes", xbase+10, ybase+75);
		
		text("Hold SPACE KEY to see", xbase+10, ybase+100);
		text("all routes on the map", xbase+10, ybase+125);

		
	}
	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			unhideMarkers(true);
		}
		
	}
	
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			unhideMarkers(false);
		}

		
	}



}
