package com.hotpads.util.core.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.LongTool;
import com.hotpads.util.core.RuntimeTool;
import com.hotpads.util.core.StringTool;


/*
 * highly optimized class
 * 
 * stored in 64 bit primitive long
 * 
 * 3 leftmost bits are blank.  next 5 bits represent the level which can be 0 through 28.  
 *
 * we should have 3 extra bits to play with
 * 
 * the 56 remaining "grid" bits are right justified so that the "bits" field can also be used as an efficient hashCode
 * 
 * y is the direction that is shifted left one bit
 * 
 * natural ordering is the same as ordering Microsoft style quad strings
*/

@SuppressWarnings("serial")
public class Quad implements Comparable<Quad>, Serializable{

	private static final long ONE_MASK = 1l;
	private static final long TWO_MASK = 2l;
	public static final String TOP_QUAD_STRING = "q";
	public static final Quad TOP_QUAD = new Quad();
	
	/********************************************* static ****************************************/
	public static final int NUM_BITS_IN_LONG = 64;
	public static final int NUM_LEVELS_AFTER_ZERO = 28;  //there's also level 0 which has an empty quad code
	public static final int MAX_LEVEL = NUM_LEVELS_AFTER_ZERO;
	public static final int NUM_GRID_BITS = 28 * 2;  //one bit for each of x and y on each level
	public static final int NUM_LEVEL_BITS = 5; //basically 28<<5
	
	public static final long ZERO_LEVEL_BITS_WITH_ALL_GRID_BITS = LongTool.setRightBits(NUM_GRID_BITS);
	public static final long MAX_LEVEL_BITS_WITH_NO_GRID_BITS = 28L << NUM_GRID_BITS; 
	


//	public static final int SIXTEEN_ONES = 0xffff;
	public static final String EIGHT_SPACE_STRING = "        ";
	public static final String SIXTEEN_SPACE_STRING = EIGHT_SPACE_STRING + EIGHT_SPACE_STRING;
	public static final String EIGHT_ZERO_STRING = "00000000";
	public static final String SIXTEEN_ZERO_STRING = EIGHT_ZERO_STRING + EIGHT_ZERO_STRING;
	

	/*************************************** fields ******************************************************/
	
	protected long bits;
	

	/*************************************** constructors *************************************************/
	
	public Quad(){
		this(TOP_QUAD_STRING);
	}
	
	public Quad(long bits){
		this.bits = bits;
	}
	
	public Quad(SQuad squad){
		this(squad.getLevel(), squad.getX(), squad.getY());
	}
	
	public Quad(long level, long x, long y){
		//shift level
		long shiftedLevel = level << NUM_GRID_BITS;
		
		//split x into left and right HALF_GRID_BITS(14) bits because that's the width of our dilation lookup table
		long rightPartOfX = x & DilatedBitTool28.TABLE_MASK;  //x and'd with 14 1's
		long dilatedRightPartOfX = DilatedBitTool28.DILATED_TABLE[(int)rightPartOfX];
		long leftPartOfX = x >>> DilatedBitTool28.HALF_GRID_BITS;
		long dilatedLeftPartOfX = DilatedBitTool28.DILATED_TABLE[(int)leftPartOfX];
		
		//do the same for y as x, but use the left shifted table
		long leftPartOfY = y >>> DilatedBitTool28.HALF_GRID_BITS;
		long dilatedLeftPartOfY = DilatedBitTool28.LEFT_SHIFTED_DILATED_TABLE[(int)leftPartOfY];
		long rightPartOfY = y & DilatedBitTool28.TABLE_MASK;
		long dilatedRightPartOfY = DilatedBitTool28.LEFT_SHIFTED_DILATED_TABLE[(int)rightPartOfY];
		bits = shiftedLevel 
				| dilatedLeftPartOfX << NUM_LEVELS_AFTER_ZERO 
				| dilatedRightPartOfX 
				| dilatedLeftPartOfY << NUM_LEVELS_AFTER_ZERO 
				| dilatedRightPartOfY;
	}
	
