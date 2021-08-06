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
package io.datarouter.batchsizeoptimizer.web;

import java.util.Collection;

import javax.inject.Inject;

import io.datarouter.batchsizeoptimizer.config.DatarouterBatchSizeOptimizerFiles;
import io.datarouter.batchsizeoptimizer.storage.optimizedbatch.DatarouterOpOptimizedBatchSizeDao;
import io.datarouter.batchsizeoptimizer.storage.optimizedbatch.OpOptimizedBatchSize;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;

public class BatchSizeOptimizerMonitoringHandler extends BaseHandler{

	@Inject
	private DatarouterOpOptimizedBatchSizeDao dao;
	@Inject
	private DatarouterBatchSizeOptimizerFiles files;

	@Handler(defaultHandler = true)
	protected Mav view(){
		return new Mav(files.jsp.datarouter.batchSizeOptimizer.batchSizeOptimizerJsp);
	}

	@Handler
	public Collection<TextOptimalBatchSize> getOptimalBatchSizes(){
		return dao.scan()
				.map(TextOptimalBatchSize::new)
				.list();
	}

	public class TextOptimalBatchSize{

		public final String opName;
		public final Integer batchSize;
		public final Double curiosity;

		public TextOptimalBatchSize(OpOptimizedBatchSize opOptimizedBatchSize){
			this.opName = opOptimizedBatchSize.getKey().getOpName();
			this.batchSize = opOptimizedBatchSize.getBatchSize();
			this.curiosity = opOptimizedBatchSize.getCuriosity();
		}

	}

}
