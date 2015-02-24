package com.hotpads.util.core.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.GenericsFactory;

// http://msdn2.microsoft.com/en-us/library/aa907679.aspx

public class GridTool {
	protected static final Logger logger = LoggerFactory.getLogger(GridTool.class);
	
	public static Integer MAX_LEVEL = 19 + 8;  //where level 0 has the entire earth in 1 pixel

    public static final double EQUATORIAL_RADIUS_M = 6378137;
    //public static final double EQUATORIAL_RADIUS = 6371000;  //this is in ArcMap for the Sphere_Mercator projection.  
    public static final double EQUATORIAL_CIRCUMFERENCE_M = EQUATORIAL_RADIUS_M * 2 * Math.PI;
  	public static final double EQUATORIAL_HALF_CIRC_M = EQUATORIAL_CIRCUMFERENCE_M / 2;

  	public static final double MAX_REASONABLE_LAT = 89.999;
  	
  	/*  MS and Google maps do not use this radius.  they treat the earth as a sphere
    public static final double POLAR_RADIUS = 6356752.3142;
    public static final double POLAR_CIRCUMFERENCE = POLAR_RADIUS * 2 * Math.PI;
    public static final double POLAR_HALF_CIRC = POLAR_CIRCUMFERENCE / 2;
    public static final double FLATTENING = (EQUATORIAL_RADIUS - POLAR_RADIUS) / EQUATORIAL_RADIUS;
    */
    
    //methods for dealing with grid size and zoom level
    
  	final public static long getCircumference(int level){
    	return (1 << level);
    }
    
    public static int getLevelForCircumference(long circumference){//why use long instead of double?
    	long numPixels = circumference / 2;
    	int level = 1;
    	while(numPixels > 1){
    		++level;
    		numPixels = numPixels >> 1;
    	}
    	return level;
    }
    
    final public static double getRadius(int level){
    	return getCircumference(level) / (2 * Math.PI);
    }

    final public static int getLevel(String quadCode){
		return quadCode.length();
	}
    	
    final public static Pixel getPixelAtLevel(Pixel startPixel, int endLevel){
		int startLevel = startPixel.getLevel();
		int levelsUp = endLevel - startLevel;
		long endX, endY;
		if (levelsUp >= 0){
			endX = startPixel.getX() << levelsUp;
			endY = startPixel.getY() << levelsUp;			
		} else {
			endX = startPixel.getX() >> Math.abs(levelsUp);
			endY = startPixel.getY() >> Math.abs(levelsUp);			
		}
		Pixel p = new Pixel(endX, endY, endLevel);
		return p;
	}
    
    final public static String getParentQuad(String quad) {
    	if (quad==null || quad.length()==0 || "q".equals(quad)) {
    		logger.error("Tried to get parent of quad="+quad);
    		Thread.dumpStack();
    		return quad;
    	}
    	return quad.substring(0, quad.length() - 1);
    }
    
    final public static List<String> getParentQuads(String quad){
    	if(quad.length() == 1){
    		return new ArrayList<String>();
    	}else{
    		List<String> parentQuads = new ArrayList<String>(quad.length() - 1);
    		for(int i=0; i < quad.length()-1; ++i){
    			parentQuads.add(quad.substring(0, i+1));
    		}
    		return parentQuads;
    	}
    }
    
