package com.hotpads.util.core.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.LongTool;
import com.hotpads.util.core.RuntimeTool;
import com.hotpads.util.core.StringTool;


/*
 * highly optimized class
 * 
 * stored in 64 bit primitive long
 * 
 * The leftmost bit is blank.  The last 5 bits represent the level which can be 0 through 29.  
 * 
 * the 58 remaining "grid" bits are left justified so that the "bits" field can also be used as an efficient hashCode
 * 
 * y is the direction that is shifted left one bit
 * 
 * natural ordering is the same as ordering Microsoft style quad strings
*/

@SuppressWarnings("serial") 
public class SQuad implements Comparable<SQuad>, Serializable{
	
	private static final long ONE_MASK = 1l;
	private static final long TWO_MASK = 2l;

	public static final String TOP_QUAD_STRING = "q";
	public static final SQuad TOP_QUAD = new SQuad();
	public static final SQuad EMPTY_STRING_QUAD = new SQuad("");


	//natural ordering is the same as ordering Microsoft style quad strings
	//We need to delete level bits, line up the grid bits, and then compare the grid bits
	@Override
	public int compareTo(SQuad other){
		if(this.bits < other.bits){ return -1; }
		if(this.bits > other.bits){ return 1; }
		return 0;
	}
	
	//get the xxxxxxx333333333 quad on level 29
	public static String getLastPossibleSubQuadFromString(String quadString){
		SQuad quad = new SQuad(quadString);
		return quad.getLastPossibleSubQuad().toMicrosoftStyleString();
	}
	
	//get the xxxxxxx333333333 quad on level 29
	public SQuad getLastPossibleSubQuad(){
		int shift = (NUM_BITS_IN_LONG - NUM_EMPTY_BITS) - (getLevel() * 2);
		long levelRemoved = getGridBits() << shift;
		long lowerLevelsFilledIn = levelRemoved | LongTool.setRightBits(shift);
		long clearLevelBits = lowerLevelsFilledIn & ZERO_LEVEL_BITS_WITH_ALL_GRID_BITS;
		long maxLevelApplied = clearLevelBits | MAX_LEVEL_BITS_WITH_NO_GRID_BITS;
		return new SQuad(maxLevelApplied);
	}
	
	/********************************************* static ****************************************/
	public static final int NUM_EMPTY_BITS = 1;
	public static final int NUM_BITS_IN_LONG = 64;
	public static final int NUM_LEVELS_AFTER_ZERO = 28;  //there's also level 0 which has an empty quad code
	public static final int NUM_LEVELS_AFTER_ZERO_30 = 30;
	public static final int MAX_LEVEL = NUM_LEVELS_AFTER_ZERO;
	public static final int NUM_GRID_BITS = 29 * 2;  //one bit for each of x and y on each level
	public static final int NUM_LEVEL_BITS = 5; //basically 29<<5
	
	public static final long ZERO_LEVEL_BITS_WITH_ALL_GRID_BITS = LongTool.setLeftBits(NUM_GRID_BITS+1);
	public static final long MAX_LEVEL_BITS_WITH_NO_GRID_BITS = 29L; 
	


	public static final int SIXTEEN_ONES = 0xffff;
	public static final String EIGHT_SPACE_STRING = "        ";
	public static final String SIXTEEN_SPACE_STRING = EIGHT_SPACE_STRING + EIGHT_SPACE_STRING;
	public static final String EIGHT_ZERO_STRING = "00000000";
	public static final String SIXTEEN_ZERO_STRING = EIGHT_ZERO_STRING + EIGHT_ZERO_STRING;
	

	/*************************************** fields ******************************************************/
	
	protected long bits;
	
	/**************************************** static method*************************************************/
	
	public static final int RIGHT_BIT_TABLE_MASK = 14;
	public static final int[] RIGHT_BIT_LOOK_UP_TABLE = new int[RIGHT_BIT_TABLE_MASK+1];
	
	static {
		int j = 1;
		RIGHT_BIT_LOOK_UP_TABLE[0] = 0;
		RIGHT_BIT_LOOK_UP_TABLE[1] = 1;
		for (int i = 2; i <= RIGHT_BIT_TABLE_MASK; i++) {
			RIGHT_BIT_LOOK_UP_TABLE[i] = RIGHT_BIT_LOOK_UP_TABLE[i-1] + (j*2);
			j*=2;
		}
	}
	

