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
package io.datarouter.conveyor.queue;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.web.exception.ExceptionRecorder;

public class PutMultiGroupQueueConsumerConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseGroupQueueConsumerConveyor<PK,D>{

	private final Consumer<Collection<D>> putMultiConsumer;

	public PutMultiGroupQueueConsumerConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<PK,D> groupQueueConsumer,
			Consumer<Collection<D>> putMultiConsumer,
			Duration peekTimeout,
			ExceptionRecorder exceptionRecorder,
			ConveyorGauges conveyorGauges){
		super(name, shouldRun, groupQueueConsumer, () -> false, peekTimeout, exceptionRecorder, conveyorGauges);
		this.putMultiConsumer = putMultiConsumer;
	}

	@Override
	protected void processDatabeans(List<D> databeans){
		putMultiConsumer.accept(databeans);
	}

}
