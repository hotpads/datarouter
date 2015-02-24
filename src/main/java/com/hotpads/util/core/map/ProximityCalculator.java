package com.hotpads.util.core.map;

import org.junit.Assert;
import org.junit.Test;



public class ProximityCalculator {
	
	public static final int BOUNDING_BOX_INCIRCLE = 4;
	public static final int BOUNDING_BOX_CIRCUMCIRCLE = 5;
	public static final int BOUNDING_BOX_AVERAGE = 6;
	
	public static final double KM_PER_MILE = 1.609344;
	public static final double EQUATORIAL_RADIUS_KM = GridTool.EQUATORIAL_RADIUS_M/1000;
	public static final double RAD_TO_DEG = 180/Math.PI;
	
	public static double milesToMeters(double miles) {
		return miles * KM_PER_MILE * 1000;
	}
	
	public static double metersToMiles(double meters) {
		return (meters / 1000) / KM_PER_MILE;
	}
	
	/**
	 * returns the bounding box centered at the given lat lon and having an average radius roughly equal to
	 * the given radius (yielding roughly the same area). 
	 * @param lat - latitude in degrees
	 * @param lon - longitude in degrees
	 * @param radiusInMiles - radius in miles
	 * @return
	 */
	public static BoundingBox getBoundingBoxForRadius(double lat, double lon, double radiusInMiles){
		return getBoundingBoxForRadius(lat, lon, radiusInMiles, BOUNDING_BOX_AVERAGE);
	}
	
	/**
	 * returns the bounding box centered at the given lat lon and having a diameter determined by the given
	 * radius and the type of conversion specified:
	 * incirle: returns the smallest bounding box containing the radius
	 * circumcircle: returns the largest bounding box contained by the radius
	 * average: returns the bounding box roughly equal in size to the circle
	 * @param lat
	 * @param lon
	 * @param radiusInMiles
	 * @param boxType - see bounding box types in ProximityCalculator
	 * @return
	 */
	public static BoundingBox getBoundingBoxForRadius(double lat, double lon, double radiusInMiles, int boxType){
		double halfSideLength;
		
		if (boxType == BOUNDING_BOX_CIRCUMCIRCLE){
			halfSideLength = KM_PER_MILE * radiusInMiles * 0.7071; // 1/sqrt(2) (for square inside circle)
		} else if (boxType == BOUNDING_BOX_INCIRCLE) {
			halfSideLength = KM_PER_MILE * radiusInMiles; 
		} else {
			halfSideLength = KM_PER_MILE * radiusInMiles * 0.8862; // sqrt(pi)/2 (for ~equal area rect and circle)
		}
		double degsOfLatFromCenter = (halfSideLength / EQUATORIAL_RADIUS_KM) * RAD_TO_DEG;
		double degsOfLonFromCenter = (halfSideLength / (EQUATORIAL_RADIUS_KM * Math.cos(lat * Math.PI / 180))) * RAD_TO_DEG;
		
		BoundingBox b = new BoundingBoxImp();
		b.setMaxLat(lat + degsOfLatFromCenter);
		b.setMinLat(lat - degsOfLatFromCenter);
		b.setMaxLon(BoundingBoxImp.getLongitudeInDomain(lon + degsOfLonFromCenter));
		b.setMinLon(BoundingBoxImp.getLongitudeInDomain(lon - degsOfLonFromCenter));
		return b;
	}
	
	public static double getMaxRadiusForBoundingBox(BoundingBox b){
		Coordinate topLeft = new Coordinate(b.getMaxLat(), b.getMinLon());
		Coordinate bottomRight = new Coordinate(b.getMinLat(), b.getMaxLon());
		return distanceBetween(topLeft, bottomRight) / 2;
	}
	
	/**
	 * @param q1
	 * @param q2
	 * @return miles between quads
	 */
	public static Double distanceBetweenQuads(String q1, String q2){
		if(q1 == null || q2 == null){
			return null;
		}
		return distanceBetween(GridTool.getCoordinateForQuadCode(q1),
				GridTool.getCoordinateForQuadCode(q2));
	}
	
	/**
	 * @param c1
	 * @param c2
	 * @return miles between coordinates
	 */
	public static double distanceBetween(Coordinate c1, Coordinate c2){
		/*haversine formula from http://www.movable-type.co.uk/scripts/GIS-FAQ-5.1.html
		dlon = lon2 - lon1
		dlat = lat2 - lat1
		a = sin^2(dlat/2) + cos(lat1) * cos(lat2) * sin^2(dlon/2)
		c = 2 * arcsin(min(1,sqrt(a)))
		d = R * c
		*/
		double R = EQUATORIAL_RADIUS_KM;
		double dlon = (c2.getLon() - c1.getLon()) * Math.PI / 180;  //convert to radians
		double dlat = (c2.getLat() - c1.getLat()) * Math.PI / 180;
		double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
			        Math.cos(c2.getLat() * Math.PI / 180) * Math.cos(c2.getLat() * Math.PI / 180) * 
			        Math.sin(dlon/2) * Math.sin(dlon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double km = R * c;
		return km/KM_PER_MILE;
	}
	
	public static class Tests{
		@Test public void testBoundingBoxForRadius(){
			BoundingBox box0 =  getBoundingBoxForRadius(40, -90, 5.0, BOUNDING_BOX_INCIRCLE);
			BoundingBox box1 =  getBoundingBoxForRadius(40, -90, 5.0, BOUNDING_BOX_AVERAGE);
			BoundingBox box2 =  getBoundingBoxForRadius(40, -90, 5.0, BOUNDING_BOX_CIRCUMCIRCLE);
			double[] a = {0.04, 0.06, 0.07, 0.08};
			double[] b = {0.05, 0.07, 0.09, 0.11};
			Coordinate c0 = new Coordinate(40 + a[0], -90.0);
			Coordinate c1 = new Coordinate(40 + a[1], -90.0);
			Coordinate c2 = new Coordinate(40 + a[2], -90.0);
			Coordinate c3 = new Coordinate(40 + a[3], -90.0);
			Coordinate c4 = new Coordinate(40.0, -90 - b[0]);
			Coordinate c5 = new Coordinate(40.0, -90 - b[1]);
			Coordinate c6 = new Coordinate(40.0, -90 - b[2]);
			Coordinate c7 = new Coordinate(40.0, -90 - b[3]);
			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c0, box0));
			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c0, box1));
			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c0, box2));
//			Assert.assertFalse(GridTool.isCoordinateInBoundingBox(c1, box0));
			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c1, box1));
//			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c1, box2));
//			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c2, box0));
			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c2, box1));
//			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c2, box2));
			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c3, box0));
			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c3, box1));
			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c3, box2));
			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c4, box0));
			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c4, box1));
			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c4, box2));
//			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c5, box0));
			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c5, box1));
//			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c5, box2));
//			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c6, box0));
			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c6, box1));
//			Assert.assertTrue(GridTool.isCoordinateInBoundingBox(c6, box2));
			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c7, box0));
			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c7, box1));
			Assert.assertTrue(!GridTool.isCoordinateInBoundingBox(c7, box2));
		}
	}

	public static Coordinate quadToCoordinate(SQuad quad) {
		String quadAsString = quad.toMicrosoftStyleString();
		Coordinate listingCoordinate = GridTool.getCoordinateForQuadCode(quadAsString);
		return listingCoordinate;
	}

}
