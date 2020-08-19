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
package io.datarouter.util.timer;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleTimer{
	private static final Logger logger = LoggerFactory.getLogger(ScheduleTimer.class);

	public static void schedule(Duration duration, Runnable runnable){
		new Timer().schedule(new RunnableToTimerTask(runnable), duration.toMillis());
	}

	private static class RunnableToTimerTask extends TimerTask{
		private final Runnable runnable;

		private RunnableToTimerTask(Runnable runnable){
			this.runnable = runnable;
		}

		@Override
		public void run(){
			try{
				runnable.run();
			}catch(Exception e){
				//control the logging of this exception instead of letting the uncaught exception handler do it
				logger.error("", e);
			}
		}
	}


}
