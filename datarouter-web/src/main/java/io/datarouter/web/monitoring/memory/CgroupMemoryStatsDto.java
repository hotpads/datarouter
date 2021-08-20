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

import io.datarouter.util.enums.DisplayablePersistentString;

public class CgroupMemoryStatsDto{

	public final CgroupMemoryStatsCategory category;
	public final long memoryBytes;

	public CgroupMemoryStatsDto(CgroupMemoryStatsCategory category, long memoryBytes){
		this.category = category;
		this.memoryBytes = memoryBytes;
	}

	public enum CgroupMemoryStatsCategory implements DisplayablePersistentString{
		ACTIVE_ANON("Total active anon", "activeAnon"),
		ACTIVE_FILE("Total active file", "activeFile"),
		CACHE("Total cache", "cache"),
		DIRTY("Total dirty", "dirty"),
		INACTIVE_ANON("Total inactive anon", "inactiveAnon"),
		INACTIVE_FILE("Total inactive file", "inactiveFile"),
		KERNEL_USAGE("Kernel usage", "kernelUsage"),
		LIMIT("Total limit", "limit"),
		MAPPED_FILE("Total mapped file", "mappedFile"),
		PGFAULT("Total pgfault", "pgfault"),
		PGMAJFAULT("Total pgmajfault", "pgmajfault"),
		PGPGIN("Total pgpgin", "pgpgin"),
		PGPGOUT("Total pgpgout", "pgpgout"),
		RSS("Total rss", "rss"),
		RSS_HUGE("Total rss huge", "rssHuge"),
		SHMEM("Total shmem", "shmem"),
		SWAP("Total swap", "swap"),
		UNEVICTABLE("Total unevictable", "unevictable"),
		USAGE("Total usage", "usage"),
		WORKING_SET("Working set (Real usage)", "workingSet"),
		WRITEBACK("Total writeback", "writeback"),
		;

		private final String display;
		private final String persistentString;

		CgroupMemoryStatsCategory(String display, String persistentString){
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

		public static CgroupMemoryStatsCategory fromDisplay(String displayString){
			for(CgroupMemoryStatsCategory enumEntry : values()){
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
