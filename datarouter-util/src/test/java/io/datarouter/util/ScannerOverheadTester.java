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
package io.datarouter.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.timer.PhaseTimer;

public class ScannerOverheadTester{
	private static final Logger logger = LoggerFactory.getLogger(ScannerOverheadTester.class);

	private static final List<Integer> INTS = Scanner.iterate(0, i -> i + 1)
			.limit(3)
			.list();
	private static final int OUTER_ITERATIONS = 10;
	private static final int INNER_ITERATIONS = 10_000_000;

	@Test
	public void testOverhead(){
		for(int i = 0; i < OUTER_ITERATIONS; ++i){
			PhaseTimer timer = new PhaseTimer();

			simpleFor();
			timer.add("simpleFor");

			enhancedFor();
			timer.add("enhancedFor");

			stream();
			timer.add("stream");

			scan();
			timer.add("scan");

			logger.warn("{}", timer);
		}
	}

	private static void simpleFor(){
		for(int i = 0; i < INNER_ITERATIONS; ++i){
			List<Integer> result = new ArrayList<>();
			for(int j = 0; j < INTS.size(); ++j){
				result.add(INTS.get(j) * 2);
			}
		}
	}

	private static void enhancedFor(){
		for(int i = 0; i < INNER_ITERATIONS; ++i){
			List<Integer> result = new ArrayList<>();
			for(Integer j : INTS){
				result.add(j * 2);
			}
		}
	}

	private static void stream(){
		for(int i = 0; i < INNER_ITERATIONS; ++i){
			INTS.stream()
					.map(j -> j * 2)
					.toList();
		}
	}

	private static void scan(){
		for(int i = 0; i < INNER_ITERATIONS; ++i){
			Scanner.of(INTS).map(j -> j * 2).list();
		}
	}

}