	/*
	 * for quads of the microsoft maps pattern below:
	 * 
	 *     01
	 *     23
	 */
	public Quad(String s){
		fromMicrosoftStyleString(s);
	}
	
	public static Quad valueOf(String s){
		return new Quad(s);
	}
	
	/*************************************** public methods *****************************************/

	//natural ordering is the same as ordering Microsoft style quad strings
	//We need to delete level bits, line up the grid bits, and then compare the grid bits
	public int compareTo(Quad other){
		
		//delete level bits
		long thisBitsToCompare = this.bits & ZERO_LEVEL_BITS_WITH_ALL_GRID_BITS;
		long otherBitsToCompare = other.bits & ZERO_LEVEL_BITS_WITH_ALL_GRID_BITS;
//		System.out.println("orig-> "+thisBitsToCompare+","+otherBitsToCompare);
		
		//line up grid bits
		int levelDiff = this.getLevel() - other.getLevel();
		if(levelDiff < 0){  //of this is deeper (smaller) than other
			thisBitsToCompare = thisBitsToCompare << (-2 * levelDiff);
		}else if(levelDiff > 0){
			otherBitsToCompare = otherBitsToCompare << (2 * levelDiff);
		}
//		System.out.println(thisBitsToCompare+","+otherBitsToCompare);
		
		//run the comparison with bits at equal levels
		//mcorgan - R.I.P. October 3rd, 2008
		if(thisBitsToCompare < otherBitsToCompare){ 
			return -1; 
		}else if(thisBitsToCompare > otherBitsToCompare){ 
			return 1; 
		}else if(levelDiff < 0){
			return -1;
		}else if(levelDiff > 0){
			return 1; 
		}else{  //must be equal
			return 0;
		}
	}
	
	//get the xxxxxxx333333333 quad on level 28
	public Quad getLastPossibleSubQuad(){
		int shift = 2 * (NUM_LEVELS_AFTER_ZERO - this.getLevel());
		long levelRemoved = this.bits & ZERO_LEVEL_BITS_WITH_ALL_GRID_BITS;
		long shifted = levelRemoved << shift;
		long lowerLevelsFilledIn = shifted | LongTool.setRightBits(shift);
		long maxLevelApplied = lowerLevelsFilledIn | MAX_LEVEL_BITS_WITH_NO_GRID_BITS;
		return new Quad(maxLevelApplied);
	}
	
	/********************************* factory methods *********************************************/
	
	public static List<Quad> fromMicrosoftStyleStrings(List<String> strings){
		List<Quad> quads = new ArrayList<Quad>(CollectionTool.sizeNullSafe(strings));
		for(String stringQuad : CollectionTool.nullSafe(strings)){
			quads.add(new Quad(stringQuad));
		}
		return quads;
	}
	
	public static List<String> getMicrosoftStyleStrings(List<Quad> quads){
		List<String> strings = new ArrayList<String>(CollectionTool.sizeNullSafe(quads));
		for(Quad quad : CollectionTool.nullSafe(quads)){
			strings.add(quad.toMicrosoftStyleString());
		}
		return strings;
	}
	
	/*************************************** useful methods *****************************************/
	public int getLevel(){
		return (int)(bits >>> NUM_GRID_BITS);
	}
	
	/*
	 * start at the right and move left through the x grid bits, adding them up each time.  not optimal
	 */
	public long getX(){
		long x = 0;
		for(short bit=0; bit < NUM_GRID_BITS; bit+=2){
			long mask = 1l << bit;
			long workingBit = bits & mask;
			long contractedWorkingBit = workingBit >>> (bit/2);
			x += contractedWorkingBit;
		}
		return x;
	}
	
	public long getY(){
//		System.out.println("   "+StringTool.pad(Long.toBinaryString(bits),'-',64));
		long y = 0;
		for(short bit=1; bit < NUM_GRID_BITS; bit+=2){
			long mask = 1l << bit;
//			System.out.println("   "+StringTool.pad(Long.toBinaryString(mask),'-',64));
			long workingBit = bits & mask;
			long contractedWorkingBit = workingBit >>> ((bit/2)+1);  //shift one extra right because y is offset 1 left
//			System.out.println(StringTool.pad(bit+"",' ',2)+" "+StringTool.pad(Long.toBinaryString(contractedWorkingBit),'-',64));
			y += contractedWorkingBit;
		}
		return y;
	}
	
