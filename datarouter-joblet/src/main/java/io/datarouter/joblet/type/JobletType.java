/**
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
package io.datarouter.joblet.type;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Supplier;

import io.datarouter.joblet.DatarouterJobletConstants;
import io.datarouter.joblet.codec.JobletCodec;
import io.datarouter.joblet.model.Joblet;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.Require;

public class JobletType<P> implements Comparable<JobletType<?>>{

	private final String persistentString;
	private final String shortQueueName;//must be short for some queueing systems
	private final Supplier<JobletCodec<P>> codecSupplier;
	private final Class<? extends Joblet<P>> clazz;
	private final boolean causesScaling;
	public final Duration pollingPeriod;

	private JobletType(
			String persistentString,
			String shortQueueName,
			Supplier<JobletCodec<P>> codecSupplier,
			Class<? extends Joblet<P>> clazz,
			boolean causesScaling,
			Duration pollingPeriod){
		this.persistentString = persistentString;
		Require.isTrue(shortQueueName.length() <= DatarouterJobletConstants.MAX_LENGTH_SHORT_QUEUE_NAME,
				"shortQueueName length must be <= " + DatarouterJobletConstants.MAX_LENGTH_SHORT_QUEUE_NAME
				+ ": " + shortQueueName);
		this.shortQueueName = shortQueueName;
		this.codecSupplier = codecSupplier;
		this.clazz = clazz;
		this.causesScaling = causesScaling;
		this.pollingPeriod = pollingPeriod;
	}

	public String getDisplay(){
		return getPersistentString();
	}

	@Override
	public String toString(){
		return getPersistentString();
	}

	@Override
	public int compareTo(JobletType<?> other){
		return ComparableTool.nullFirstCompareTo(persistentString, other.persistentString);
	}

	public static void assertAllSameShortQueueName(Collection<? extends JobletType<?>> jobletTypes){
		long numShortQueueNames = jobletTypes.stream()
				.map(JobletType::getShortQueueName)
				.distinct()
				.count();
		Require.equals(numShortQueueNames, 1L);
	}

	public String getPersistentString(){
		return persistentString;
	}

	public String getShortQueueName(){
		return shortQueueName;
	}

	public Supplier<? extends JobletCodec<P>> getCodecSupplier(){
		return codecSupplier;
	}

	public Class<? extends Joblet<P>> getAssociatedClass(){
		return clazz;
	}

	public boolean causesScaling(){
		return causesScaling;
	}

	public static class JobletTypeBuilder<P>{

		private String persistentString;
		private String shortQueueName;
		private Supplier<JobletCodec<P>> codecSupplier;
		private Class<? extends Joblet<P>> clazz;
		private boolean causesScaling = true;
		private Duration pollingPeriod = Duration.ofSeconds(5);

		public JobletTypeBuilder(
				String persistentString,
				Supplier<JobletCodec<P>> codecSupplier,
				Class<? extends Joblet<P>> clazz){
			this.persistentString = persistentString;
			this.codecSupplier = codecSupplier;
			this.clazz = clazz;
		}

		/**
		 * Only needed if persistentString is longer than 38 chars
		 */
		public JobletTypeBuilder<P> withShortQueueName(String shortQueueName){
			this.shortQueueName = shortQueueName;
			return this;
		}

		public JobletTypeBuilder<P> disableScaling(){
			this.causesScaling = false;
			return this;
		}

		public JobletTypeBuilder<P> withPollingPeriod(Duration pollingPeriod){
			this.pollingPeriod = pollingPeriod;
			return this;
		}

		public JobletType<P> build(){
			return new JobletType<>(
					persistentString,
					shortQueueName != null ? shortQueueName : persistentString,
					codecSupplier,
					clazz,
					causesScaling,
					pollingPeriod);
		}

	}

}
