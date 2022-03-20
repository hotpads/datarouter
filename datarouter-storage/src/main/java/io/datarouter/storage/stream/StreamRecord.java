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
package io.datarouter.storage.stream;

import java.time.Instant;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;

public class StreamRecord<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseStreamRecord<PK,D>{

	private final D databean;

	public StreamRecord(String sequenceNumber, Instant approximateArrivalTimestamp, D databean){
		super(sequenceNumber, approximateArrivalTimestamp);
		this.databean = databean;
	}

	public D getDatabean(){
		return databean;
	}

}