	/*************************************** constructors *************************************************/
		
	public SQuad(){
		this(TOP_QUAD_STRING);
	}
	
	public SQuad(long bits){
		this.bits = bits;
	}
	
	public SQuad(Quad quad){
		this(quad.getLevel(), quad.getX(), quad.getY());
	}
	
	public SQuad(long level, long x, long y){
		
		if (level < 15) {
			long rightXBits = x & RIGHT_BIT_LOOK_UP_TABLE[(int)level];
			long dilatedRightXBits = DilatedBitTool.DILATED_TABLE[(int)rightXBits];
			long rightYBits = y & RIGHT_BIT_LOOK_UP_TABLE[(int)level];
			long dilatedRightYBits = DilatedBitTool.LEFT_SHIFTED_DILATED_TABLE[(int)rightYBits];

			bits = level 
					| dilatedRightXBits << (NUM_GRID_BITS + NUM_LEVEL_BITS) - (level * 2)
					| dilatedRightYBits << (NUM_GRID_BITS + NUM_LEVEL_BITS) - (level * 2);

		} else {
			long shift = level - 15;
			
			//split x into left and right HALF_GRID_BITS(14) bits because that's the width of our dilation lookup table
			long leftXBits = x >>> shift;			
			long dilatedLeftXBits = DilatedBitTool.DILATED_TABLE[(int)leftXBits];
			long rightXBits = x & RIGHT_BIT_LOOK_UP_TABLE[(int)shift];			
			long dilatedRightXBits = DilatedBitTool.DILATED_TABLE[(int)rightXBits];
			
			//do the same for y as x, but use the left shifted table
			long leftYBits = y >>> shift;
			long dilatedLeftYBits = DilatedBitTool.LEFT_SHIFTED_DILATED_TABLE[(int)leftYBits];
			long rightYBits = y & RIGHT_BIT_LOOK_UP_TABLE[(int)shift];
			long dilatedRightYBits = DilatedBitTool.LEFT_SHIFTED_DILATED_TABLE[(int)rightYBits];
			bits = level
					| dilatedLeftXBits << (NUM_LEVELS_AFTER_ZERO + NUM_LEVEL_BITS) 
					| dilatedRightXBits << (NUM_LEVELS_AFTER_ZERO + NUM_LEVEL_BITS) - (shift * 2)
					| dilatedLeftYBits << (NUM_LEVELS_AFTER_ZERO + NUM_LEVEL_BITS) 
					| dilatedRightYBits << (NUM_LEVELS_AFTER_ZERO + NUM_LEVEL_BITS) - (shift * 2);
		}
	}
	
	/*
	 * for quads of the microsoft maps pattern below:
	 * 
	 *     01
	 *     23
	 */
	public SQuad(String s){
		fromMicrosoftStyleString(s);
	}
	
	
	
	/********************************* factory methods *********************************************/
	
	public static List<SQuad> fromMicrosoftStyleStrings(Collection<String> ins){
		List<SQuad> outs = ListTool.createArrayListWithSize(ins);
		for(String in : IterableTool.nullSafe(ins)){
			outs.add(new SQuad(in));
		}
		return outs;
	}
	
	public static List<String> getMicrosoftStyleStrings(List<SQuad> quads){
		List<String> strings = new ArrayList<String>(CollectionTool.sizeNullSafe(quads));
		for(SQuad quad : CollectionTool.nullSafe(quads)){
			strings.add(quad.toMicrosoftStyleString());
		}
		return strings;
	}
	
	
	/*************************************** useful methods *****************************************/

	public int getLevel() {
		return (int)(bits & RIGHT_BIT_LOOK_UP_TABLE[5]);
	} 
	
	/*
	 * start at the right and move left through the x grid bits, adding them up each time.  not optimal
	 */
	public long getX(){
		long x = 0;
		long level = getLevel();
		
		long shift = (NUM_GRID_BITS + NUM_LEVEL_BITS) - (level * 2);
		
		for(short bit=0; bit < NUM_GRID_BITS; bit+=2){
			long mask = 1l << bit;
			long workingBit = (bits >>> shift) & mask;
			long contractedWorkingBit = workingBit >>> (bit/2);
			x += contractedWorkingBit;
		}
		return x;
	}

