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
package io.datarouter.bytes;

import java.math.BigDecimal;
import java.math.MathContext;

public enum ByteUnitType{
	BYTE(1L, ByteUnitName.BYTE_DEC),
	KB(BYTE.numBytes * ByteUnitName.BYTE_DEC.unitSystem.step, ByteUnitName.KB),
	MB(KB.numBytes * ByteUnitName.MB.unitSystem.step, ByteUnitName.MB),
	GB(MB.numBytes * ByteUnitName.GB.unitSystem.step, ByteUnitName.GB),
	TB(GB.numBytes * ByteUnitName.TB.unitSystem.step, ByteUnitName.TB),
	PB(TB.numBytes * ByteUnitName.PB.unitSystem.step, ByteUnitName.PB),

	KiB(BYTE.numBytes * ByteUnitName.BYTE_BIN.unitSystem.step, ByteUnitName.KiB),
	MiB(KiB.numBytes * ByteUnitName.MiB.unitSystem.step, ByteUnitName.MiB),
	GiB(MiB.numBytes * ByteUnitName.GiB.unitSystem.step, ByteUnitName.GiB),
	TiB(GiB.numBytes * ByteUnitName.TiB.unitSystem.step, ByteUnitName.TiB),
	PiB(TiB.numBytes * ByteUnitName.PiB.unitSystem.step, ByteUnitName.PiB),
	;

	private final long numBytes;
	private final ByteUnitName unitName;

	ByteUnitType(long numBytes, ByteUnitName unitName){
		this.numBytes = numBytes;
		this.unitName = unitName;
	}

	public static enum ByteUnitSystem{
		DECIMAL(1000),
		BINARY(1024),
		;

		private final long step;

		ByteUnitSystem(long step){
			this.step = step;
		}

		public long getStep(){
			return step;
		}

	}

	private static enum ByteUnitName{
		BYTE_DEC("B", ByteUnitSystem.DECIMAL),
		KB("KB", ByteUnitSystem.DECIMAL),
		MB("MB", ByteUnitSystem.DECIMAL),
		GB("GB", ByteUnitSystem.DECIMAL),
		TB("TB", ByteUnitSystem.DECIMAL),
		PB("PB", ByteUnitSystem.DECIMAL),

		BYTE_BIN("B", ByteUnitSystem.BINARY),
		KiB("KiB", ByteUnitSystem.BINARY),
		MiB("MiB", ByteUnitSystem.BINARY),
		GiB("GiB", ByteUnitSystem.BINARY),
		TiB("TiB", ByteUnitSystem.BINARY),
		PiB("PiB", ByteUnitSystem.BINARY),
		;

		private final String shortName;
		private final ByteUnitSystem unitSystem;

		ByteUnitName(String shortName, ByteUnitSystem unitSystem){
			this.shortName = shortName;
			this.unitSystem = unitSystem;
		}

		private String getShortName(){
			return shortName;
		}

	}

	public static final ByteUnitType[] BIN_SORTED_ASC = {BYTE, KiB, MiB, GiB, TiB, PiB};
	private static final ByteUnitType[] DEC_SORTED_ASC = {BYTE, KB, MB, GB, TB, PB};

	private static final ByteUnitSystem DEFAULT_UNIT_SYSTEM = ByteUnitSystem.BINARY;

	public static ByteUnitType[] getAscValues(ByteUnitSystem byteUnitSystem){
		if(byteUnitSystem == null){
			byteUnitSystem = DEFAULT_UNIT_SYSTEM;
		}
		if(ByteUnitSystem.DECIMAL == byteUnitSystem){
			return DEC_SORTED_ASC;
		}
		return BIN_SORTED_ASC;
	}

	public static String getNumBytesDisplay(long value, ByteUnitType toByteUnit){
		if(value < 0 || toByteUnit == null){
			return null;
		}
		double numBytes = getNumBytes(value, toByteUnit);
		var bigDecimal = new BigDecimal(numBytes).round(new MathContext(3));
		return bigDecimal.toPlainString() + " " + toByteUnit.getByteUnitName().getShortName();
	}

	public String getNumBytesDisplay(long value){
		return getNumBytesDisplay(value, this);
	}

	public static double getNumBytes(long numBytes, ByteUnitType toByteUnit){
		if(numBytes < 0 || toByteUnit == null){
			return -1;
		}
		return (double)numBytes / (double)toByteUnit.getNumBytes();
	}

	public long getNumBytes(){
		return numBytes;
	}

	public ByteUnitName getByteUnitName(){
		return unitName;
	}

	public long toBytes(long input){
		return input * numBytes;
	}

	public int toBytesInt(long input){
		return (int)(input * numBytes);
	}

}
