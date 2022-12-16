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
package io.datarouter.bytes.codec.bytestringcodec;

import io.datarouter.bytes.codec.stringcodec.StringCodec;

public class HexByteStringCodec implements ByteStringCodec{

	public static final HexByteStringCodec INSTANCE = new HexByteStringCodec();

	private static final byte[] HEX_DIGITS = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private static final int[] VALUE_BY_HEX_DIGIT = new int['f' + 1];
	static{
		for(int value = 0; value < HEX_DIGITS.length; ++value){
			int index = HEX_DIGITS[value];
			VALUE_BY_HEX_DIGIT[index] = value;
		}
	}

	@Override
	public String encode(byte[] bytes){
		byte[] hexBytes = new byte[2 * bytes.length];
		int cursor = 0;
		for(int i = 0; i < bytes.length; i++){
			hexBytes[cursor] = HEX_DIGITS[(0xF0 & bytes[i]) >>> 4];
			++cursor;
			hexBytes[cursor] = HEX_DIGITS[0x0F & bytes[i]];
			++cursor;
		}
		return StringCodec.US_ASCII.decode(hexBytes);
	}

	@Override
	public byte[] decode(String hex){
		int hexLength = hex.length();
		if(hexLength % 2 != 0){
			throw new IllegalArgumentException("String length must be a multiple of 2");
		}
		int bytesLength = hexLength / 2;
		byte[] bytes = new byte[bytesLength];
		int hexCursor = 0;
		int bytesCursor = 0;
		while(hexCursor < hexLength){
			char leftChar = hex.charAt(hexCursor);
			++hexCursor;
			validateChar(leftChar);
			char rightChar = hex.charAt(hexCursor);
			++hexCursor;
			validateChar(rightChar);
			int leftPart = VALUE_BY_HEX_DIGIT[leftChar] << 4;
			int rightPart = VALUE_BY_HEX_DIGIT[rightChar];
			int byteValue = leftPart + rightPart;
			bytes[bytesCursor] = (byte)byteValue;
			++bytesCursor;
		}
		return bytes;
	}

	private static final void validateChar(char ch){
		boolean validDigit = ch >= '0' && ch <= '9';
		boolean validChar = ch >= 'a' && ch <= 'f';
		if(!(validDigit || validChar)){
			throw new IllegalArgumentException("Invalid character=" + ch);
		}
	}

}
