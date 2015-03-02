package com.hotpads.util.core.bytes;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public enum ByteUnitType {
	BYTE(1l, ByteUnitName.BYTE_DEC),
	KB(BYTE.numBytes * ByteUnitName.BYTE_DEC.unitSystem.step,  ByteUnitName.KB), 
	MB(  KB.numBytes * ByteUnitName.MB.unitSystem.step,  ByteUnitName.MB),
	GB(  MB.numBytes * ByteUnitName.GB.unitSystem.step,  ByteUnitName.GB),
	TB(  GB.numBytes * ByteUnitName.TB.unitSystem.step,  ByteUnitName.TB),
	PB(  TB.numBytes * ByteUnitName.PB.unitSystem.step,  ByteUnitName.PB),
			
	KiB(BYTE.numBytes * ByteUnitName.BYTE_BIN.unitSystem.step,  ByteUnitName.KiB), 
	MiB( KiB.numBytes * ByteUnitName.MiB.unitSystem.step,  ByteUnitName.MiB),
	GiB( MiB.numBytes * ByteUnitName.GiB.unitSystem.step,  ByteUnitName.GiB),
	TiB( GiB.numBytes * ByteUnitName.TiB.unitSystem.step,  ByteUnitName.TiB),
	PiB( TiB.numBytes * ByteUnitName.PiB.unitSystem.step,  ByteUnitName.PiB),
	;
		
	private ByteUnitType(long numBytes, ByteUnitName unitName) {
		this.numBytes = numBytes;
		this.unitName = unitName;
	}

	public enum ByteUnitSystem {
		DECIMAL(1000),
		BINARY(1024);

		private long step;

		private ByteUnitSystem(long step) {
			this.step = step;
		}

		public long getStep() {
			return step;
		}
	}
		
	public enum ByteUnitName {
		BYTE_DEC("B", "byte", ByteUnitSystem.DECIMAL), 
		KB("KB", "kilobyte", ByteUnitSystem.DECIMAL), 
		MB("MB", "megabyte", ByteUnitSystem.DECIMAL), 
		GB("GB", "gigabyte", ByteUnitSystem.DECIMAL), 
		TB("TB", "terabyte", ByteUnitSystem.DECIMAL), 
		PB("PB", "petabyte", ByteUnitSystem.DECIMAL),
		
		BYTE_BIN("B", "byte", ByteUnitSystem.BINARY),
		KiB("KiB", "kibibyte", ByteUnitSystem.BINARY), 
		MiB("MiB", "mebibyte", ByteUnitSystem.BINARY), 
		GiB("GiB", "gibibyte", ByteUnitSystem.BINARY), 
		TiB("TiB", "tebibyte", ByteUnitSystem.BINARY), 
		PiB("PiB", "pebibyte", ByteUnitSystem.BINARY),
		;
			
		private ByteUnitName(String shortName, String longName, ByteUnitSystem unitSystem) {
			this.shortName = shortName;
			this.longName = longName;
			this.unitSystem = unitSystem;
		}

		private String shortName;
		private String longName;
		private ByteUnitSystem unitSystem;

		public String getShortName() {
			return shortName;
		}

		public String getLongName() {
			return longName;
		}

		public ByteUnitSystem getByteUnitSystem() {
			return unitSystem;
		}
	}

	private long numBytes;
	private ByteUnitName unitName;

	private static final ByteUnitType[] BIN_SORTED_DESC = { PiB, TiB, GiB, MiB, KiB, BYTE };
	private static final ByteUnitType[] BIN_SORTED_ASC = { BYTE, KiB, MiB, GiB, TiB, PiB };
	private static final ByteUnitType[] DEC_SORTED_DESC = { PB, TB, GB, MB, KB, BYTE };
	private static final ByteUnitType[] DEC_SORTED_ASC = { BYTE, KB, MB, GB, TB, PB };

	private static final ByteUnitSystem DEFAULT_UNIT_SYSTEM = ByteUnitSystem.BINARY;
	private static final DecimalFormat DEFAULT_FORMAT = new DecimalFormat("#,##0.00");
	static {
		DEFAULT_FORMAT.setRoundingMode(RoundingMode.FLOOR);
	}

	/** getters ***************************************************************************************************/

	public static ByteUnitType[] getDescValues(ByteUnitSystem byteUnitSystem) {
		if (byteUnitSystem == null) {
			byteUnitSystem = DEFAULT_UNIT_SYSTEM;
		}
		if (ByteUnitSystem.DECIMAL == byteUnitSystem) {
			return DEC_SORTED_DESC;
		} else {
			return BIN_SORTED_DESC;
		}
	}

	public static ByteUnitType[] getAscValues(ByteUnitSystem byteUnitSystem) {
		if (byteUnitSystem == null) {
			byteUnitSystem = DEFAULT_UNIT_SYSTEM;
		}
		if (ByteUnitSystem.DECIMAL == byteUnitSystem) {
			return DEC_SORTED_ASC;
		} else {
			return BIN_SORTED_ASC;
		}
	}

	public static String getNumBytesDisplay(long value, ByteUnitType toByteUnit) {
		if (value < 0 || toByteUnit == null) {
			return null;
		}
		return DEFAULT_FORMAT.format(getNumBytes(value, toByteUnit)) + " "
				+ toByteUnit.getByteUnitName().getShortName();
	}

	public static double getNumBytes(long numBytes, ByteUnitType toByteUnit) {
		if (numBytes < 0 || toByteUnit == null) {
			return -1;
		}
		return ((double) numBytes / (double) toByteUnit.getNumBytes());
	}

	public String getNumBytesDisplay(long value) {
		return getNumBytesDisplay(value, this);
	}

	public double getNumBytes(long value) {
		return getNumBytes(value, this);
	}

	public long getNumBytes() {
		return numBytes;
	}

	public ByteUnitName getByteUnitName() {
		return unitName;
	}
}
