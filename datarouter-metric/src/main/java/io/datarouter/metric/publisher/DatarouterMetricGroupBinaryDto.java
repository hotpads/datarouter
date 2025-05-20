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

import java.util.List;

import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;

public class DatarouterMetricGroupBinaryDto extends BinaryDto<DatarouterMetricGroupBinaryDto>{

	@BinaryDtoField(index = 0)
	public final String environment;
	@BinaryDtoField(index = 1)
	public final String serviceName;
	@BinaryDtoField(index = 2)
	public final String serverName;
	@BinaryDtoField(index = 3)
	public final Long periodStartMs;
	@BinaryDtoField(index = 4)
	public final List<DatarouterCountBinaryDto> counts;
	@BinaryDtoField(index = 5)
	public final List<DatarouterGaugeBinaryDto> gauges;
	@BinaryDtoField(index = 6)
	public final List<DatarouterMeasurementBinaryDto> measurementBatches;
	@BinaryDtoField(index = 7)
	public final Integer random;

	public DatarouterMetricGroupBinaryDto(
			String environment,
			String serviceName,
			String serverName,
			Long periodStartMs,
			List<DatarouterCountBinaryDto> counts,
			List<DatarouterGaugeBinaryDto> gauges,
			List<DatarouterMeasurementBinaryDto> measurementBatches,
			Integer random){
		this.environment = environment;
		this.serviceName = serviceName;
		this.serverName = serverName;
		this.periodStartMs = periodStartMs;
		this.counts = counts;
		this.gauges = gauges;
		this.measurementBatches = measurementBatches;
		this.random = random;
	}

	//TODO convert to double values
	public static class DatarouterCountBinaryDto extends BinaryDto<DatarouterCountBinaryDto>{
		@BinaryDtoField(index = 0)
		public final String metricName;
		@BinaryDtoField(index = 1)
		public final Long value; //TODO rename to sum

		public DatarouterCountBinaryDto(String name, Long value){
			this.metricName = name;
			this.value = value;
		}
	}

	//TODO convert to double values
	public static class DatarouterGaugeBinaryDto extends BinaryDto<DatarouterGaugeBinaryDto>{
		@BinaryDtoField(index = 0)
		public final String metricName;
		@BinaryDtoField(index = 1)
		public final Long sum;
		@BinaryDtoField(index = 2)
		public final Long count;
		@BinaryDtoField(index = 3)
		public final Long min;
		@BinaryDtoField(index = 4)
		public final Long max;

		public DatarouterGaugeBinaryDto(String metricName, Long sum, Long count, Long min, Long max){
			this.metricName = metricName;
			this.sum = sum;
			this.count = count;
			this.min = min;
			this.max = max;
		}
	}

	//TODO convert to double values
	public static class DatarouterMeasurementBinaryDto extends BinaryDto<DatarouterMeasurementBinaryDto>{
		@BinaryDtoField(index = 0)
		public final Long batchId;
		@BinaryDtoField(index = 1)
		public final String metricName;
		@BinaryDtoField(index = 2)
		public final long[] values;

		public DatarouterMeasurementBinaryDto(Long batchId, String metricName, long[] values){
			this.batchId = batchId;
			this.metricName = metricName;
			this.values = values;
		}
	}

}
