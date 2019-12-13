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
package io.datarouter.conveyor.queue;

import java.util.function.Consumer;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.setting.Setting;

public class BasePutQueueConsumerConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseQueueConsumerConveyor<PK,D>{

	private final Consumer<D> putConsumer;

	public BasePutQueueConsumerConveyor(String name, Setting<Boolean> shouldRunSetting,
			QueueConsumer<PK,D> groupConsumer, Consumer<D> putConsumer){
		super(name, shouldRunSetting, groupConsumer);
		this.putConsumer = putConsumer;
	}

	@Override
	protected void processOne(D databean){
		putConsumer.accept(databean);
	}

}