	public long getY(){
		long y = 0, shift;
		long level = getLevel();
		
		shift = (NUM_GRID_BITS + NUM_LEVEL_BITS) - (level * 2);
		
		for(short bit=1; bit < NUM_GRID_BITS; bit+=2){
			long mask = 1l << bit;
			long workingBit = (bits >>> shift) & mask;
			long contractedWorkingBit = workingBit >>> ((bit/2)+1);  //shift one extra right because y is offset 1 left
			y += contractedWorkingBit;
		}
		return y;
	}

	public boolean isXEven(){
		return (getGridBits() & ONE_MASK) != ONE_MASK;
	}
	
	public boolean isYEven(){
		return (getGridBits() & TWO_MASK) != TWO_MASK;
	}
	
	public void replaceQWithEmptyStringQuad(){
		if(toMicrosoftStyleString().equals(TOP_QUAD_STRING)){
			bits = 0;
		}
	}
	
	public SQuad getParent(){
		return this.getAtLevel(this.getLevel()-1);
	}

	public void shiftLeft(){
		long distance = 0l;
		int currentBit = 0;
		int shift = (NUM_BITS_IN_LONG - NUM_EMPTY_BITS) - (getLevel() * 2);
		
		while(((getGridBits() >> currentBit) & ONE_MASK) == 0){
			distance = distance << 1;			
			distance += 1;
			distance = distance << 1;
			currentBit+=2;
		}
		distance += 1;
		
		bits = ((getGridBits() - distance) << shift) | getLevel();
	}

	public void shiftRight(){
		long distance = 0l;
		int currentBit = 0;
		int shift = (NUM_BITS_IN_LONG - NUM_EMPTY_BITS) - (getLevel() * 2);
		
		while(((getGridBits() >> currentBit) & ONE_MASK) == 1){
			distance = distance << 1;
			distance += 1;
			distance = distance << 1;
			currentBit+=2;
		}
		distance += 1;
		
		bits = ((getGridBits() + distance) << shift) | getLevel();
	}
	
	public void shiftUp(){
		long oneMask = 1l;
		long distance = 0l;
		int currentBit = 1;
		int shift = (NUM_BITS_IN_LONG - NUM_EMPTY_BITS) - (getLevel() * 2);
		
		while(((getGridBits() >> currentBit) & oneMask) == 0){
			distance = distance << 1;
			distance += 1;
			distance = distance << 1;
			currentBit+=2;
		}
		distance += 1;
		distance = distance << 1;
		
		bits = ((getGridBits() - distance) << shift) | getLevel();
	}

	public void shiftDown(){
		long oneMask = 1l;
		long distance = 0l;
		int currentBit = 1;
		int shift = (NUM_BITS_IN_LONG - NUM_EMPTY_BITS) - (getLevel() * 2);
		
		while(((getGridBits() >> currentBit) & oneMask) == 1){
			distance = distance << 1;
			distance += 1;
			distance = distance << 1;
			currentBit+=2;
		}
		distance += 1;
		distance = distance << 1;

		bits = ((getGridBits() + distance) << shift) | getLevel();
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
		if(getLevel()==0){
			return true;
		}
		return false;		
	}
	
	public List<SQuad> getChildren(){
		int shift = (NUM_BITS_IN_LONG - NUM_EMPTY_BITS) - (getLevel() * 2);
		long gridBits = getGridBits();		
		long length = getLevel();			
		long currentChild = ++length;
		
		gridBits = gridBits << 2;
		long formattedGridBits = gridBits << shift-2;
		currentChild |= formattedGridBits;
		
		List<SQuad> children = new ArrayList<SQuad>(4);
		for(int i = 0; i < 4; i++){
			children.add(new SQuad(currentChild));
			currentChild = length;			
			gridBits++;
			formattedGridBits = gridBits << shift-2;
			currentChild |= formattedGridBits;
		}
		
		return children;
	}
	
	public List<SQuad> getDescendantAtLevel(int level){
		List<SQuad> descendant = ListTool.create();
		int tempLevel = getLevel();
		if(tempLevel < level){
			List<SQuad> children = getChildren();
			for(SQuad child : children){
				descendant.addAll(child.getDescendantAtLevel(level));
			}
		}else if(tempLevel == level){
			descendant.add(this);
		}		
		return descendant;
	}
	
