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
package io.datarouter.model.util;

public class CommonFieldSizes{

	/*----------- memcached -----------*/

	public static final int MEMCACHED_MAX_KEY_LENGTH = 250;

	/*----------- mysql -----------*/

	// ERROR 1071 (42000): Specified key was too long; max key length is 767 bytes
	public static final int MAX_KEY_LENGTH = 767;
	// 767 / 4
	public static final int MAX_KEY_LENGTH_UTF8MB4 = 191;
	public static final int LENGTH_50 = 50;
	public static final int DEFAULT_LENGTH_VARCHAR = (1 << 8) - 1;
	public static final int MAX_LENGTH_VARBINARY = 767;
	public static final int MAX_LENGTH_LONGBLOB = (1 << 24) - 1;
	public static final int MAX_LENGTH_TEXT = (1 << 16) - 1;
	public static final int MAX_LENGTH_MEDIUMTEXT = (1 << 24) - 1;
	// use this to get schema-update to create a LONGTEXT field
	public static final int INT_LENGTH_LONGTEXT = Integer.MAX_VALUE;
	public static final long MAX_LENGTH_LONGTEXT = (1L << 32) - 1;

	/*----------- spanner -----------*/

	public static final int MAX_CHARACTERS_SPANNER = 2_621_440;

}