	public boolean isXEven(){
		return (this.bits & ONE_MASK) != ONE_MASK;
	}
	
	public boolean isYEven(){
		return (this.bits & TWO_MASK) != TWO_MASK;
	}
	
	public Quad getParent(){
		return this.getAtLevel(this.getLevel()-1);
	}
	
	public void shiftLeft(){
		long distance = 0l;
		int currentBit = 0;
		while(((bits >> currentBit) & ONE_MASK) == 0){
			distance = distance << 1;			
			distance += 1;
			distance = distance << 1;
			currentBit+=2;
		}
		distance += 1;
		
		bits -= distance;
	}
	
	public void shiftRight(){
		long distance = 0l;
		int currentBit = 0;
		
		while(((bits >> currentBit) & ONE_MASK) == 1){
			distance = distance << 1;
			distance += 1;
			distance = distance << 1;
			currentBit+=2;
		}
		distance += 1;
		
		bits += distance;
	}
	
	public void shiftUp(){
		long oneMask = 1l;
		long distance = 0l;
		int currentBit = 1;
		
		while(((bits >> currentBit) & oneMask) == 0){
			distance = distance << 1;
			distance += 1;
			distance = distance << 1;
			currentBit+=2;
		}
		distance += 1;
		distance = distance << 1;
		
		bits -= distance;
	}
	
	public void shiftDown(){
		long oneMask = 1l;
		long distance = 0l;
		int currentBit = 1;
		
		while(((bits >> currentBit) & oneMask) == 1){
			distance = distance << 1;
			distance += 1;
			distance = distance << 1;
			currentBit+=2;
		}
		distance += 1;
		distance = distance << 1;
		
		bits += distance;
	}
	
	public long getNumQuadsAcross(){
		long toBeShifted = 1;
		return toBeShifted << getLevel();
	}
	
	public Pixel getPixel(){
		return new Pixel(getX(), getY(), getLevel());
	}
	
	public Coordinate getCoordinate(){
		return GridTool.getCoordinateFromPixel(getPixel());
	}
	
	public boolean isRoot(){
		if(this.getLevel()==0){
			return true;
		}
		return false;
	}
	
	public List<Quad> getChildren(){
		long length = this.bits >>> NUM_GRID_BITS;
		length+=1;
		long currentChild = length << NUM_GRID_BITS;
		currentChild |= (this.bits & ZERO_LEVEL_BITS_WITH_ALL_GRID_BITS) << 2;
		List<Quad> children = new ArrayList<Quad>(4);
		for(int i = 0; i < 4; i++){
			children.add(new Quad(currentChild));
			currentChild ++;
		}
		return children;
	}
	
	public List<Quad> get3Siblings(){
		if(this.isRoot()){ return null; }
		List<Quad> siblings = new ArrayList<Quad>(3);
		long currentSibling = (bits | 3l) - 3l;
		for(int i = 0; i < 4; i++){
			if(bits != currentSibling){
				siblings.add(new Quad(currentSibling));
			}
			currentSibling ++;
		}
		return siblings;
	}

	public boolean isParentOfOrEqualTo(Quad other){
		if(getLevel() > other.getLevel()){
			return false;
		}
		return gridBitsAtLevel(getLevel()) == other.gridBitsAtLevel(getLevel()); 
	}

	public boolean isChildOfOrEqualTo(Quad other) {
		if(getLevel() < other.getLevel()){
			return false;
		}
		
		return gridBitsAtLevel(other.getLevel()) == other.gridBitsAtLevel(other.getLevel());
	}
	
