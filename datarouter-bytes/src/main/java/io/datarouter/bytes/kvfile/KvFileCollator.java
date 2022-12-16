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
package io.datarouter.bytes.kvfile;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.datarouter.scanner.Scanner;

/**
 * Collate KvFileEntries using only the binary data, not requiring the objects to be decoded.
 *
 * Facilitates sorting much more data than fits in memory.
 */
public class KvFileCollator{

	private final List<KvFileReader> readers;

	public KvFileCollator(List<KvFileReader> readers){
		this.readers = Objects.requireNonNull(readers);
	}

	/**
	 * Merge entries keeping all versions and ops, including duplicates.
	 * Assumes input is sorted by key+version+op.
	 */
	public Scanner<KvFileEntry> mergeKeepingEverything(){
		return collateByKeyVersionOp();
	}

	/**
	 * Merge entries keeping the latest version.
	 * If lastest version is a DELETE then it's omitted.
	 * Assumes input is sorted by key+version.
	 * If multiple entries have same latest version then selection is undefined.
	 */
	public Scanner<KvFileEntry> mergeKeepingLatestVersion(){
		return collateByKeyVersionOp()
				.splitBy(Function.identity(), KvFileEntry::equalsKeyOptimized)
				.map(entries -> entries.findLast().orElseThrow())
				.exclude(entry -> entry.op() == KvFileOp.DELETE);
	}

	/**
	 * Merge entries from multiple KvFiles.
	 * The input files are assumed to be sorted by key+version+op already.
	 * If multiple values are found with the same key+version+op then all are returned but in unspecified order.
	 */
	private Scanner<KvFileEntry> collateByKeyVersionOp(){
		return Scanner.of(readers)
				.collateV2(KvFileReader::scanEntries, KvFileEntry::compareKeyVersionOpOptimized);
	}

}