    public static List<String> subtractQuads(String largerQuad, String smallerQuad){
    	if(largerQuad.length() >= smallerQuad.length()){
    		throw new IllegalArgumentException("Cannot subtract " + largerQuad + " from " + smallerQuad);
    	}
    	List<String> result = new ArrayList<String>();
    	for(String subQuad : getChildQuadsAtLevel(largerQuad, smallerQuad.length())){
    		if(!smallerQuad.startsWith(subQuad)){
    			result.add(subQuad);
    		}
    	}
    	result = reduceQuads(result);
    	return result;
    }
//    public static List<Quad> subtractQuads(Quad largerQuad, Quad smallerQuad){
//    	if(largerQuad.getLevel() >= smallerQuad.getLevel()){
//    		throw new IllegalArgumentException("Cannot subtract " + largerQuad + " from " + smallerQuad);
//    	}
//    	List<Quad> result = new ArrayList<Quad>();
//    	for(Quad subQuad : getChildQuadsAtLevel(largerQuad, smallerQuad.getLevel())){
//    		if(!smallerQuad.isChildOfOrEqualTo(subQuad)){
//    			result.add(subQuad);
//    		}
//    	}
//    	result = reduce(result);
//    	return result;
//    }
    public static List<SQuad> subtractQuads(SQuad largerQuad, SQuad smallerQuad){
    	if(largerQuad.getLevel() >= smallerQuad.getLevel()){
    		throw new IllegalArgumentException("Cannot subtract " + largerQuad + " from " + smallerQuad);
    	}
    	List<SQuad> result = new ArrayList<SQuad>();
    	for(SQuad subQuad : getChildQuadsAtLevel(largerQuad, smallerQuad.getLevel())){
    		if(!smallerQuad.isChildOfOrEqualTo(subQuad)){
    			result.add(subQuad);
    		}
    	}
    	result = reduce(result);
    	return result;
    }
    
    public static List<String> reduceQuads(List<String> quads){
    	//could be improved
    	List<String> result = new ArrayList<String>(quads);
    	for(String quad : quads){
    		if(result.contains(getParentQuad(quad))){
    			result.remove(quad);
    		}
    		List<String> siblingsAndSelf = getChildQuads(getParentQuad(quad));
    		if(result.containsAll(siblingsAndSelf)){
    			if(!result.contains(getParentQuad(quad))){
    				result.add(getParentQuad(quad));
    			}
    			result.removeAll(siblingsAndSelf);
    		}
    	}    	
    	return result;
    }
//    public static List<Quad> reduce(List<Quad> quads){
//    	//could be improved
//    	List<Quad> result = new ArrayList<Quad>(quads);
//    	for(Quad quad : quads){
//    		if(result.contains(quad.getParent())){
//    			result.remove(quad);
//    		}
//    		List<Quad> siblingsAndSelf = quad.getParent().getChildren();
//    		if(result.containsAll(siblingsAndSelf)){
//    			if(!result.contains(quad.getParent())){
//    				result.add(quad.getParent());
//    			}
//    			result.removeAll(siblingsAndSelf);
//    		}
//    	}    	
//    	return result;
//    }
    public static List<SQuad> reduce(List<SQuad> quads){
    	//could be improved
    	List<SQuad> result = new ArrayList<SQuad>(quads);
    	for(SQuad quad : quads){
    		if(result.contains(quad.getParent())){
    			result.remove(quad);
    		}
    		List<SQuad> siblingsAndSelf = quad.getParent().getChildren();
    		if(result.containsAll(siblingsAndSelf)){
    			if(!result.contains(quad.getParent())){
    				result.add(quad.getParent());
    			}
    			result.removeAll(siblingsAndSelf);
    		}
    	}    	
    	return result;
    }
    
    public static List<String> getChildQuadsAtLevel(String quad, Integer deepestLevel){
//    	if(quad.length() >= deepestLevel){ return Arrays.asList(new String[]{quad}); }
//    	Collection<String> children = new HashSet<String>(getChildQuads(quad));
//		if(deepestLevel.equals(CollectionTool.getFirst(children).length())){
//			return children;
//		}else{
//			Collection<String> lastChildren = new HashSet<String>(children);
//			children.clear()
//			for(String lastChild : lastChildren){
//				children.addAll(getChildQuads(lastChild));
//			}
//		}
    	
    	//better than recursion
    	String baseQuad = (quad==null?"":quad);
    	int levelDifference = deepestLevel-baseQuad.length();
    	int numChildrenAtLevel = 1 << (2*levelDifference);
    	List<String> childrenAtLevel = new ArrayList<String>(numChildrenAtLevel);
    	for(int i=0; i < numChildrenAtLevel; ++i){
    		StringBuilder suffix = new StringBuilder();
    		for(int j=levelDifference-1; j >= 0; --j){
    			suffix.append((i>>(2*j))%4);
    		}
    		childrenAtLevel.add(baseQuad + suffix.toString());
    	}
    	return childrenAtLevel;
    }
    
