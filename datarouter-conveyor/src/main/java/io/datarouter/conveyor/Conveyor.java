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
package io.datarouter.conveyor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class Conveyor implements ConveyorRunnable{


	private final ConveyorService conveyorService;
	private final ConveyorConfiguration conveyorConfiguration;
	private final String name;
	private final AtomicBoolean isShuttingDown;
	private final Supplier<Boolean> shouldRun;

	public Conveyor(ConveyorService conveyorService,
			ConveyorConfiguration conveyorConfiguration,
			String name,
			Supplier<Boolean> shouldRun){
		this.conveyorService = conveyorService;
		this.conveyorConfiguration = conveyorConfiguration;
		this.name = name;
		this.shouldRun = shouldRun;
		this.isShuttingDown = new AtomicBoolean();
	}

	@Override
	public void run(){
		conveyorService.run(conveyorConfiguration, this);
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public boolean shouldRun(){
		return shouldRun.get();
	}

	@Override
	public void setIsShuttingDown(){
		isShuttingDown.set(true);
	}

	@Override
	public boolean isShuttingDown(){
		return isShuttingDown.get();
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return conveyorConfiguration.shouldRunOnShutdown();
	}

	public record ProcessResult(boolean shouldContinueImmediately){
	}

	@Override
	public String toString(){
		return "Conveyor-" + name;
	}

}
