package com.hotpads.util.core.map;

import java.io.Serializable;
import java.util.Comparator;


import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("serial")
public class BoundingBoxImp implements BoundingBox, Serializable{
	public static double FAR_EAST_LONGITUDE = 170;
	public static double FAR_WEST_LONGITUDE = -170;
	public static double LONGITUDE_CIRCUMFERENCE = 360;
	public static double MAX_LONGITUDE_DEGREES = 180;
	public static double MIN_LONGITUDE_DEGREES = -180;
	private static double LONGITUDE_HEMISPHERE_DEGREES = 180;

	
	private Double maxLat;
	private Double minLat;
	private Double maxLon;
	private Double minLon;
	
	public BoundingBoxImp(){
	}
	
	public BoundingBoxImp(Double maxLat, Double minLat, Double maxLon, Double minLon){
		this.maxLat = maxLat;
		this.minLat = minLat;
		this.maxLon = maxLon;
		this.minLon = minLon;
	}
	
	public static Double getCenterLon(Double minLon, Double maxLon){
		if(minLon == null || maxLon == null){
			return null;
		}
		double centerLon = (minLon + maxLon)/2d; 
		if(minLon > maxLon){
			centerLon =  centerLon + (centerLon > 0 ? -1 : 1) * (LONGITUDE_CIRCUMFERENCE/2d);
		}
		return centerLon;
	}

	public static Double getCenterLat(Double minLat, Double maxLat){
		if(minLat == null || maxLat == null){
			return null;
		}
		double centerLat = (minLat + maxLat)/2d; 
		return centerLat;
	}

	
	public static boolean isCoordinateInBoundingBox(Coordinate c, BoundingBox boundingBox){
		Double maxLat = boundingBox.getMaxLat();
		Double minLat = boundingBox.getMinLat();
		Double maxLon = boundingBox.getMaxLon();
		Double minLon = boundingBox.getMinLon();
		if (maxLat != null && c.getLat() > maxLat){
			return false;
		}
		if (minLat != null && c.getLat() < minLat){
			return false;
		}
		
		if(maxLon == null || minLon == null){
			return true;
		}
		if(maxLon < minLon){
			if(c.getLon() < 0 && c.getLon() > maxLon){
				return false;
			}
			if(c.getLon() > 0 && c.getLon() < minLon){
				return false;
			}
		}else{
			if (c.getLon() > maxLon){
				return false;
			}
			if (c.getLon() < minLon){
				return false;
			}
		}
		return true;
	}
	
	public static Double calculateWidth(Double minLon, Double maxLon){
		if(minLon == null || maxLon == null){
			return null;
		}
		return maxLon - minLon > 0 ? maxLon - minLon : maxLon - minLon + LONGITUDE_CIRCUMFERENCE;
	}
	
	public static Double calculateHeight(Double minLat, Double maxLat){
		if(minLat == null || maxLat == null){
			return null;
		}
		return maxLat - minLat;
	}
	
	public static Double getLongitudeInDomain(Double lon){
		if(lon == null) return null;
		if(lon > LONGITUDE_HEMISPHERE_DEGREES){
			return lon - LONGITUDE_CIRCUMFERENCE;
		}else if(lon < -LONGITUDE_HEMISPHERE_DEGREES){
			return lon + LONGITUDE_CIRCUMFERENCE;
		}
		return lon;
	}
	