    public static List<Quad> getChildQuadsAtLevel(Quad quad, Integer deepestLevel){
    	//TODO:don't use strings to do the work
    	Quad baseQuad = (quad==null?new Quad():quad);
    	int levelDifference = deepestLevel-baseQuad.getLevel();
    	int numChildrenAtLevel = 1 << (2*levelDifference);
    	List<Quad> childrenAtLevel = new ArrayList<Quad>(numChildrenAtLevel);
    	for(int i=0; i < numChildrenAtLevel; ++i){
    		StringBuilder suffix = new StringBuilder();
    		for(int j=levelDifference-1; j >= 0; --j){
    			suffix.append((i>>(2*j))%4);
    		}
    		childrenAtLevel.add(new Quad(baseQuad + suffix.toString()));
    	}
    	return childrenAtLevel;
    }

    public static List<SQuad> getChildQuadsAtLevel(SQuad quad, Integer deepestLevel){
    	//TODO:don't use strings to do the work
    	SQuad baseQuad = (quad==null?new SQuad():quad);
    	int levelDifference = deepestLevel-baseQuad.getLevel();
    	int numChildrenAtLevel = 1 << (2*levelDifference);
    	List<SQuad> childrenAtLevel = new ArrayList<SQuad>(numChildrenAtLevel);
    	for(int i=0; i < numChildrenAtLevel; ++i){
    		StringBuilder suffix = new StringBuilder();
    		for(int j=levelDifference-1; j >= 0; --j){
    			suffix.append((i>>(2*j))%4);
    		}
    		childrenAtLevel.add(new SQuad(baseQuad + suffix.toString()));
    	}
    	return childrenAtLevel;
    }
    
    @Deprecated //new tilecache does not use
    public static int getCdnBucketForQuadCode(String quad) {
		Pixel p = GridTool.getPixelForQuadCode(quad);
		double gridHeight = Math.pow(2, p.getLevel());
        long y = (long)gridHeight - p.getY() - 1;

		int maptileCdnBucket;
		if (p.getX() % 2 == 0 && y % 2 == 0) {
			maptileCdnBucket = 0;
		} else if (p.getX() % 2 == 0 && y % 2 == 1) {
			maptileCdnBucket = 1;
		} else if (p.getX() % 2 == 1 && y % 2 == 0) {
			maptileCdnBucket = 2;
		} else {
			maptileCdnBucket = 3;
		}

		return maptileCdnBucket;
    }
    
    /**
     * determine the top left pixel for a given lat/lon bounding box that will fit within the given pixel
     * dimensions (calculated from the max dims of the map minus 2*the padding) 
     * @param b
     * @param maxWidth
     * @param maxHeight
     * @param horizontalPadding
     * @param verticalPadding
     * @return
     */
    public static Pixel getTopLeftPixel(BoundingBox b, int maxWidth, int maxHeight, 
    													int horizontalPadding, int verticalPadding){
    	int maxInnerWidth = maxWidth - 2 * horizontalPadding;
    	int maxInnerHeight = maxHeight - 2 * verticalPadding;
    	
    	Coordinate upperLeftCoord = new Coordinate(b.getMaxLat(), b.getMinLon());
    	Coordinate lowerRightCoord = new Coordinate(b.getMinLat(), b.getMaxLon());
    	Coordinate centerCoord = b.getCenter();
    	Pixel upperLeftPixel = getPixelForCoordinateAtLevel(upperLeftCoord, MAX_LEVEL);
    	Pixel lowerRightPixel = getPixelForCoordinateAtLevel(lowerRightCoord, MAX_LEVEL);
    	long width = lowerRightPixel.getX() - upperLeftPixel.getX();
    	long height = lowerRightPixel.getY() - upperLeftPixel.getY();
    	
    	int level;
    	if (width <= maxInnerWidth && height <= maxInnerHeight){
    		level = MAX_LEVEL;
    	} else {
    		double dWidth = width;
    		double dHeight = height;
    		double logWidth = Math.log(dWidth/maxInnerWidth) / Math.log(2);
    		double logHeight = Math.log(dHeight/maxInnerWidth) / Math.log(2);
    		Double dLevel = MAX_LEVEL - Math.ceil(Math.max(logWidth, logHeight)); 
    		level = dLevel.intValue();
    	}
    	
    	Pixel centerPixel = getPixelForCoordinateAtLevel(centerCoord, level);
    	long topLeftX = centerPixel.getX() - (maxWidth / 2);
    	long topLeftY = centerPixel.getY() - (maxHeight / 2);
    	
    	return new Pixel(topLeftX, topLeftY, level);
    }
    
