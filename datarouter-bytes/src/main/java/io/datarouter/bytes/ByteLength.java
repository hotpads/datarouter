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
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class ByteLength{

	public static final ByteLength MIN = ByteLength.ofBytes(0);
	public static final ByteLength MAX = ByteLength.ofBytes(Long.MAX_VALUE);

	private static final Unit[] BIN_SORTED_ASC = {Unit.B, Unit.KiB, Unit.MiB, Unit.GiB, Unit.TiB, Unit.PiB};
	private static final Unit[] DEC_SORTED_ASC = {Unit.B, Unit.KB, Unit.MB, Unit.GB, Unit.TB, Unit.PB};

	public enum Unit{
		B(1),
		KB(1_000),
		MB(KB.unitValue * 1_000),
		GB(MB.unitValue * 1_000),
		TB(GB.unitValue * 1_000),
		PB(TB.unitValue * 1_000),

		KiB(1_024),
		MiB(KiB.unitValue * 1_024),
		GiB(MiB.unitValue * 1_024),
		TiB(GiB.unitValue * 1_024),
		PiB(TiB.unitValue * 1_024);

		public final long unitValue;

		Unit(long unitValue){
			this.unitValue = unitValue;
		}

	}

	public enum ByteUnitSystem{
		DECIMAL(1000),
		BINARY(1024),
		;

		public final long step;

		ByteUnitSystem(long step){
			this.step = step;
		}

	}

	private final long length;

	private ByteLength(long length){
		this.length = length;
	}

	public long toBytes(){
		return length;
	}

	public int toBytesInt(){
		return Math.toIntExact(length);
	}

	public double toBytesDouble(){
		return length;
	}

	@Override
	public int hashCode(){
		return Objects.hash(length);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		ByteLength other = (ByteLength)obj;
		return length == other.length;
	}

	@Override
	public String toString(){
		return Long.toString(length);
	}

	public String toDisplay(){
		return toDisplay(ByteUnitSystem.BINARY);
	}

	public String toDisplay(ByteUnitSystem byteUnitSystem){
		Objects.requireNonNull(byteUnitSystem);
		Long step = byteUnitSystem.step;
		for(Unit unit : getAscValues(byteUnitSystem)){
			if(step.compareTo(Math.abs(length / unit.unitValue)) <= 0){
				continue;
			}
			return getNumBytesDisplay(unit);
		}
		if(ByteUnitSystem.BINARY == byteUnitSystem){
			return getNumBytesDisplay(Unit.PiB);
		}
		return getNumBytesDisplay(Unit.PB);
	}

	private String getNumBytesDisplay(Unit byteUnit){
		double numBytes = (double) length / (double)byteUnit.unitValue;
		var bigDecimal = new BigDecimal(numBytes).round(new MathContext(3));
		return bigDecimal.toPlainString() + " " + byteUnit;
	}

	public static Unit[] getAscValues(ByteUnitSystem byteUnitSystem){
		if(ByteUnitSystem.DECIMAL == byteUnitSystem){
			return DEC_SORTED_ASC;
		}
		return BIN_SORTED_ASC;
	}

	public static ByteLength ofBytes(long input){
		return of(input, Unit.B);
	}

	public static ByteLength ofKB(long input){
		return of(input, Unit.KB);
	}

	public static ByteLength ofMB(long input){
		return of(input, Unit.MB);
	}

	public static ByteLength ofGB(long input){
		return of(input, Unit.GB);
	}

	public static ByteLength ofTB(long input){
		return of(input, Unit.TB);
	}

	public static ByteLength ofPB(long input){
		return of(input, Unit.PB);
	}

	public static ByteLength ofKiB(long input){
		return of(input, Unit.KiB);
	}

	public static ByteLength ofMiB(long input){
		return of(input, Unit.MiB);
	}

	public static ByteLength ofGiB(long input){
		return of(input, Unit.GiB);
	}

	public static ByteLength ofTiB(long input){
		return of(input, Unit.TiB);
	}

	public static ByteLength ofPiB(long input){
		return of(input, Unit.PiB);
	}

	private static ByteLength of(long input, Unit unit){
		return new ByteLength(input * unit.unitValue);
	}

	public long toKB(){
		return to(Unit.KB);
	}

	public long toMB(){
		return to(Unit.MB);
	}

	public long toGB(){
		return to(Unit.GB);
	}

	public long toTB(){
		return to(Unit.TB);
	}

	public long toPB(){
		return to(Unit.PB);
	}

	public long toKiB(){
		return to(Unit.KiB);
	}

	public long toMiB(){
		return to(Unit.MiB);
	}

	public long toGiB(){
		return to(Unit.GiB);
	}

	public long toTiB(){
		return to(Unit.TiB);
	}

	public long toPiB(){
		return to(Unit.PiB);
	}

	private long to(Unit unit){
		return this.length / unit.unitValue;
	}

	/*-------- to Double precision -------*/

	private double toDouble(Unit unit){
		return (double)length / (double)unit.unitValue;
	}

	public double toTiBDouble(){
		return toDouble(Unit.TiB);
	}

	/*-------- sum -----------*/

	public static ByteLength sum(ByteLength... byteLengths){
		return sum(Arrays.asList(byteLengths));
	}

	public static ByteLength sum(Collection<ByteLength> byteLengths){
		long totalBytes = byteLengths.stream()
				.mapToLong(ByteLength::toBytes)
				.sum();
		return ByteLength.ofBytes(totalBytes);
	}

}