	public static void extendBoundingBox(Coordinate c, BoundingBox boundingBox){
		Double maxLat = boundingBox.getMaxLat();
		Double minLat = boundingBox.getMinLat();
		Double maxLon = boundingBox.getMaxLon();
		Double minLon = boundingBox.getMinLon();
		
		if (maxLat==null || c.getLat() > maxLat){
			boundingBox.setMaxLat(c.getLat());
		}
		if (minLat==null || c.getLat() < minLat){
			boundingBox.setMinLat(c.getLat());
		}
		
		if(maxLon==null){
			boundingBox.setMaxLon(c.getLon());
			boundingBox.setMinLon(c.getLon());
			return;
		}
		if (minLon <= maxLon  && (maxLon > FAR_EAST_LONGITUDE && c.getLon() < FAR_WEST_LONGITUDE)){
			boundingBox.setMaxLon(c.getLon());
			return;
		}
		if(minLon <= maxLon && minLon < FAR_WEST_LONGITUDE && c.getLon() > FAR_EAST_LONGITUDE){
			boundingBox.setMinLon(c.getLon());
			return;
		}	
		if(minLon > maxLon &&c.getLon() < FAR_WEST_LONGITUDE && c.getLon() < maxLon ){
			return;
		}	
		if(minLon > maxLon &&c.getLon() > FAR_EAST_LONGITUDE && c.getLon() > minLon ){
			return;
		}		
		if(minLon > maxLon &&c.getLon() < FAR_WEST_LONGITUDE && c.getLon() > maxLon ){
			boundingBox.setMaxLon(c.getLon());
			return;
		}		
		if(minLon > maxLon &&c.getLon() > FAR_EAST_LONGITUDE && c.getLon() < minLon ){
			boundingBox.setMinLon(c.getLon());
			return;
		}		
		if (c.getLon() < minLon){
			boundingBox.setMinLon(c.getLon());
		}		
		if (c.getLon() > maxLon){
			boundingBox.setMaxLon(c.getLon());
		}
	}
	
	public Coordinate getCenter(){
		return new Coordinate(getCenterLat(), getCenterLon());
	}
	
	public Double getCenterLat(){
		return (maxLat + minLat) / 2;
	}
	
	public Double getCenterLon(){
		return getCenterLon(minLon, maxLon);
	}
	
	//needs double check
	public void growToFit(Coordinate c){
		extendBoundingBox(c, this);
	}	
	
	public boolean in(Coordinate c) {
		return isCoordinateInBoundingBox(c, this);
	}
	
	public static class CenterDistanceComparator implements Comparator<BoundingBox>{
		private Coordinate centerCoordinate;
		
		public CenterDistanceComparator(Coordinate centerCoordinate){
			this.centerCoordinate = centerCoordinate;
		}
		
		public int compare(BoundingBox boundingBox1, BoundingBox boundingBox2){
			double diff = ProximityCalculator.distanceBetween(boundingBox1.getCenter(), centerCoordinate)
					- ProximityCalculator.distanceBetween(boundingBox2.getCenter(), centerCoordinate);
			if(diff > 0){
				return 1;
			}else if(diff == 0){
				return 0;
			}else{
				return -1;
			}
		}
	}
	
	@Override
	public boolean hasLats() {
		return getMaxLat() != null && getMinLat() != null;
	}
	
	@Override
	public boolean hasLons() {
		return getMaxLon() != null && getMinLon() != null;
	}
	
	public Double getMaxLat() {
		return maxLat;
	}
	public void setMaxLat(Double maxLat) {
		this.maxLat = maxLat;
	}
	public Double getMaxLon() {
		return maxLon;
	}
	public void setMaxLon(Double maxLon) {
		this.maxLon = maxLon;
	}
	public Double getMinLat() {
		return minLat;
	}
	public void setMinLat(Double minLat) {
		this.minLat = minLat;
	}
	public Double getMinLon() {
		return minLon;
	}
	public void setMinLon(Double minLon) {
		this.minLon = minLon;
	}
	
	public String toString(){
		String result = "";
		
		result += "BoundingBoxImp[maxLat: " + maxLat + " minLat: " + minLat + " maxLon: " + maxLon + " minLon: " + minLon + " center:(" + getCenter().getLat() + ", " + getCenter().getLon() + ")]";
		
		return result;
	}
	
	public static class BoundingBoxImpTests{
		@Test
		public void testGetCenterLon(){
			Assert.assertEquals((double)getCenterLon(33.1, 44.1), 38.6, 0.0001);
			Assert.assertEquals((double)getCenterLon(179.0, -178.0), -179.5, 0.00001);
			Assert.assertEquals((double)getCenterLon(178.0, -179.0), 179.5, 0.00001);
			Assert.assertEquals((double)getCenterLon(-179.0, -178.0), -178.5, 0.00001);//no revert
			Assert.assertEquals((double)getCenterLon(-178.0, -179.0), 1.5, 0.00001);//reverted
		}
		