    public static List<String> getChildQuads(final String quad){
    	String quadCopy = (quad==null?"":quad);
    	return Arrays.asList(new String[]{quadCopy+"0",quadCopy+"1",quadCopy+"2",quadCopy+"3"});
    }
	
	//simple radians/degrees converters
	
	final public static double radiansToDegrees(double radians){
        return radians / Math.PI * 180.0;
    }
    
    final public static double degreesToRadians(double degrees){
    	return degrees * Math.PI / 180.0;
    }
	
	// methods to convert between lat/lon and y/x
	
    final public static double getLatitudeForYAtLevel(long y, int level){    	
        double metersPerPixel = EQUATORIAL_CIRCUMFERENCE_M / getCircumference(level);
        double meters = EQUATORIAL_HALF_CIRC_M - (y * metersPerPixel);
        double pixelRadiansFromOrigin = Math.exp(meters * 2 / EQUATORIAL_RADIUS_M);
        double radiansFromOrigin = Math.asin((pixelRadiansFromOrigin - 1) / (pixelRadiansFromOrigin + 1));
        return radiansToDegrees(radiansFromOrigin);
    }
    
    final public static long getYForLatitudeAtLevel(double lat, int level){
    	if(lat > MAX_REASONABLE_LAT){
    		lat = MAX_REASONABLE_LAT;
    	}
    	if(lat < -MAX_REASONABLE_LAT){
    		lat = -MAX_REASONABLE_LAT;
    	}
        double latRadians = degreesToRadians(lat);
        double y = getRadius(level)/2.0 * 
                Math.log( (1.0 + Math.sin(latRadians)) /
                          (1.0 - Math.sin(latRadians)) );
        return (long)(.5 * getCircumference(level) - y);
    }
    
    final public static double getLongitudeForXAtLevel(long x, int level){
        long totalPixels = getCircumference(level);
        double percentFromLeft = x / (double)totalPixels;
        return (percentFromLeft * 360) - 180;
    }
    
    final public static long getXForLongitudeAtLevel(double lon, int level){
        long totalPixels = getCircumference(level);
        double degreesFromLeft = 180 + lon;
        double percentRight = degreesFromLeft / 360;
        long x = (long)(totalPixels * percentRight);
        return x;
    }
	
    final public static Pixel getPixelForCoordinateAtLevel(Coordinate c, int level){
		long x = getXForLongitudeAtLevel(c.getLon(), level);
		long y = getYForLatitudeAtLevel(c.getLat(), level);
		return new Pixel(x, y, level);
	}
	
    final public static Coordinate getCoordinateFromPixel(Pixel p){
		double lon = getLongitudeForXAtLevel(p.getX(), p.getLevel());
		double lat = getLatitudeForYAtLevel(p.getY(), p.getLevel());
		return new Coordinate(lat, lon);
	}
    
    public static final boolean isCoordinateInBoundingBox(Coordinate c, BoundingBox b){
    	return BoundingBoxImp.isCoordinateInBoundingBox(c, b);
    }
    
    //methods for calculating and decoding sector names
    
