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

public enum Ascii{

	NULL(0),
	START_OF_HEADER(1),
	START_OF_TEXT(2),
	END_OF_TEXT(3),
	END_OF_TRANSMISSION(4),
	ENQUIRY(5),
	ACKNOWLEDGE(6),
	BELL(7),
	BACKSPACE(8),
	HORIZONTAL_TAB(9),
	LINE_FEED(10),
	VERTICAL_TAB(11),
	FORM_FEED(12),
	CARRIAGE_RETURN(13),
	SHIFT_OUT(14),
	SHIFT_IN(15),
	DATA_LINK_ESCAPE(16),
	DEVICE_CONTROL_1(17),
	DEVICE_CONTROL_2(18),
	DEVICE_CONTROL_3(19),
	DEVICE_CONTROL_4(20),
	NEGATIVE_ACKNOWLEDGE(21),
	SYNCHRONOUS_IDLE(22),
	END_OF_TRANS_BLOCK(23),
	CANCEL(24),
	END_OF_MEDIUM(25),
	SUBSTITUTE(26),
	ESCAPE(27),
	FULL_SEPARATOR(28),
	GROUP_SEPARATOR(29),
	RECORD_SEPARATOR(30),
	UNIT_SEPARATOR(31),

	COMMA(44),

	ZERO(48),
	;

	public final byte code;

	private Ascii(int code){
		if(code < 0 || code > 255){
			throw new IllegalArgumentException(code + " must be 0 to 255");
		}
		this.code = (byte)code;
	}

}