	public List<SQuad> get3Siblings(){
		if(isRoot()){ return null; }
		
		int shift = (NUM_BITS_IN_LONG - NUM_EMPTY_BITS) - (getLevel() * 2);
//		long gridBits = getGridBits();
		List<SQuad> siblings = new ArrayList<SQuad>(3);
		long currentSibling = (getGridBits() | 3l) - 3l;
		for(int i = 0; i < 4; i++){
			if(getGridBits() != currentSibling){
				long formattedSibling = (currentSibling << shift) | getLevel();
				siblings.add(new SQuad(formattedSibling));
			}
			currentSibling ++;
		}
		return siblings;
	}
	
	public boolean isParentOfOrEqualTo(SQuad other){
		if(getLevel() > other.getLevel()){
			return false;
		}
		return gridBitsAtLevel(getLevel()) == other.gridBitsAtLevel(getLevel()); 
	}
	
	public boolean isChildOfOrEqualTo(SQuad other) {
		if(getLevel() < other.getLevel()){
			return false;
		}
		return gridBitsAtLevel(other.getLevel()) == other.gridBitsAtLevel(other.getLevel());
	}
	
	public boolean isChildOf(SQuad other) {
		if(getLevel() <= other.getLevel()){
			return false;
		}
		return gridBitsAtLevel(other.getLevel()) == other.gridBitsAtLevel(other.getLevel());
	}
	
	public SQuad getAtMaxLevel(){
		return getAtLevel(MAX_LEVEL);
	}
	
	public SQuad getAtLevel(int level){
		long levelBits = levelBitsAtLevel(level);
		long formattedGridBitsAtLevel = gridBitsAtLevel(level) << ((NUM_GRID_BITS+NUM_LEVEL_BITS) - (level*2));
		
		return new SQuad(levelBits | formattedGridBitsAtLevel);
	}
		
	protected long gridBitsAtLevel(int level){
		int levelsDeeper = level - this.getLevel();
		if(levelsDeeper > 0){
			long shiftedGridBits = getGridBits() << (2* levelsDeeper);
			return shiftedGridBits;
		}else if(levelsDeeper < 0){
			long shiftedGridBits = getGridBits() >>> (-2* levelsDeeper);
			return shiftedGridBits;
		}else{
			return getGridBits();
		}
	}

	public long getGridBits() {
		long gridBits;
		long shift = (NUM_GRID_BITS + NUM_LEVEL_BITS) - (getLevel() * 2);
		
		gridBits = bits >> shift;
			
		return gridBits;		
	}
	
	protected long levelBitsAtLevel(int level){
		return level;
	}
	
	
	/*************************************** standard methods *****************************************/
	
	@Override
	public int hashCode(){
		return (int)bits;
	}
	
	@Override
	public boolean equals(Object other){
		return this.bits == ((SQuad)other).bits;
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
		String gridBits = s.substring(1, 1+58);
		String levelBits = s.substring(1+58);
		
		return "0" + "_" + gridBits + "_" + levelBits;
	}
	
	public String toHexStringPadded(){
		return StringTool.pad(Long.toHexString(bits), '0', 16);
	}
	