    /**
     * get the quad to the west of the given quad on the same level
     */
    final public static String getQuadWest(String quadCode) {
    	Character lastChar = quadCode.charAt(quadCode.length()-1);
		if(lastChar == '0'){ //Top Left of current quad
			return getQuadWest(getParentQuad(quadCode)) + "1";
		}
		else if(lastChar == '1'){ //Top Right of current quad
			return getParentQuad(quadCode) + "0";
		}
		else if(lastChar == '2'){ //Bottom Left of current quad
			return getQuadWest(getParentQuad(quadCode)) + "3";
		}
		else if(lastChar == '3'){ //Bottom Right of current quad
			return getParentQuad(quadCode) + "2";
		}
		
		return null; //should not reach
    }
    
    /**
     * get the quad to the north of the given quad on the same level
     */
    final public static String getQuadNorth(String quadCode) {
    	Character lastChar = quadCode.charAt(quadCode.length()-1);
		if(lastChar == '0'){ //Top Left of current quad
			return getQuadNorth(getParentQuad(quadCode)) + "2";
		}
		else if(lastChar == '1'){ //Top Right of current quad
			return getQuadNorth(getParentQuad(quadCode)) + "3";
		}
		else if(lastChar == '2'){ //Bottom Left of current quad
			return getParentQuad(quadCode) + "0";
		}
		else if(lastChar == '3'){ //Bottom Right of current quad
			return getParentQuad(quadCode) + "1";
		}
		
		return null; //should not reach   	
    }
    
    /**
     * get the quad to the east of the given quad on the same level
     */
    final public static String getQuadEast(String quadCode) {
    	Character lastChar = quadCode.charAt(quadCode.length()-1);
		if(lastChar == '0'){ //Top Left of current quad
			return getParentQuad(quadCode) + "1";
		}
		else if(lastChar == '1'){ //Top Right of current quad
			return getQuadEast(getParentQuad(quadCode)) + "0";
		}
		else if(lastChar == '2'){ //Bottom Left of current quad
			return getParentQuad(quadCode) + "3";
		}
		else if(lastChar == '3'){ //Bottom Right of current quad
			return getQuadEast(getParentQuad(quadCode)) + "2";
		}
		
		return null; //should not reach   	
    }
    
    /**
     * get the quad to the south of the given quad on the same level
     */
    final public static String getQuadSouth(String quadCode) {
    	Character lastChar = quadCode.charAt(quadCode.length()-1);
		if(lastChar == '0'){ //Top Left of current quad
			return getParentQuad(quadCode) + "2";
		}
		else if(lastChar == '1'){ //Top Right of current quad
			return getParentQuad(quadCode) + "3";
		}
		else if(lastChar == '2'){ //Bottom Left of current quad
			return getQuadSouth(getParentQuad(quadCode)) + "0";
		}
		else if(lastChar == '3'){ //Bottom Right of current quad
			return getQuadSouth(getParentQuad(quadCode)) + "1";
		}
		
		return null; //should not reach   	
    }
    
    /**
     * get the quad to the southeast of the given quad on the same level
     */
    final public static String getQuadSouthEast(String quadCode) {
    	return getQuadSouth(getQuadEast(quadCode));
    }
 
    /**
     * get the quad to the northeast of the given quad on the same level
     */
    final public static String getQuadNorthEast(String quadCode) {
    	return getQuadNorth(getQuadEast(quadCode));
    }
    
    /**
     * get the quad to the southwest of the given quad on the same level
     */
    final public static String getQuadSouthWest(String quadCode) {
    	return getQuadSouth(getQuadWest(quadCode));
    }
    
    /**
     * get the quad to the northwest of the given quad on the same level
     */
    final public static String getQuadNorthWest(String quadCode) {
    	return getQuadNorth(getQuadWest(quadCode));
    }
    
    final public static List<String> getSurroundingQuads(String quadCode){
    	List<String> qs = GenericsFactory.makeArrayListWithCapacity(8);
    	qs.add(getQuadNorth(quadCode));
    	qs.add(getQuadNorthEast(quadCode));
    	qs.add(getQuadEast(quadCode));
    	qs.add(getQuadSouthEast(quadCode));
    	qs.add(getQuadSouth(quadCode));
    	qs.add(getQuadSouthWest(quadCode));
    	qs.add(getQuadWest(quadCode));
    	qs.add(getQuadNorthWest(quadCode));
    	return qs;
    }
    
