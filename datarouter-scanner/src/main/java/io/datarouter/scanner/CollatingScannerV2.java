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
package io.datarouter.scanner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollatingScannerV2<T> extends BaseScanner<T>{
	private static final Logger logger = LoggerFactory.getLogger(CollatingScannerV2.class);

	private final Comparator<? super Scanner<T>> scannerComparator;
	// This array should remain sorted by the first item in each Scanner
	private final Scanner<T>[] scanners;
	private int numScanners;
	private boolean closed;

	@SuppressWarnings("unchecked")
	public CollatingScannerV2(List<Scanner<T>> inputs, Comparator<? super T> comparator){
		scannerComparator = Comparator.comparing(Scanner::current, comparator);
		scanners = Scanner.of(inputs)
				.include(Scanner::advance)
				.sort(scannerComparator)
				.list()
				.toArray(new Scanner[0]);
		numScanners = scanners.length;
		closed = false;
	}

	@Override
	public boolean advance(){
		if(closed){
			return false;
		}
		if(numScanners == 0){
			return false;
		}
		current = scanners[0].current();
		if(scanners[0].advance()){
			if(numScanners > 1){
				updateHead();
			}
		}else{
			removeHead();
		}
		return true;
	}

	private void updateHead(){
		int newIndex = findNewIndex();
		if(newIndex > 0){
			moveHeadToIndex(newIndex);
		}
	}

	private int findNewIndex(){
		Scanner<T> head = scanners[0];
		// Important optimization.
		// Assumes input scanners will frequently have multiple consecutive elements originating from the same scanner.
		// The penalty for having completely randomized data should be small.
		if(scannerComparator.compare(head, scanners[1]) <= 0){
			return 0;
		}
		// fromIndex=2 because we already looked at index 1
		int index = Arrays.binarySearch(scanners, 2, numScanners, head, scannerComparator);
		return index >= 0 ? index - 1 : -index - 2;
	}

	private void moveHeadToIndex(int index){
		Scanner<T> head = scanners[0];
		for(int i = 0; i < index; ++i){
			scanners[i] = scanners[i + 1];
		}
		scanners[index] = head;
	}

	private void removeHead(){
		scanners[0].close();
		--numScanners;
		for(int i = 0; i < numScanners; ++i){
			scanners[i] = scanners[i + 1];
		}
	}

	@Override
	public void close(){
		if(closed){
			return;
		}
		for(int i = 0; i < numScanners; ++i){
			try{
				scanners[i].close();
			}catch(Exception e){
				logger.warn("scanner exception on input.close", e);
			}
		}
		closed = true;
	}

}
