package com.hotpads.util.core.bytes;

import java.math.RoundingMode;
import java.text.DecimalFormat;

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

	private ByteUnitType(long numBytes, ByteUnitName unitName){
		this.numBytes = numBytes;
		this.unitName = unitName;
	}

	public enum ByteUnitSystem{
		DECIMAL(1000),
		BINARY(1024);

		private long step;

		private ByteUnitSystem(long step){
			this.step = step;
		}

		public long getStep(){
			return step;
		}
	}

	public enum ByteUnitName{
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

		private ByteUnitName(String shortName, ByteUnitSystem unitSystem){
			this.shortName = shortName;
			this.unitSystem = unitSystem;
		}

		private String shortName;
		private ByteUnitSystem unitSystem;

		public String getShortName(){
			return shortName;
		}

	}

	private long numBytes;
	private ByteUnitName unitName;

	private static final ByteUnitType[] BIN_SORTED_DESC = {PiB, TiB, GiB, MiB, KiB, BYTE};
	private static final ByteUnitType[] BIN_SORTED_ASC = {BYTE, KiB, MiB, GiB, TiB, PiB};
	private static final ByteUnitType[] DEC_SORTED_DESC = {PB, TB, GB, MB, KB, BYTE};
	private static final ByteUnitType[] DEC_SORTED_ASC = {BYTE, KB, MB, GB, TB, PB};

	private static final ByteUnitSystem DEFAULT_UNIT_SYSTEM = ByteUnitSystem.BINARY;
	private static final DecimalFormat DEFAULT_FORMAT = new DecimalFormat("#,##0.00");
	static{
		DEFAULT_FORMAT.setRoundingMode(RoundingMode.FLOOR);
	}

	/** getters ***************************************************************************************************/

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
		return DEFAULT_FORMAT.format(getNumBytes(value, toByteUnit)) + " " + toByteUnit.getByteUnitName()
				.getShortName();
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
}