	protected long gridBitsAtLevel(int level){
		long gridBits = this.bits & ZERO_LEVEL_BITS_WITH_ALL_GRID_BITS;
		int levelsDeeper = level - this.getLevel();
		if(levelsDeeper > 0){
			long shiftedGridBits = gridBits << (2* levelsDeeper);
			return shiftedGridBits;
		}else if(levelsDeeper < 0){
			long shiftedGridBits = gridBits >>> (-2* levelsDeeper);
			return shiftedGridBits;
		}else{
			return gridBits;
		}
	}
	
	protected long levelBitsAtLevel(int level){
		return ((long)level) << NUM_GRID_BITS;
	}
	
	public Quad getAtLevel(int level){
//		System.out.println(StringTool.repeat("-", 3+this.NUM_LEVEL_BITS)+this.toMicrosoftStyleString());
//		System.out.println(LongTool.toBitString(bits));
		long levelBits = levelBitsAtLevel(level);
//		System.out.println(LongTool.toBitString(levelBits));
		long gridBits = gridBitsAtLevel(level);
//		System.out.println(LongTool.toBitString(gridBits));
		return new Quad(levelBits | gridBits);
	}
	
	public Quad getAtMaxLevel(){
		return getAtLevel(MAX_LEVEL);
	}
	

	/*************************************** standard methods *****************************************/
	
	@Override
	public int hashCode(){
		return (int)bits;
	}
	
	@Override
	public boolean equals(Object other){
		return this.bits == ((Quad)other).bits;
	}
	
	public boolean notEquals(Object other){
		return ! equals(other);
	}

	@Override
	public String toString(){
		return toMicrosoftStyleString();
	}
	
	/****************************************** getters /setters ***********************************/
	
	public long getBits(){
		return bits;
	}
	
	/*************************************** printing methods *****************************************/
	
	public String toCsvString(){
		return getLevel()+","+getX()+","+getY();
	}
	
	public String toBinaryStringPadded(){
		return StringTool.pad(Long.toBinaryString(bits), '0', 64);
	}
	
	public String toBinaryStringFormatted(){
		String s = StringTool.pad(Long.toBinaryString(bits), '0', 64);
		String levelBits = s.substring(3, 3+5);
		String gridBits = s.substring(3+5);
		return "0" + "_" + levelBits + "_" + gridBits;
	}
	
	public String toHexStringPadded(){
		return StringTool.pad(Long.toHexString(bits), '0', 16);
	}
	
	public String toMicrosoftStyleString(){
		int length = getLevel();
		char[] s = new char[length];
		for(int i=0; i < length; ++i){
			long twoBits = (bits >>> (i*2)) & 3l;
			if(twoBits==0){
				s[length-i-1] = '0';
			}else if(twoBits==1){
				s[length-i-1] = '1';
			}else if(twoBits==2){
				s[length-i-1] = '2';
			}else if(twoBits==3){
				s[length-i-1] = '3';
			}
		}
		return new String(s);
	}
	
	public String toMicrosoftStyleStringNoEmptyString(){
		int length = getLevel();
		char[] s = new char[length];
		for(int i=0; i < length; ++i){
			long twoBits = (bits >>> (i*2)) & 3l;
			if(twoBits==0){
				s[length-i-1] = '0';
			}else if(twoBits==1){
				s[length-i-1] = '1';
			}else if(twoBits==2){
				s[length-i-1] = '2';
			}else if(twoBits==3){
				s[length-i-1] = '3';
			}
		}
		if(s.length == 0){
			return TOP_QUAD_STRING;
		}
		return new String(s);
	}
	
	public String getMicrosoftStyleString(){
		return toMicrosoftStyleString();
	}

	public Quad fromMicrosoftStyleString(String s){
		if(s==null || s.length()==0 || TOP_QUAD_STRING.equals(s)){
			bits = 0;
			return this;
		}
		long length = s.length();
		bits = length << NUM_GRID_BITS;
		for(int i=0; i < length; ++i){
			if(s.charAt(i)=='0'){
				//nothing
			}else if(s.charAt(i)=='1'){
				bits |= 1l << (2*(length-i-1));
			}else if(s.charAt(i)=='2'){
				bits |= 2l << (2*(length-i-1));
			}else if(s.charAt(i)=='3'){
				bits |= 3l << (2*(length-i-1));
			}
		}
		return this;
	}
	