		@Test
		public void testGetCenterLat(){
			Assert.assertEquals((double)getCenterLat(33.1, 44.1), 38.6, 0.0001);
		}
		
		@Test
		public void testGrowToFit(){
			BoundingBoxImp boundingBox = new BoundingBoxImp(20.0, 5.0, -177.0, 178.0);
			boundingBox.growToFit(new Coordinate(10d, 179d));
			Assert.assertEquals(-177d, boundingBox.getMaxLon(),0.000001);
			Assert.assertEquals(178d, boundingBox.getMinLon(),0.000001);
			
			boundingBox = new BoundingBoxImp(20.0, 5.0, -177.0, 178.0);
			boundingBox.growToFit(new Coordinate(10d, 177d));
			Assert.assertEquals(-177d, boundingBox.getMaxLon(),0.000001);
			Assert.assertEquals(177d, boundingBox.getMinLon(),0.000001);
			
			boundingBox = new BoundingBoxImp(20.0, 5.0, -177.0, 178.0);
			boundingBox.growToFit(new Coordinate(10d, -175d));
			Assert.assertEquals(-175d, boundingBox.getMaxLon(),0.000001);
			Assert.assertEquals(178d, boundingBox.getMinLon(),0.000001);
			
			boundingBox = new BoundingBoxImp(20.0, 5.0, -177.0, 178.0);
			boundingBox.growToFit(new Coordinate(21d, -175d));
			Assert.assertEquals(-175d, boundingBox.getMaxLon(),0.000001);
			Assert.assertEquals(178d, boundingBox.getMinLon(),0.000001);
			Assert.assertEquals(21d, boundingBox.getMaxLat(),0.000001);
			Assert.assertEquals(5d, boundingBox.getMinLat(),0.000001);
			
			boundingBox = new BoundingBoxImp(20.0, 5.0, -177.0, -178.0);
			boundingBox.growToFit(new Coordinate(10d, -177.5d));
			Assert.assertEquals(-177d, boundingBox.getMaxLon(),0.000001);
			Assert.assertEquals(-178d, boundingBox.getMinLon(),0.000001);
			
			boundingBox = new BoundingBoxImp(20.0, 5.0, -177.0, -178.0);
			boundingBox.growToFit(new Coordinate(10d, -175d));
			Assert.assertEquals(-175d, boundingBox.getMaxLon(),0.000001);
			Assert.assertEquals(-178d, boundingBox.getMinLon(),0.000001);
			
			boundingBox = new BoundingBoxImp(20.0, 5.0, -177.0, -178.0);
			boundingBox.growToFit(new Coordinate(10d, -179d));
			Assert.assertEquals(-177d, boundingBox.getMaxLon(),0.000001);
			Assert.assertEquals(-179d, boundingBox.getMinLon(),0.000001);
		}
		
		@Test
		public void testIsCoordinateInBoundingBox(){
			Assert.assertTrue(isCoordinateInBoundingBox(new Coordinate(33.1, 110.1), new BoundingBoxImp(50.0, 30.0, 120.0, 110.0)));
			Assert.assertTrue(isCoordinateInBoundingBox(new Coordinate(10.1, 179.1), new BoundingBoxImp(20.0, 5.0, -177.0, 178.0)));
			Assert.assertTrue(isCoordinateInBoundingBox(new Coordinate(33.1, -179.1), new BoundingBoxImp(50.0, 30.0, -177.0, 178.0)));
			Assert.assertFalse(isCoordinateInBoundingBox(new Coordinate(33.1, 177.1), new BoundingBoxImp(50.0, 30.0, -177.0, 178.0)));
			Assert.assertFalse(isCoordinateInBoundingBox(new Coordinate(33.1, 110.1), new BoundingBoxImp(50.0, 30.0, -179.0, 177.0)));
		}
		
		@Test
		public void testGetLongitudeInDomain(){
			Assert.assertEquals(-179.5, getLongitudeInDomain(179 + 1.5), 0.0001);
			Assert.assertEquals(175.5, getLongitudeInDomain(-179 - 5.5), 0.0001);
			Assert.assertEquals(171.5, getLongitudeInDomain(170 + 1.5), 0.0001);
		}
	}
	
}