	public String toMicrosoftStyleString(){
		int length = getLevel();
		char[] s = new char[length];
		long XYbits = bits >>> (NUM_GRID_BITS + NUM_LEVEL_BITS) - length*2;
			
		for(int i=0; i < length; ++i){
			long twoBits = (XYbits >>> (i*2)) & 3l;
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
		long XYbits = bits >>> (NUM_GRID_BITS + NUM_LEVEL_BITS) - length*2;
			
		for(int i=0; i < length; ++i){
			long twoBits = (XYbits >>> (i*2)) & 3l;
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
	
	public SQuad fromMicrosoftStyleString(String s){
		if(s==null || s.length()==0 || TOP_QUAD_STRING.equals(s)){
			bits = 0;
			return this;
		}
		
		long length = s.length();
		long xyBits = 0;
		
		bits = length;		
		for(int i=1; i <= length; ++i){
			if(s.charAt(i-1)=='0'){
				//nothing
			}else if(s.charAt(i-1)=='1'){
				xyBits |= 1l << (NUM_GRID_BITS + NUM_LEVEL_BITS) - (2*i);
			}else if(s.charAt(i-1)=='2'){
				xyBits |= 2l << (NUM_GRID_BITS + NUM_LEVEL_BITS) - (2*i);
			}else if(s.charAt(i-1)=='3'){
				xyBits |= 3l << (NUM_GRID_BITS + NUM_LEVEL_BITS) - (2*i);
			}
		}
		bits |= xyBits;
		
		return this;
	}
	
	/*************************************** ByteAware *****************************************/

	public long getNumBytes(){
		//8 byte long, plus either 4 or 8 byte pointer
		if(RuntimeTool.is64BitVM()){ return 16; }
		else{ return 12; }
	}
	
	
	/*************************************** tests and main *****************************************/

	
	public static class SQuadTests{
		@Test public void testConstants(){
			String tableMaskBitString = Integer.toBinaryString(DilatedBitTool.TABLE_MASK);
//			System.out.println(HALF_GRID_BITS+" "+tableMaskBitString);
			for(int i=0; i < DilatedBitTool.HALF_GRID_BITS; ++i){
				Assert.assertEquals('1', tableMaskBitString.charAt(i));
			}
		}
		@Test public void testLevelBits(){
			long level = 4;
			String bits = LongTool.toBitString(level);
			Assert.assertEquals(61, bits.indexOf("1"));
			Assert.assertEquals(61, bits.lastIndexOf("1"));
		}
		@Test public void testYBits(){
			long y = 15, x = 13, level = 4;
			String yBinaryString = "1111";
			Assert.assertEquals(yBinaryString, Long.toBinaryString(y));
			
			long rightPartOfY = y & DilatedBitTool.TABLE_MASK;  //x and'd with 16 1's
			Assert.assertEquals(15, rightPartOfY);
			long dilatedRightPartOfY = DilatedBitTool.LEFT_SHIFTED_DILATED_TABLE[(int)rightPartOfY];
			String dilatedLeftShiftedRightPartOfYBinaryString = "10101010";
//			System.out.println("  r y_"+StringTool.pad(dilatedLeftShiftedRightPartOfYBinaryString,'-',64));
			Assert.assertEquals(dilatedLeftShiftedRightPartOfYBinaryString, Long.toBinaryString(dilatedRightPartOfY));
			
			long leftPartOfY = y >>> 16;
			Assert.assertEquals(0, leftPartOfY);
			long dilatedLeftPartOfY = DilatedBitTool.LEFT_SHIFTED_DILATED_TABLE[(int)leftPartOfY];
//			String dilatedLeftShiftedLeftPartOfYBinaryString = "";
//			System.out.println("  l y_"+StringTool.pad(dilatedLeftShiftedLeftPartOfYBinaryString,'-',64));
			Assert.assertEquals(0, dilatedLeftPartOfY);
			
			//now put the same values in a quad and see if it behaves
			SQuad q = new SQuad(level,x,y);
//			System.out.println("    q_"+q.toBinaryStringPadded());
			Assert.assertEquals(4, q.getLevel());
			Assert.assertEquals(13, q.getX());
			Assert.assertEquals(15, q.getY());
			
			String aboveQuadAsMicrosoftString = "3323";
			SQuad s = new SQuad(aboveQuadAsMicrosoftString);
			Assert.assertEquals(4, s.getLevel());
			Assert.assertEquals(13, s.getX());
			Assert.assertEquals(15, s.getY());
		}
		@Test public void testLongZero(){
			String s = SIXTEEN_ZERO_STRING+"000000";
			SQuad q = new SQuad(s);
			Assert.assertEquals(s.length(), q.getLevel());
			Assert.assertEquals(0, q.getX());
			Assert.assertEquals(0, q.getY());
		}
		@Test public void testLongOnes(){
			String s = "1111111111111111111";
			SQuad q = new SQuad(s);
			Assert.assertEquals(s.length(), q.getLevel());
//			System.out.println("toBinaryStringPadded:    " + q.toBinaryStringPadded());
//			System.out.println("getX: " + q.getX());
			Assert.assertEquals((1l<<s.length())-1, q.getX());
			Assert.assertEquals(0, q.getY());
		}
		@Test public void testFullTwos(){
			String s = "22222222222222222222222222222";
			SQuad q = new SQuad(s);
			Assert.assertEquals(s.length(), q.getLevel());
			Assert.assertEquals(0, q.getX());
			Assert.assertEquals((1l<<s.length())-1, q.getY());
		}
		@Test public void testFullThrees(){
			String s = "33333333333333333333333333333";
			SQuad q = new SQuad(s);
			Assert.assertEquals(s.length(), q.getLevel());
			Assert.assertEquals((1l<<s.length())-1, q.getX());
			Assert.assertEquals((1l<<s.length())-1, q.getY());
		}
		@Test public void testComplexQuad(){
			String s = "0320100322312132010";
			int sLevel = GridTool.getLevel(s);
			Pixel p = GridTool.getPixelForQuadCode(s);
			SQuad q = new SQuad(s);

			Assert.assertEquals("19,149938,200536",q.toCsvString());
			Assert.assertEquals(sLevel, q.getLevel());
			Assert.assertEquals(p.getX(), q.getX());
			Assert.assertEquals(p.getY(), q.getY());
		}
		@Test public void testComplexQuad2(){
			String s = "032021232302132110303333201";
			int sLevel = GridTool.getLevel(s);
			Pixel p = GridTool.getPixelForQuadCode(s);
			SQuad q = new SQuad(s);
			
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
			SQuad q0 = new SQuad(s0);
			SQuad q1 = new SQuad(s1);
			SQuad qp = new SQuad(sp);
			Assert.assertTrue(q0.bits != q1.bits);
			Assert.assertTrue(qp.bits != q1.bits);
			Assert.assertTrue(qp.bits != q0.bits);
			Assert.assertFalse(q0.equals(q1));
			Assert.assertFalse(sp.equals(q0));
			Assert.assertFalse(sp.equals(q1));
		}
		@Test public void testComplexQuad4(){
			String s0 = "03200300000000000000000";
			SQuad q0 = new SQuad(s0);
			SQuad qc = q0.getLastPossibleSubQuad();
			Assert.assertEquals("03200300000000000000000333333",qc.toString());
		}		
		@Test public void testComplexQuad5(){
			String s0 = "03201003220303101300";
			SQuad q = new SQuad(s0);
			SQuad roundTripped = new SQuad(q.getLevel(), q.getX(), q.getY());
			
			Assert.assertEquals(q.getLevel(), roundTripped.getLevel());
			Assert.assertEquals(q.getX(), roundTripped.getX());
			Assert.assertEquals(q.getY(), roundTripped.getY());
			Assert.assertEquals(q, roundTripped);
		}		
		@Test public void testMultipleComplexQuads(){
			ArrayList<String> quads = ListTool.createArrayList(
					"032023112222022003200100200",
					"032023112222022113120000333",
					"032023112222022133123310132",
					"032023112222030002032330312"
					);
			
			List<SQuad> sQuads = SQuad.fromMicrosoftStyleStrings(quads);

			for(SQuad sQuad : sQuads){
				SQuad quad = new SQuad(sQuad.getLevel(), sQuad.getX(), sQuad.getY());
				Assert.assertEquals(sQuad.getLevel(), quad.getLevel());
				Assert.assertEquals(sQuad.getX(), quad.getX());
				Assert.assertEquals(sQuad.getY(), quad.getY());				
				Assert.assertEquals(sQuad.getMicrosoftStyleString(), quad.getMicrosoftStyleString());
				
				SQuad newQuad = new SQuad(quad.getLevel(), quad.getX(), quad.getY());
				Assert.assertEquals(sQuad.getLevel(), newQuad.getLevel());
				Assert.assertEquals(sQuad.getX(), newQuad.getX());
				Assert.assertEquals(sQuad.getY(), newQuad.getY());				
				Assert.assertEquals(sQuad.getMicrosoftStyleString(), newQuad.getMicrosoftStyleString());
			}
		}
		@Test public void testMicrosoftStyleString(){
			String s = "0320100322312132010";//--0,032000000000330332220210113,218188,032002333132010030030121100 - 03200300000000000000000000
			SQuad q = new SQuad(s);
			Assert.assertEquals(s, q.toMicrosoftStyleString());
		}
		@Test public void testGetGridBits() {
			String s0 = "3323";
			String s1 = "032000";
			SQuad q0 = new SQuad(s0);
			SQuad q1 = new SQuad(4L, 13, 15);
			SQuad q2 = new SQuad(s1);
			Assert.assertEquals("11111011", Long.toBinaryString(q0.getGridBits()));
			Assert.assertEquals("11111011", Long.toBinaryString(q1.getGridBits()));
			Assert.assertEquals("1110000000", Long.toBinaryString(q2.getGridBits()));
		}
		@Test public void testNestingChild(){
			SQuad parent =    new SQuad("032021230201121202");
			SQuad notAChild = new SQuad("032023112222213202320112132");
			SQuad child =     new SQuad("03202123020112120229");
			Assert.assertTrue(child.isChildOfOrEqualTo(parent));
			Assert.assertFalse(parent.isChildOfOrEqualTo(notAChild));
			Assert.assertFalse(parent.isChildOfOrEqualTo(child));
			Assert.assertTrue(parent.isChildOfOrEqualTo(parent));
		}
		@Test public void testNestingParent(){
			SQuad parent =    new SQuad("032021230201121202");
			SQuad notAChild = new SQuad("032023112222213202320112132");
			SQuad child =     new SQuad("03202123020112120229");
			Assert.assertFalse(parent.isParentOfOrEqualTo(notAChild));
			Assert.assertTrue(parent.isParentOfOrEqualTo(child));
			Assert.assertTrue(parent.isParentOfOrEqualTo(parent));
		}
		@Test public void testRange(){
			SQuad q0 = new SQuad("031");
			SQuad q1 = new SQuad("0312");
			String twentyFiveThrees = "3333333333333333333333333";
			String twentySixThrees = "33333333333333333333333333";
			Assert.assertEquals("031"+twentySixThrees, q0.getLastPossibleSubQuad().toMicrosoftStyleString());
			Assert.assertEquals("0312"+twentyFiveThrees, q1.getLastPossibleSubQuad().toMicrosoftStyleString());
		}
		@Test public void testGetSiblings(){
			SQuad q1 = new SQuad("031");
			SQuad[] siblings = q1.get3Siblings().toArray(new SQuad[]{});
			Assert.assertArrayEquals(new SQuad[]{new SQuad("030"),new SQuad("032"),new SQuad("033")}, siblings);
		}
		@Test public void testGetChildren(){
			SQuad q0 = new SQuad("");
			SQuad q1 = new SQuad("031");
			SQuad[] children = q0.getChildren().toArray(new SQuad[]{});
			SQuad[] children1 = q1.getChildren().toArray(new SQuad[]{});
			Assert.assertArrayEquals(new SQuad[]{new SQuad("0"),new SQuad("1"),new SQuad("2"),new SQuad("3")}, children);
			Assert.assertArrayEquals(new SQuad[]{new SQuad("0310"),new SQuad("0311"),new SQuad("0312"),new SQuad("0313")}, children1);
		}
		@Test public void testGetNumQuadsAcross(){
			SQuad q1 = new SQuad("031");
			Assert.assertEquals(q1.getNumQuadsAcross(), 8);
			SQuad q2 = new SQuad("0");
			Assert.assertEquals(q2.getNumQuadsAcross(), 2);
			SQuad q3 = new SQuad("q");
			Assert.assertEquals(q3.getNumQuadsAcross(), 1);
			SQuad q4 = new SQuad("00223133211200332121");
			Assert.assertEquals(q4.getNumQuadsAcross(), 1024*1024);
		}
		@Test public void testShiftLeft(){
			SQuad q1 = new SQuad("001");
			SQuad q2 = new SQuad("030");
			SQuad q3 = new SQuad("300");
			SQuad q4 = new SQuad("1022");
			q1.shiftLeft();
			q2.shiftLeft();
			q3.shiftLeft();
			q4.shiftLeft();
			Assert.assertEquals(new SQuad("000"), q1);
			Assert.assertEquals(new SQuad("021"), q2);
			Assert.assertEquals(new SQuad("211"), q3);
			Assert.assertEquals(new SQuad("0133"), q4);
		}
		@Test public void testShiftRight(){
			SQuad q1 = new SQuad("000");
			SQuad q2 = new SQuad("021");
			SQuad q3 = new SQuad("211");
			SQuad q4 = new SQuad("0133");
			q1.shiftRight();
			q2.shiftRight();
			q3.shiftRight();
			q4.shiftRight();
			Assert.assertEquals(new SQuad("001"), q1);
			Assert.assertEquals(new SQuad("030"), q2);
			Assert.assertEquals(new SQuad("300"), q3);
			Assert.assertEquals(new SQuad("1022"), q4);
		}
		@Test public void testShiftUp(){
			SQuad q1 = new SQuad("002");
			SQuad q2 = new SQuad("021");
			SQuad q3 = new SQuad("300");
			SQuad q4 = new SQuad("2111");
			q1.shiftUp();
			q2.shiftUp();
			q3.shiftUp();
			q4.shiftUp();
			Assert.assertEquals(new SQuad("000"), q1);
			Assert.assertEquals(new SQuad("003"), q2);
			Assert.assertEquals(new SQuad("122"), q3);
			Assert.assertEquals(new SQuad("0333"), q4);
		}
		@Test public void testShiftDown(){
			SQuad q1 = new SQuad("000");
			SQuad q2 = new SQuad("003");
			SQuad q3 = new SQuad("122");
			SQuad q4 = new SQuad("0333");
			q1.shiftDown();
			q2.shiftDown();
			q3.shiftDown();
			q4.shiftDown();
			Assert.assertEquals(new SQuad("002"), q1);
			Assert.assertEquals(new SQuad("021"), q2);
			Assert.assertEquals(new SQuad("300"), q3);
			Assert.assertEquals(new SQuad("2111"), q4);
		}
		@Test public void testGetAtLevel(){
			String s1 = "2123";
			SQuad q = new SQuad(s1);
			
			Assert.assertEquals(new SQuad("21"), q.getAtLevel(2));
			Assert.assertEquals(new SQuad("21230"), q.getAtLevel(5));
			Assert.assertEquals(new SQuad("21230000000000000000000000000"), q.getAtLevel(29));
			
			SQuad q1 = new SQuad("03133333");
			Assert.assertEquals(q1.getLevel(), 8);
			SQuad q2 = q1.getAtLevel(1);
			SQuad q3 = q1.getAtLevel(3);
			SQuad q12 = q1.getAtLevel(12);
			Assert.assertEquals(q2.toMicrosoftStyleString(), "0");
			Assert.assertEquals(q3.toMicrosoftStyleString(), "031");
			Assert.assertEquals(q12.toMicrosoftStyleString(), "031333330000");
		}
		@Test public void testGetParent(){
			String s1 = "0123";
			String s2 = "0123123";
			SQuad q = new SQuad(s1);
			SQuad q1 = new SQuad(s2);
			
			Assert.assertEquals("012", q.getParent().toMicrosoftStyleString());
			Assert.assertEquals("012312", q1.getParent().toMicrosoftStyleString());
			
			Assert.assertEquals(new SQuad("012"), q.getParent());
			Assert.assertEquals(new SQuad("012312"), q1.getParent());
		}
		@Test public void testIsXYEven(){
			SQuad q1 = new SQuad("000");
			SQuad q2 = new SQuad("003");
			SQuad q3 = new SQuad("122");
			SQuad q4 = new SQuad("0331");
			Assert.assertEquals(true, q1.isXEven());
			Assert.assertEquals(false, q2.isXEven());
			Assert.assertEquals(true, q3.isXEven());
			Assert.assertEquals(false, q4.isXEven());
			
			Assert.assertEquals(true, q1.isYEven());
			Assert.assertEquals(false, q2.isYEven());
			Assert.assertEquals(false, q3.isYEven());
			Assert.assertEquals(true, q4.isYEven());
		}
		@Test public void testCompareTo(){
			SQuad q0 = new SQuad("0320");
			SQuad q1 = new SQuad("032");
			Assert.assertTrue(q1.compareTo(q0) < 0);
			
			SQuad q2 = new SQuad("33100");
			SQuad q3 = new SQuad("3310000");
			Assert.assertTrue(q2.compareTo(q3) < 0);
		}
		@Test public void testReplaceQWithEmptyStringQuad(){
			SQuad q = new SQuad("q");
			Assert.assertEquals("", q.toMicrosoftStyleString());
		}
		@Test public void testGetLastPossibleSubQuadFromStringQuad(){
			String s1 = "11323";
			Assert.assertEquals("11323333333333333333333333333", SQuad.getLastPossibleSubQuadFromString(s1));
		}
		
		@Test public void testGetDescendantAtLevel(){
			SQuad q = new SQuad("13102");
			System.out.println(q.getLevel());
			Assert.assertEquals(16, q.getDescendantAtLevel(7).size());
		}
	}
	
	public static void main(String[] args){
		SQuad q0 = new SQuad(4L, 13, 15);
		SQuad q1 = new SQuad("3323");
	
		List<SQuad> children = q1.getChildren();
		
		for (SQuad child : children) {
			System.out.println("Child: " + Long.toBinaryString(child.bits));
		}		
	}
}