	/*************************************** ByteAware *****************************************/

	public long getNumBytes(){
		//8 byte long, plus either 4 or 8 byte pointer
		if(RuntimeTool.is64BitVM()){ return 16; }
		else{ return 12; }
	}
	
	
	/*************************************** tests and main *****************************************/

	
	public static class QuadTests{
		@Test public void testConstants(){
			String tableMaskBitString = Integer.toBinaryString(DilatedBitTool28.TABLE_MASK);
//			System.out.println(HALF_GRID_BITS+" "+tableMaskBitString);
			for(int i=0; i < DilatedBitTool28.HALF_GRID_BITS; ++i){
				Assert.assertEquals('1', tableMaskBitString.charAt(i));
			}
		}
		@Test public void testBasics(){
			//careful changing the 3,13,15 combo.  this test won't work for bigger numbers
			
			//print alternating zero strings for visible alignment
//			System.out.println("      "+EIGHT_ZERO_STRING+EIGHT_SPACE_STRING+EIGHT_ZERO_STRING+EIGHT_SPACE_STRING+EIGHT_ZERO_STRING+EIGHT_SPACE_STRING+EIGHT_ZERO_STRING+EIGHT_SPACE_STRING);
			
			//level
			long level = 4;
			long shiftedLevel = level << NUM_GRID_BITS;
			String bits = StringTool.pad(Long.toBinaryString(shiftedLevel), '0', 64);
//			System.out.println("level_"+bits);
			Assert.assertEquals("00000100", bits.substring(0,8));
			
			//x part
			long x = 13;
			String xBinaryString = "1101";
			Assert.assertEquals(xBinaryString, Long.toBinaryString(x));
			
			long rightPartOfX = x & DilatedBitTool28.TABLE_MASK;  //x and'd with 16 1's
			Assert.assertEquals(13, rightPartOfX);
			long dilatedRightPartOfX = DilatedBitTool28.DILATED_TABLE[(int)rightPartOfX];
			String dilatedRightPartOfXBinaryString = "1010001";  //omit leading 0
//			System.out.println("  r x_"+StringTool.pad(dilatedRightPartOfXBinaryString,'-',64));
			Assert.assertEquals(dilatedRightPartOfXBinaryString, Long.toBinaryString(dilatedRightPartOfX));
			
			long leftPartOfX = x >>> 16;
			Assert.assertEquals(0, leftPartOfX);
			long dilatedLeftPartOfX = DilatedBitTool28.DILATED_TABLE[(int)leftPartOfX];
//			String dilatedLeftPartOfXBinaryString = "";
//			System.out.println("  l x_"+StringTool.pad(dilatedLeftPartOfXBinaryString,'-',64));
			Assert.assertEquals(0, dilatedLeftPartOfX);
			
			//y part
			long y = 15;
			String yBinaryString = "1111";
			Assert.assertEquals(yBinaryString, Long.toBinaryString(y));
			
			long rightPartOfY = y & DilatedBitTool28.TABLE_MASK;  //x and'd with 16 1's
			Assert.assertEquals(15, rightPartOfY);
			long dilatedRightPartOfY = DilatedBitTool28.LEFT_SHIFTED_DILATED_TABLE[(int)rightPartOfY];
			String dilatedLeftShiftedRightPartOfYBinaryString = "10101010";
//			System.out.println("  r y_"+StringTool.pad(dilatedLeftShiftedRightPartOfYBinaryString,'-',64));
			Assert.assertEquals(dilatedLeftShiftedRightPartOfYBinaryString, Long.toBinaryString(dilatedRightPartOfY));
			
			long leftPartOfY = y >>> 16;
			Assert.assertEquals(0, leftPartOfY);
			long dilatedLeftPartOfY = DilatedBitTool28.LEFT_SHIFTED_DILATED_TABLE[(int)leftPartOfY];
//			String dilatedLeftShiftedLeftPartOfYBinaryString = "";
//			System.out.println("  l y_"+StringTool.pad(dilatedLeftShiftedLeftPartOfYBinaryString,'-',64));
			Assert.assertEquals(0, dilatedLeftPartOfY);
			
			//now put the same values in a quad and see if it behaves
			Quad q = new Quad(level,x,y);
//			System.out.println("    q_"+q.toBinaryStringPadded());
			Assert.assertEquals(4, q.getLevel());
			Assert.assertEquals(13, q.getX());
			Assert.assertEquals(15, q.getY());
			
			String aboveQuadAsMicrosoftString = "3323";
			Quad s = new Quad(aboveQuadAsMicrosoftString);
			Assert.assertEquals(4, s.getLevel());
			Assert.assertEquals(13, s.getX());
			Assert.assertEquals(15, s.getY());
		}
		@Test public void testLongZero(){
			String s = SIXTEEN_ZERO_STRING+"000000";
			Quad q = new Quad(s);
			Assert.assertEquals(s.length(), q.getLevel());
			Assert.assertEquals(0, q.getX());
			Assert.assertEquals(0, q.getY());
		}
		@Test public void testLongOnes(){
			String s = "1111111111111111111";
			Quad q = new Quad(s);
			Assert.assertEquals(s.length(), q.getLevel());
			Assert.assertEquals((1l<<s.length())-1, q.getX());
			Assert.assertEquals(0, q.getY());
		}
		@Test public void testFullTwos(){
			String s = "2222222222222222222222222222";
			Quad q = new Quad(s);
			Assert.assertEquals(s.length(), q.getLevel());
			Assert.assertEquals(0, q.getX());
			Assert.assertEquals((1l<<s.length())-1, q.getY());
		}
		@Test public void testFullThrees(){
			String s = "3333333333333333333333333333";
			Quad q = new Quad(s);
			Assert.assertEquals(s.length(), q.getLevel());
			Assert.assertEquals((1l<<s.length())-1, q.getX());
			Assert.assertEquals((1l<<s.length())-1, q.getY());
		}
		@Test public void testComplexQuad(){
			String s = "0320100322312132010";
			int sLevel = GridTool.getLevel(s);
			Pixel p = GridTool.getPixelForQuadCode(s);
			Quad q = new Quad(s);

			Assert.assertEquals("19,149938,200536",q.toCsvString());
			Assert.assertEquals(sLevel, q.getLevel());
			Assert.assertEquals(p.getX(), q.getX());
			Assert.assertEquals(p.getY(), q.getY());
		}
		@Test public void testComplexQuad2(){
			String s = "032021232302132110303333201";
			int sLevel = GridTool.getLevel(s);
			Pixel p = GridTool.getPixelForQuadCode(s);
			Quad q = new Quad(s);
			
			Assert.assertEquals("27,36334969,56537468",q.toCsvString());
			Assert.assertEquals(sLevel, q.getLevel());
			Assert.assertEquals(s.length(), q.getLevel());
			Assert.assertEquals(s, q.toMicrosoftStyleString());
			Assert.assertEquals(p.getY(), q.getY());
			Assert.assertEquals(p.getX(), q.getX());
		}
		@Test public void testComplexQuad3(){
			String s0 = "03201002330";
			String s1 = "03201002331";
			String sp = "0320100233";
			Quad q0 = new Quad(s0);
			Quad q1 = new Quad(s1);
			Quad qp = new Quad(sp);
			Assert.assertTrue(q0.bits != q1.bits);
			Assert.assertTrue(qp.bits != q1.bits);
			Assert.assertTrue(qp.bits != q0.bits);
			Assert.assertFalse(q0.equals(q1));
			Assert.assertFalse(sp.equals(q0));
			Assert.assertFalse(sp.equals(q1));
		}
		@Test public void testComplexQuad4(){
			String s0 = "03200300000000000000000";
			Quad q0 = new Quad(s0);
			Quad qc = q0.getLastPossibleSubQuad();
			Assert.assertEquals("0320030000000000000000033333",qc.toString());
		}
		@Test public void testComplexQuad5(){
			String s0 = "03201003220303101300";
			Quad q = new Quad(s0);
			Quad roundTripped = new Quad(q.getLevel(), q.getX(), q.getY());
			Assert.assertEquals(q, roundTripped);
		}
		@Test public void testMicrosoftStyleString(){
			String s = "0320100322312132010";//--0,032000000000330332220210113,218188,032002333132010030030121100 - 03200300000000000000000000
			Quad q = new Quad(s);
			Assert.assertEquals(s, q.toMicrosoftStyleString());
		}
		@Test public void testNestingParent(){
			Quad parent =    new Quad("032021230201121202");
			Quad notAChild = new Quad("032023112222213202320112132");
			Quad child =     new Quad("03202123020112120229");
			Assert.assertFalse(parent.isParentOfOrEqualTo(notAChild));
			Assert.assertTrue(parent.isParentOfOrEqualTo(child));
			Assert.assertTrue(parent.isParentOfOrEqualTo(parent));
		}
		@Test public void testNestingChild(){
			Quad parent =    new Quad("032021230201121202");
			Quad notAChild = new Quad("032023112222213202320112132");
			Quad child =     new Quad("03202123020112120229");
			Assert.assertTrue(child.isChildOfOrEqualTo(parent));
			Assert.assertFalse(parent.isChildOfOrEqualTo(notAChild));
			Assert.assertFalse(parent.isChildOfOrEqualTo(child));
			Assert.assertTrue(parent.isChildOfOrEqualTo(parent));
		}
		@Test public void testCompare(){
			Quad q0 = new Quad("0320");
			Quad q1 = new Quad("032");
			Assert.assertTrue(q1.compareTo(q0) < 0);
		}
		@Test public void testSort(){
			List<Quad> quads = new ArrayList<Quad>(); 
			quads.add(new Quad("q"));
			quads.add(new Quad("03202"));
			quads.add(new Quad("03"));
			quads.add(new Quad("030"));
			quads.add(new Quad("033"));
			quads.add(new Quad("031"));
			quads.add(new Quad("01"));
			quads.add(new Quad("031"));
			Collections.sort(quads);
			
			List<String> strings = Quad.getMicrosoftStyleStrings(quads);

			List<String> sortedStrings = new ArrayList<String>(strings);
			Collections.sort(sortedStrings);
			
			Assert.assertArrayEquals(sortedStrings.toArray(), strings.toArray());
		}
		@Test public void testRange(){
			Quad q = new Quad("031");
			String twentyFiveThrees = "3333333333333333333333333";
			Assert.assertEquals("031"+twentyFiveThrees, q.getLastPossibleSubQuad().toMicrosoftStyleString());
		}
		@Test public void testGetAtLevel(){
			Quad q = new Quad("03133333");
			Assert.assertEquals(q.getLevel(), 8);
			Quad q1 = q.getAtLevel(1);
			Quad q3 = q.getAtLevel(3);
			Quad q12 = q.getAtLevel(12);
			Assert.assertEquals(q1.toMicrosoftStyleString(), "0");
			Assert.assertEquals(q3.toMicrosoftStyleString(), "031");
			Assert.assertEquals(q12.toMicrosoftStyleString(), "031333330000");
		}
		@Test public void testGetNumQuadsAcross(){
			Quad q1 = new Quad("031");
			Assert.assertEquals(q1.getNumQuadsAcross(), 8);
			Quad q2 = new Quad("0");
			Assert.assertEquals(q2.getNumQuadsAcross(), 2);
			Quad q3 = new Quad("q");
			Assert.assertEquals(q3.getNumQuadsAcross(), 1);
			Quad q4 = new Quad("00223133211200332121");
			Assert.assertEquals(q4.getNumQuadsAcross(), 1024*1024);
		}
		@Test public void testGetSiblings(){
			Quad q1 = new Quad("031");
			Quad[] siblings = q1.get3Siblings().toArray(new Quad[]{});
			Assert.assertArrayEquals(new Quad[]{new Quad("030"),new Quad("032"),new Quad("033")}, siblings);
		}
		@Test public void testGetChildren(){
			Quad q1 = new Quad("031");
			Quad[] children = q1.getChildren().toArray(new Quad[]{});
			Assert.assertArrayEquals(new Quad[]{new Quad("0310"),new Quad("0311"),new Quad("0312"),new Quad("0313")}, children);
		}
		@Test public void testShiftLeft(){
			Quad q1 = new Quad("001");
			Quad q2 = new Quad("030");
			Quad q3 = new Quad("300");
			Quad q4 = new Quad("1022");
			q1.shiftLeft();
			q2.shiftLeft();
			q3.shiftLeft();
			q4.shiftLeft();
			Assert.assertEquals(new Quad("000"), q1);
			Assert.assertEquals(new Quad("021"), q2);
			Assert.assertEquals(new Quad("211"), q3);
			Assert.assertEquals(new Quad("0133"), q4);
		}
		@Test public void testShiftRight(){
			Quad q1 = new Quad("000");
			Quad q2 = new Quad("021");
			Quad q3 = new Quad("211");
			Quad q4 = new Quad("0133");
			q1.shiftRight();
			q2.shiftRight();
			q3.shiftRight();
			q4.shiftRight();
			Assert.assertEquals(new Quad("001"), q1);
			Assert.assertEquals(new Quad("030"), q2);
			Assert.assertEquals(new Quad("300"), q3);
			Assert.assertEquals(new Quad("1022"), q4);
		}
		@Test public void testShiftUp(){
			Quad q1 = new Quad("002");
			Quad q2 = new Quad("021");
			Quad q3 = new Quad("300");
			Quad q4 = new Quad("2111");
			q1.shiftUp();
			q2.shiftUp();
			q3.shiftUp();
			q4.shiftUp();
			Assert.assertEquals(new Quad("000"), q1);
			Assert.assertEquals(new Quad("003"), q2);
			Assert.assertEquals(new Quad("122"), q3);
			Assert.assertEquals(new Quad("0333"), q4);
		}
		@Test public void testShiftDown(){
			Quad q1 = new Quad("000");
			Quad q2 = new Quad("003");
			Quad q3 = new Quad("122");
			Quad q4 = new Quad("0333");
			q1.shiftDown();
			q2.shiftDown();
			q3.shiftDown();
			q4.shiftDown();
			Assert.assertEquals(new Quad("002"), q1);
			Assert.assertEquals(new Quad("021"), q2);
			Assert.assertEquals(new Quad("300"), q3);
			Assert.assertEquals(new Quad("2111"), q4);
		}
		
