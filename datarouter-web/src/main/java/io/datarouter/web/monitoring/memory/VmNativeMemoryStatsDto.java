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
package io.datarouter.web.monitoring.memory;

import io.datarouter.enums.DisplayablePersistentString;

public class VmNativeMemoryStatsDto{

	public final VmNativeMemoryStatsCategory category;
	public final long reservedMemoryBytes;
	public final long committedMemoryBytes;

	public VmNativeMemoryStatsDto(
			VmNativeMemoryStatsCategory category,
			long reservedMemoryBytes,
			long committedMemoryBytes){
		this.category = category;
		this.reservedMemoryBytes = reservedMemoryBytes;
		this.committedMemoryBytes = committedMemoryBytes;
	}

	public enum VmNativeMemoryStatsCategory implements DisplayablePersistentString{
		ARENA_CHUNK("Arena Chunk", "arenaChunk"),
		ARGUMENTS("Arguments", "arguments"),
		CLASS("Class", "class"),
		CODE("Code", "code"),
		COMPILER("Compiler", "compiler"),
		GC("GC", "gc"),
		INTERNAL("Internal", "internal"),
		JAVA_HEAP("Java Heap", "heap"),
		LOGGING("Logging", "logging"),
		MODULE("Module", "module"),
		NATIVE_MEMORY_TRACKING("Native Memory Tracking", "nativeMemoryTracking"),
		OTHER("Other", "other"),
		SAFEPOINT("Safepoint", "safepoint"),
		SHARED_CLASS_SPACE("Shared class space", "sharedClassSpace"),
		SYMBOL("Symbol", "symbol"),
		SYNCHRONIZATION("Synchronization", "synchronization"),
		THREAD("Thread", "thread"),
		TOTAL("Total", "total"),
		;

		private final String display;
		private final String persistentString;

		VmNativeMemoryStatsCategory(String display, String persistentString){
			this.display = display;
			this.persistentString = persistentString;
		}

		@Override
		public String getDisplay(){
			return display;
		}

		@Override
		public String getPersistentString(){
			return persistentString;
		}

		public static VmNativeMemoryStatsCategory fromDisplay(String displayString){
			for(VmNativeMemoryStatsCategory enumEntry : values()){
				String display = enumEntry.getDisplay();
				if(display == null){
					if(displayString == null){
						return enumEntry;
					}
					continue;
				}
				if(display.equals(displayString)
						|| display.equalsIgnoreCase(displayString)){
					return enumEntry;
				}
			}
			return null;
		}

	}

}
