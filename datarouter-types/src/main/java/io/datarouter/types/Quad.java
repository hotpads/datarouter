/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a Microsoft style String "quad" like 03112003 in a 64 bit long.
 *
 * https://learn.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system
 *
 * The leftmost bit is blank.  The last 5 bits represent the level which can be 0 through 29.
 *
 * The 58 remaining "grid" bits are left justified so that the "bits" field can also be used as an efficient hashCode
 *
 * y is the direction that is shifted left one bit
 *
 * Natural ordering is the same as ordering Microsoft style quad strings
*/
public record Quad(
		long bits)
implements Comparable<Quad>{

	private static final long LONG_ALL_BITS = -1;
	private static final long ONE_MASK = 1L;
	private static final long TWO_MASK = 2L;

	private static final String TOP_QUAD_STRING = "q";

	public static final Quad TOP_QUAD = new Quad();
	public static final Quad EMPTY_STRING_QUAD = new Quad("");

	private static final int NUM_EMPTY_BITS = 1;
	private static final int NUM_BITS_IN_LONG = 64;
	private static final int NUM_LEVELS_AFTER_ZERO = 28; // there's also level 0 which has an empty quad code
	private static final int NUM_GRID_BITS = 29 * 2; // one bit for each of x and y on each level
	private static final int NUM_LEVEL_BITS = 5; // basically 29<<5

	private static final long ZERO_LEVEL_BITS_WITH_ALL_GRID_BITS = setLeftBits(NUM_GRID_BITS + 1);
	private static final long MAX_LEVEL_BITS_WITH_NO_GRID_BITS = 29L;

	private static final String EIGHT_ZERO_STRING = "00000000";
	public static final String SIXTEEN_ZERO_STRING = EIGHT_ZERO_STRING + EIGHT_ZERO_STRING;

	public static final int RIGHT_BIT_TABLE_MASK = 14;
	public static final int[] RIGHT_BIT_LOOK_UP_TABLE = new int[RIGHT_BIT_TABLE_MASK + 1];

	static{
		int jval = 1;
		RIGHT_BIT_LOOK_UP_TABLE[0] = 0;
		RIGHT_BIT_LOOK_UP_TABLE[1] = 1;
		for(int i = 2; i <= RIGHT_BIT_TABLE_MASK; i++){
			RIGHT_BIT_LOOK_UP_TABLE[i] = RIGHT_BIT_LOOK_UP_TABLE[i - 1] + jval * 2;
			jval *= 2;
		}
	}

	/*-------------- construct ---------------*/

	public Quad(){
		this(TOP_QUAD_STRING);
	}

	public Quad(long bits){
		this.bits = bits;
	}

	public Quad(long level, long xPosition, long yPosition){
		this(levelXyToBits(level, xPosition, yPosition));
	}

	/* for microsoft style strings like 31101201 */
	public Quad(String str){
		this(stringQuadToBits(str));
	}

	/*------------- static construct -------*/

	private static long levelXyToBits(long level, long xPosition, long yPosition){
		if(level < 15){
			long rightXBits = xPosition & RIGHT_BIT_LOOK_UP_TABLE[(int)level];
			long dilatedRightXBits = DilatedBits.DILATED_TABLE[(int)rightXBits];
			long rightYBits = yPosition & RIGHT_BIT_LOOK_UP_TABLE[(int)level];
			long dilatedRightYBits = DilatedBits.LEFT_SHIFTED_DILATED_TABLE[(int)rightYBits];
			return level | dilatedRightXBits << NUM_GRID_BITS + NUM_LEVEL_BITS - level * 2
					| dilatedRightYBits << NUM_GRID_BITS + NUM_LEVEL_BITS - level * 2;
		}else{
			long shift = level - 15;

			// split x into left and right HALF_GRID_BITS(14) bits because that's the width of our dilation lookup table
			long leftXBits = xPosition >>> shift;
			long dilatedLeftXBits = DilatedBits.DILATED_TABLE[(int)leftXBits];
			long rightXBits = xPosition & RIGHT_BIT_LOOK_UP_TABLE[(int)shift];
			long dilatedRightXBits = DilatedBits.DILATED_TABLE[(int)rightXBits];

			// do the same for y as x, but use the left shifted table
			long leftYBits = yPosition >>> shift;
			long dilatedLeftYBits = DilatedBits.LEFT_SHIFTED_DILATED_TABLE[(int)leftYBits];
			long rightYBits = yPosition & RIGHT_BIT_LOOK_UP_TABLE[(int)shift];
			long dilatedRightYBits = DilatedBits.LEFT_SHIFTED_DILATED_TABLE[(int)rightYBits];
			return level | dilatedLeftXBits << NUM_LEVELS_AFTER_ZERO + NUM_LEVEL_BITS
					| dilatedRightXBits << NUM_LEVELS_AFTER_ZERO + NUM_LEVEL_BITS - shift * 2
					| dilatedLeftYBits << NUM_LEVELS_AFTER_ZERO + NUM_LEVEL_BITS
					| dilatedRightYBits << NUM_LEVELS_AFTER_ZERO + NUM_LEVEL_BITS - shift * 2;
		}
	}

	private static long stringQuadToBits(String str){
		if(str == null || str.isEmpty() || TOP_QUAD_STRING.equals(str)){
			return 0;
		}
		long workingBits;
		long length = str.length();
		long xyBits = 0;
		workingBits = length;
		for(int i = 1; i <= length; ++i){
			if(str.charAt(i - 1) == '0'){
				// nothing
			}else if(str.charAt(i - 1) == '1'){
				xyBits |= 1L << NUM_GRID_BITS + NUM_LEVEL_BITS - 2 * i;
			}else if(str.charAt(i - 1) == '2'){
				xyBits |= 2L << NUM_GRID_BITS + NUM_LEVEL_BITS - 2 * i;
			}else if(str.charAt(i - 1) == '3'){
				xyBits |= 3L << NUM_GRID_BITS + NUM_LEVEL_BITS - 2 * i;
			}
		}
		workingBits |= xyBits;
		return workingBits;
	}

	/*----------- methods ------------*/

	public long getNumQuadsAcross(){
		long toBeShifted = 1;
		return toBeShifted << getLevel();
	}

	public boolean isRoot(){
		return getLevel() == 0;
	}

	public Quad getAtLevel(int level){
		long levelBits = levelBitsAtLevel(level);
		long formattedGridBitsAtLevel = gridBitsAtLevel(level) << NUM_GRID_BITS + NUM_LEVEL_BITS - level * 2;
		return new Quad(levelBits | formattedGridBitsAtLevel);
	}

	// get the xxxxxxx333333333 quad on level 29
	public Quad getLastPossibleSubQuad(){
		int shift = NUM_BITS_IN_LONG - NUM_EMPTY_BITS - getLevel() * 2;
		long levelRemoved = getGridBits() << shift;
		long lowerLevelsFilledIn = levelRemoved | setRightBits(shift);
		long clearLevelBits = lowerLevelsFilledIn & ZERO_LEVEL_BITS_WITH_ALL_GRID_BITS;
		long maxLevelApplied = clearLevelBits | MAX_LEVEL_BITS_WITH_NO_GRID_BITS;
		return new Quad(maxLevelApplied);
	}

	public Quad getLastPossibleSubQuadAtLevel(int level){
		int shift = NUM_BITS_IN_LONG - NUM_EMPTY_BITS - getLevel() * 2;
		long levelRemoved = getGridBits() << shift;
		long lowerLevelsFilledIn = levelRemoved | setRightBits(shift);
		long clearLevelBits = lowerLevelsFilledIn & setLeftBits(2 * level + 1);
		long maxLevelApplied = clearLevelBits | level;
		return new Quad(maxLevelApplied);
	}

	/*-------------- level, x, y ---------------*/

	public int getLevel(){
		return (int)(bits & RIGHT_BIT_LOOK_UP_TABLE[5]);
	}

	/* start at the right and move left through the x grid bits, adding them up each time. not optimal */
	public long getX(){
		long xposition = 0;
		long level = getLevel();
		long shift = NUM_GRID_BITS + NUM_LEVEL_BITS - level * 2;
		for(short bit = 0; bit < NUM_GRID_BITS; bit += 2){
			long mask = 1L << bit;
			long workingBit = bits >>> shift & mask;
			long contractedWorkingBit = workingBit >>> bit / 2;
			xposition += contractedWorkingBit;
		}
		return xposition;
	}

	public long getY(){
		long yposition = 0, shift;
		long level = getLevel();
		shift = NUM_GRID_BITS + NUM_LEVEL_BITS - level * 2;
		for(short bit = 1; bit < NUM_GRID_BITS; bit += 2){
			long mask = 1L << bit;
			long workingBit = bits >>> shift & mask;
			long contractedWorkingBit = workingBit >>> bit / 2 + 1; // shift one extra right because y is offset 1 left
			yposition += contractedWorkingBit;
		}
		return yposition;
	}

	public boolean isXEven(){
		return (getGridBits() & ONE_MASK) != ONE_MASK;
	}

	public boolean isYEven(){
		return (getGridBits() & TWO_MASK) != TWO_MASK;
	}

	/*--------------- bits -------------*/

	private long gridBitsAtLevel(int level){
		int levelsDeeper = level - this.getLevel();
		if(levelsDeeper > 0){
			return getGridBits() << 2 * levelsDeeper;
		}else if(levelsDeeper < 0){
			return getGridBits() >>> -2 * levelsDeeper;
		}else{
			return getGridBits();
		}
	}

	public long getGridBits(){
		long gridBits;
		long shift = NUM_GRID_BITS + NUM_LEVEL_BITS - getLevel() * 2;
		gridBits = bits >> shift;
		return gridBits;
	}

	private long levelBitsAtLevel(int level){
		return level;
	}

	/*------------- relatives ------------*/

	public Quad getParent(){
		return getAtLevel(this.getLevel() - 1);
	}

	public List<Quad> getChildren(){
		int shift = NUM_BITS_IN_LONG - NUM_EMPTY_BITS - getLevel() * 2;
		long gridBits = getGridBits();
		long length = getLevel();
		long currentChild = ++length;

		gridBits = gridBits << 2;
		long formattedGridBits = gridBits << shift - 2;
		currentChild |= formattedGridBits;

		List<Quad> children = new ArrayList<>(4);
		for(int i = 0; i < 4; i++){
			children.add(new Quad(currentChild));
			currentChild = length;
			gridBits++;
			formattedGridBits = gridBits << shift - 2;
			currentChild |= formattedGridBits;
		}
		return children;
	}

	public List<Quad> getDescendantAtLevel(int level){
		List<Quad> descendant = new ArrayList<>();
		int tempLevel = getLevel();
		if(tempLevel < level){
			List<Quad> children = getChildren();
			for(Quad child : children){
				descendant.addAll(child.getDescendantAtLevel(level));
			}
		}else if(tempLevel == level){
			descendant.add(this);
		}
		return descendant;
	}

	public List<Quad> get3Siblings(){
		if(isRoot()){
			return null;
		}
		int shift = NUM_BITS_IN_LONG - NUM_EMPTY_BITS - getLevel() * 2;
		// long gridBits = getGridBits();
		List<Quad> siblings = new ArrayList<>(3);
		long currentSibling = (getGridBits() | 3L) - 3L;
		for(int i = 0; i < 4; i++){
			if(getGridBits() != currentSibling){
				long formattedSibling = currentSibling << shift | getLevel();
				siblings.add(new Quad(formattedSibling));
			}
			currentSibling++;
		}
		return siblings;
	}

	public boolean isParentOfOrEqualTo(Quad other){
		if(getLevel() > other.getLevel()){
			return false;
		}
		return gridBitsAtLevel(getLevel()) == other.gridBitsAtLevel(getLevel());
	}

	public boolean isChildOfOrEqualTo(Quad other){
		if(getLevel() < other.getLevel()){
			return false;
		}
		return gridBitsAtLevel(other.getLevel()) == other.getGridBits();
	}

	public boolean isChildOf(Quad other){
		if(getLevel() <= other.getLevel()){
			return false;
		}
		return gridBitsAtLevel(other.getLevel()) == other.getGridBits();
	}

	/*--------------- shift -------------*/

	public Quad shiftLeft(){
		long distance = 0L;
		int currentBit = 0;
		int shift = NUM_BITS_IN_LONG - NUM_EMPTY_BITS - getLevel() * 2;
		while((getGridBits() >> currentBit & ONE_MASK) == 0){
			distance = distance << 1;
			distance += 1;
			distance = distance << 1;
			currentBit += 2;
		}
		distance += 1;
		long newBits = getGridBits() - distance << shift | getLevel();
		return new Quad(newBits);
	}

	public Quad shiftRight(){
		long distance = 0L;
		int currentBit = 0;
		int shift = NUM_BITS_IN_LONG - NUM_EMPTY_BITS - getLevel() * 2;
		while((getGridBits() >> currentBit & ONE_MASK) == 1){
			distance = distance << 1;
			distance += 1;
			distance = distance << 1;
			currentBit += 2;
		}
		distance += 1;
		long newBits = getGridBits() + distance << shift | getLevel();
		return new Quad(newBits);
	}

	public Quad shiftUp(){
		long oneMask = 1L;
		long distance = 0L;
		int currentBit = 1;
		int shift = NUM_BITS_IN_LONG - NUM_EMPTY_BITS - getLevel() * 2;
		while((getGridBits() >> currentBit & oneMask) == 0){
			distance = distance << 1;
			distance += 1;
			distance = distance << 1;
			currentBit += 2;
		}
		distance += 1;
		distance = distance << 1;
		long newBits = getGridBits() - distance << shift | getLevel();
		return new Quad(newBits);
	}

	public Quad shiftDown(){
		long oneMask = 1L;
		long distance = 0L;
		int currentBit = 1;
		int shift = NUM_BITS_IN_LONG - NUM_EMPTY_BITS - getLevel() * 2;

		while((getGridBits() >> currentBit & oneMask) == 1){
			distance = distance << 1;
			distance += 1;
			distance = distance << 1;
			currentBit += 2;
		}
		distance += 1;
		distance = distance << 1;
		long newBits = getGridBits() + distance << shift | getLevel();
		return new Quad(newBits);
	}

	/*------------- string ------------*/

	public String toMicrosoftStyleString(){
		int length = getLevel();
		char[] val = new char[length];
		long xyBits = bits >>> NUM_GRID_BITS + NUM_LEVEL_BITS - length * 2;
		for(int i = 0; i < length; ++i){
			long twoBits = xyBits >>> i * 2 & 3L;
			if(twoBits == 0){
				val[length - i - 1] = '0';
			}else if(twoBits == 1){
				val[length - i - 1] = '1';
			}else if(twoBits == 2){
				val[length - i - 1] = '2';
			}else if(twoBits == 3){
				val[length - i - 1] = '3';
			}
		}
		return new String(val);
	}

	@Deprecated
	public String getMicrosoftStyleString(){
		return toMicrosoftStyleString();
	}

	/*------------ static ---------------*/

	// get the xxxxxxx333333333 quad on level 29
	public static String getLastPossibleSubQuadFromString(String quadString){
		Quad quad = new Quad(quadString);
		return quad.getLastPossibleSubQuad().toMicrosoftStyleString();
	}

	public static List<Quad> fromMicrosoftStyleStrings(Collection<String> microsoftStrings){
		return microsoftStrings.stream()
				.map(Quad::new)
				.toList();
	}

	private static long setRightBits(int numSetBitsOnRight){
		return LONG_ALL_BITS >>> (64 - numSetBitsOnRight);
	}

	private static long setLeftBits(int numSetBitsOnLeft){
		return LONG_ALL_BITS << (64 - numSetBitsOnLeft);
	}

	/*----------- Object -------------*/

	@Override
	public String toString(){
		return toMicrosoftStyleString();
	}

	/*--------- Comparable -----------*/

	@Override
	public int compareTo(Quad other){
		if(bits < other.bits){
			return -1;
		}
		if(bits > other.bits){
			return 1;
		}
		return 0;
	}

	/*------------ DilatedBitTool --------------*/

	/**
	 *  Build the lookup tables for dilation and contraction.
	 *  Table will only be 16k values so will have to be used twice.
	 */
	public static class DilatedBits{

		// 28 is 2 * 14, and 2^14 => 16k, so our table is 16k entries
		public static final int MAX_BITS = 15;
		public static final int MAX_DILATED_BITS = 2 * MAX_BITS;

		public static final int HALF_GRID_BITS = MAX_DILATED_BITS >>> 1; // levelBits halved. 28 >>> 1 is 14
		public static final int TABLE_SLOTS = 1 << HALF_GRID_BITS;
		public static final int TABLE_MASK = TABLE_SLOTS - 1;
		public static final int[] DILATED_TABLE = new int[TABLE_SLOTS];
		public static final int[] LEFT_SHIFTED_DILATED_TABLE = new int[TABLE_SLOTS];

		static{
			for(int i = 0; i < TABLE_SLOTS; ++i){
				int dilated = 0;
				for(short shift = 0; shift < HALF_GRID_BITS; ++shift){
					int mask = 1 << shift;
					int workingBit = i & mask;
					int dilatedWorkingBit = workingBit << shift;
					dilated += dilatedWorkingBit;
					DILATED_TABLE[i] = dilated;
					int dilatedShiftedLeft = dilated << 1;
					LEFT_SHIFTED_DILATED_TABLE[i] = dilatedShiftedLeft;
				}
			}
		}

	}

}