		@Test public void isXYEven(){
			Quad q1 = new Quad("000");
			Quad q2 = new Quad("003");
			Quad q3 = new Quad("122");
			Quad q4 = new Quad("0331");
			Assert.assertEquals(true, q1.isXEven());
			Assert.assertEquals(false, q2.isXEven());
			Assert.assertEquals(true, q3.isXEven());
			Assert.assertEquals(false, q4.isXEven());
			
			Assert.assertEquals(true, q1.isYEven());
			Assert.assertEquals(false, q2.isYEven());
			Assert.assertEquals(false, q3.isYEven());
			Assert.assertEquals(true, q4.isYEven());
		}
	}
	
	public static void main(String[] args){
		String q  = "032010023313233230130010230";
		List<Quad> quads = new ArrayList<Quad>();
		SortedSet<Quad> quadSet = new TreeSet<Quad>();
		for(int i=0; i <= q.length(); ++i){
			Quad quad = new Quad(q.substring(0,i));
			quads.add(quad);
			quadSet.add(quad);
		}
		System.out.println("\n\n");
		for(int i=0; i < quads.size(); ++i){
			System.out.println(i+" "+quads.get(i).bits);
		}
		System.out.println("\n\n");
		for(int i=0; i < quads.size(); ++i){
			System.out.println(i+" "+quads.get(i));
		}
		System.out.println("\n\n");
		int counter = 0;
		for(Quad theq : quadSet){
			System.out.println(counter++ +" "+theq);
		}
		
		
//		for (Quad child : children) {
//			System.out.println("Child: " + child);
//		}
	}
}