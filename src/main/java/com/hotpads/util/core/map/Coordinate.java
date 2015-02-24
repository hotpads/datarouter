package com.hotpads.util.core.map;

public class Coordinate{
	
	public static final Coordinate SAMPLE_WASHINGTON_DC = new Coordinate(38.895111, -77.036667);
	public static final Coordinate SAMPLE_CHICAGO_IL = new Coordinate(41.836944, -87.684444);
	public static final Coordinate SAMPLE_DENVER_CO = new Coordinate(39.739167, -104.984722);
	public static final Coordinate SAMPLE_SAN_FRANCISCO_CA = new Coordinate(37.7793, -122.4192);
	public static final Coordinate SAMPLE_LONDON_UK = new Coordinate(51.508056, -0.124722);
	public static final Coordinate SAMPLE_BEIJING_CN = new Coordinate(39.913889, 116.391667);
	
	public Coordinate(Double lat, Double lon){
		this.lat = lat;
		this.lon = lon;
	}
	
	Double lat = null;
	Double lon = null;

	
	public Double getLat() {
		return lat;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public Double getLon() {
		return lon;
	}
	public void setLon(Double lon) {
		this.lon = lon;
	}
	
	@Override
	public String toString(){
		return ("["+lat+","+lon+"]");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lat == null) ? 0 : lat.hashCode());
		result = prime * result + ((lon == null) ? 0 : lon.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (lat == null) {
			if (other.lat != null)
				return false;
		} else if (!lat.equals(other.lat))
			return false;
		if (lon == null) {
			if (other.lon != null)
				return false;
		} else if (!lon.equals(other.lon))
			return false;
		return true;
	}
	
	
	
	
	
	
}