    final public static Pixel getPixelForQuadCode(String quadCode){		
		boolean bottom = false;  //top
		boolean right = false;  //left
		
		long yQuad = 0;
		long xQuad = 0;
		
		for(int level=0; level < quadCode.length(); ++level){
			Character c = quadCode.charAt(level);
			if(c == '0'){ bottom=false; right=false; }
			if(c == '1'){ bottom=false; right=true; }
			if(c == '2'){ bottom=true; right=false; }
			if(c == '3'){ bottom=true; right=true; }
			
			long granularity = getCircumference(quadCode.length() - (level + 1));
			
			if(bottom){ yQuad += granularity; }
			if(right){ xQuad += granularity; }
		}
		
		return new Pixel(xQuad, yQuad, quadCode.length());
	}
	
    public static String getLastCommonAncestorQuad(List<String> quadCodes){
    	int index = 0;
    	if (CollectionTool.isEmpty(quadCodes)) return "";
    	
    	String quadCode = quadCodes.get(0);
    	for (index = 0; index < quadCode.length(); index++){
    		Character c = quadCode.charAt(index);
    		Boolean success = true;
    		for (String qc : quadCodes){
    			if (qc.length() <= index || !c.equals(qc.charAt(index))){
    				success = false;
    				break;
    			}
    		}
    		if (!success) break;
    	}
		return quadCode.substring(0, index);
    }
	
	public static BoundingBox getBoundingBoxForCoordinates(Collection<Coordinate> coords){
		BoundingBoxImp b = new BoundingBoxImp();
		if (CollectionTool.isEmpty(coords)){
			return null;
		}
		for (Coordinate c : coords){
			b.growToFit(c);
		}
		return b;
	}	
	
	public static BoundingBox getCenteredBoundingBoxForCoordinate(Coordinate coord, int height, int width, int level) {
		BoundingBoxImp b = new BoundingBoxImp();
		Pixel p = GridTool.getPixelForCoordinateAtLevel(coord, level);
		b.setMaxLat(GridTool.getLatitudeForYAtLevel(p.y - height/2, level));
		b.setMinLat(GridTool.getLatitudeForYAtLevel(p.y + height/2, level));
		b.setMaxLon(GridTool.getLongitudeForXAtLevel(p.x + width/2, level));
		b.setMinLon(GridTool.getLongitudeForXAtLevel(p.x - width/2, level));
		return b;
	}
	
    final public static Coordinate getCoordinateForQuadCode(String quadCode){
		Pixel sc = getPixelForQuadCode(quadCode);
		int level = quadCode.length();
		double lat = getLatitudeForYAtLevel(sc.getY(), level);
		double lon = getLongitudeForXAtLevel(sc.getX(), level);
		return new Coordinate(lat, lon);
	}
	
    final public static String getQuadCode(Pixel p){
		int pLevel = p.getLevel();
		long x = p.getX();
		long y = p.getY();
		
		boolean[] xSides = new boolean[pLevel];
		boolean[] ySides = new boolean[pLevel];
		
		for(int i=pLevel; i > 0; --i){
			xSides[i-1] = (x & 1) == 1;
			ySides[i-1] = (y & 1) == 1;
			x = x >> 1;
			y = y >> 1;
		}
		
		char[] levelChars = new char[pLevel];
		for(int i=0; i < pLevel; ++i){
			boolean xOver = xSides[i];
			boolean yOver = ySides[i];
			char levelChar = 'n';
			if(xOver && yOver){ levelChar = '3'; } 
			if(xOver && !yOver){ levelChar = '1'; }
			if(!xOver && yOver){ levelChar = '2'; }
			if(!xOver && !yOver){ levelChar = '0'; }
			if(levelChar < '0' || levelChar > '3'){throw new RuntimeException("error calculating grid square");}
			levelChars[i] = levelChar;
			
		}
		return new String(levelChars);
	}
	
    final public static String getQuadCodeAtLevel(Coordinate c, int level){
		Pixel p = getPixelForCoordinateAtLevel(c, level);
		String quadCode = getQuadCode(p);
		return quadCode;
	}
	
