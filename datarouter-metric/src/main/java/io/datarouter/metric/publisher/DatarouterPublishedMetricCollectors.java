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
package io.datarouter.metric.publisher;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod;
import io.datarouter.scanner.Scanner;

public class DatarouterPublishedMetricCollectors{

	private static final List<BaseDatarouterMetricCollector> ALL = new ArrayList<>();

	public static void register(BaseDatarouterMetricCollector collector){
		ALL.add(collector);
	}

	public static List<PublishedMetricPeriod> poll(){
		return Scanner.of(ALL)
				.concatOpt(BaseDatarouterMetricCollector::poll)
				.list();
	}

}