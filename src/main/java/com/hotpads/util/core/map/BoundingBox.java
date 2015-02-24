package com.hotpads.util.core.map;



public interface BoundingBox {

	Double getMaxLat();
	Double getMinLat();
	Double getMaxLon();
	Double getMinLon();
	boolean hasLats();
	boolean hasLons();
	
	void setMaxLat(Double maxLat);
	void setMinLat(Double minLat);
	void setMaxLon(Double maxLon);
	void setMinLon(Double minLon);
	
	Double getCenterLat();
	Double getCenterLon();
	
	Coordinate getCenter();
	boolean in(Coordinate c);
}
