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
package io.datarouter.filesystem.util;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.scanner.BaseLinkedScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.Pathbean;

public class PathbeanBatchingScanner extends BaseLinkedScanner<Pathbean,List<Pathbean>>{

	private final long maxBytes;
	private final int maxPathbeans;

	private List<Pathbean> batch = new ArrayList<>();
	private long batchBytes = 0;
	private boolean finished = false;

	public PathbeanBatchingScanner(Scanner<Pathbean> input, long maxBytes, int maxPathbeans){
		super(input);
		this.maxBytes = maxBytes;
		this.maxPathbeans = maxPathbeans;
	}

	@Override
	public boolean advanceInternal(){
		if(finished){
			return false;
		}
		boolean advanced = false;
		while(input.advance()){
			Pathbean pathbean = input.current();
			long numBytes = pathbean.getSize();

			//batching conditions
			long wouldBeBytes = batchBytes + numBytes;
			int wouldBePathbeans = batch.size() + 1;
			boolean hasAtLeastOne = !batch.isEmpty();
			boolean exceedsMaxBytes = wouldBeBytes > maxBytes;
			boolean exceedsMaxPathbeans = wouldBePathbeans > maxPathbeans;
			boolean exceedsMax = exceedsMaxBytes || exceedsMaxPathbeans;

			//flush batch
			if(hasAtLeastOne && exceedsMax){
				current = batch;
				batch = new ArrayList<>();
				batchBytes = 0;
				advanced = true;
			}

			//accumulate input
			batch.add(pathbean);
			batchBytes += numBytes;

			if(advanced){
				//the batch was full before accumulating this input
				return true;
			}
		}

		finished = true;
		current = batch;
		return !current.isEmpty();
	}

}