    final public static String getQuadCodeAtLevel(Pixel pixel, int level){
		Pixel p = getPixelAtLevel(pixel, level);
		String quadCode = getQuadCode(p);
		return quadCode;
	}

    final public static String getQuadCodeAtLevel(String quadCode, int newLevel){
		int currentLevel = quadCode.length();
		int extraLevels = newLevel - currentLevel;
		if(extraLevels >= 0){
			StringBuilder qcBuilder = new StringBuilder();
			qcBuilder.append(quadCode);
			for(int i=0; i < currentLevel; ++ i){
				qcBuilder.append("0");
			}
			return qcBuilder.toString();
		}else{
			return quadCode.substring(0, newLevel);
		}
	}
	
	public static boolean isValidQuad(String quad){
		for(int i=0; i < quad.length(); ++i){
			Character ch = quad.charAt(i);
			if(ch.equals("0") || ch.equals("1") || ch.equals("2") || ch.equals("3")){
				return false;
			}
		}
		return true;
	}
	
	public static int getPixelWidthOnScreen(String quad, Integer zoom){
		return 1 << (zoom - quad.length());
	}
	
	public static String getParentQuad(List<String> children){
		int maxLength = GridTool.MAX_LEVEL;
		for(String quad : children){
			if(quad.length() < maxLength){
				maxLength = quad.length();
			}
		}

		int i=0;
		boolean done = false;
		for(i=0; i < maxLength; ++i){
			for(String quad : children){
				if(children.get(0).charAt(0) != quad.charAt(0)){
					done = true;
					break;
				}
				if(done){ break; }
			}
		}
		return children.get(0).substring(0, i-1);
	}
	
	public static class Tests{
		@Test public void testGetLastCommonAncestorQuad(){
			List<String> quadList1 = Lists.newArrayList("1","1","12","123");
			List<String> quadList2 = Lists.newArrayList("","1","12","123");
			List<String> quadList3 = Lists.newArrayList("1234","123","123","123");
			List<String> quadList4 = Lists.newArrayList("1","10","12","123");
			List<String> quadList5 = Lists.newArrayList("1","10","","123");
			Assert.assertEquals("1", getLastCommonAncestorQuad(quadList1));
			Assert.assertEquals("", getLastCommonAncestorQuad(quadList2));
			Assert.assertEquals("123", getLastCommonAncestorQuad(quadList3));
			Assert.assertEquals("1", getLastCommonAncestorQuad(quadList4));
			Assert.assertEquals("", getLastCommonAncestorQuad(quadList5));
		}
		
		@Test public void testGetLevelWithBoundingBoxSmallerThanPixelWidthAndHeight(){
			BoundingBox b = new BoundingBoxImp(38.229550455326134, 
											   37.33959185135918, 
											   -121.10504150390625, 
											   -123.739013671875);
			int level1 = getTopLeftPixel(b, 1960, 825, 0, 0).getLevel();
			int level2 = getTopLeftPixel(b, 1960, 825, 25, 25).getLevel();
			int level3 = getTopLeftPixel(b, 2010, 875, 25, 25).getLevel();
			Assert.assertEquals(18, level1);
			Assert.assertTrue(18 > level2);
			Assert.assertEquals(18, level3);
		}
		
		@Test public void testCircumference(){
			Assert.assertEquals(1, getCircumference(0));
		}
		@Test public void testBasics(){
			Pixel p = GridTool.getPixelForQuadCode("0");
			Assert.assertEquals(0, p.getX());
			Assert.assertEquals(0, p.getY());
			
			Pixel p2 = GridTool.getPixelForQuadCode("01");
			Assert.assertEquals(1, p2.getX());
			Assert.assertEquals(0, p2.getY());
			Assert.assertEquals(2, p2.level);
		}
		
		@Test public void testGetQuadCode(){
			String qc1 = "0123012301230123"; 
			Pixel p1 = new Pixel(qc1);
			Pixel p2 = new Pixel(512,512,10);
			Assert.assertEquals(qc1, getQuadCode(p1));
			Assert.assertEquals("3000000000", getQuadCode(p2));
		}
	}

}
