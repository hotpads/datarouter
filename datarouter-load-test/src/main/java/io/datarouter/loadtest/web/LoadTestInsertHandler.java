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
package io.datarouter.loadtest.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.loadtest.service.LoadTestInsertDao;
import io.datarouter.loadtest.storage.RandomValue;
import io.datarouter.loadtest.storage.RandomValueKey;
import io.datarouter.loadtest.util.LoadTestTool;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.write.StorageWriter;
import io.datarouter.util.concurrent.CallableTool;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

public class LoadTestInsertHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(LoadTestInsertHandler.class);

	private static final String P_num = "num";
	private static final String P_numThreads = "numThreads";
	private static final String P_batchSize = "batchSize";
	private static final String P_logPeriod = "logPeriod";
	private static final String P_persistentPut = "persistentPut";
	private static final String P_submitAction = "submitAction";

	private static final int DEFAULT_NUM = 1_000_000;
	private static final int DEFAULT_NUM_THREADS = 10;
	private static final int DEFAULT_BATCH_SIZE = 100;
	private static final boolean DEFAULT_PERSISTENT_PUT = true;
	private static final int DEFAULT_LOG_PERIOD = 10_000;

	@Inject
	private LoadTestInsertDao dao;
	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Handler(defaultHandler = true)
	private Mav insert(
			@Param(P_num) OptionalString num,
			@Param(P_numThreads) OptionalString numThreads,
			@Param(P_batchSize) OptionalString batchSize,
			@Param(P_logPeriod) OptionalString logPeriod,
			@Param(P_persistentPut) OptionalString persistentPut,
			@Param(P_submitAction) OptionalString submitAction){
		var form = new HtmlForm()
				.withMethod("post");
		form.addTextField()
				.withDisplay("Num")
				.withName(P_num)
				.withPlaceholder("100000")
				.withValue(num.orElse(null));
		form.addTextField()
				.withDisplay("Num Threads")
				.withName(P_numThreads)
				.withPlaceholder("10")
				.withValue(numThreads.orElse(null));
		form.addTextField()
				.withDisplay("Batch Size")
				.withName(P_batchSize)
				.withPlaceholder("100")
				.withValue(batchSize.orElse(null));
		form.addTextField()
				.withDisplay("Log Period")
				.withName(P_logPeriod)
				.withPlaceholder("10000")
				.withValue(logPeriod.orElse(null));
		form.addTextField()
				.withDisplay("Persistent Put")
				.withName(P_persistentPut)
				.withPlaceholder("true")
				.withValue(persistentPut.orElse(null));
		form.addButton()
				.withDisplay("Run Insert")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Load Test - Insert")
					.withContent(Html.makeContent(form))
					.buildMav();
		}

		//params
		int pNum = num
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_NUM);
		int pNumThreads = numThreads
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_NUM_THREADS);
		int pBatchSize = batchSize
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_BATCH_SIZE);
		int pLogPeriod = logPeriod
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_LOG_PERIOD);
		boolean pPersistentPut = persistentPut
				.map(StringTool::nullIfEmpty)
				.map(Boolean::valueOf)
				.orElse(DEFAULT_PERSISTENT_PUT);

		PhaseTimer timer = new PhaseTimer("insert");

		//tracking
		AtomicInteger counter = new AtomicInteger(0);
		AtomicLong lastBatchFinished = new AtomicLong(System.nanoTime());

		//execute
		int numBatches = LoadTestTool.numBatches(pNum, pBatchSize);
		ExecutorService executor = Executors.newFixedThreadPool(pNumThreads);
		Scanner.of(IntStream.range(0, numBatches).mapToObj(Integer::valueOf))
				.map(batchId -> LoadTestTool.makePredictableIdBatch(pNum, pBatchSize, batchId))
				.map(ids -> new InsertBatchCallable(dao.getNode(), ids, pPersistentPut, pLogPeriod, lastBatchFinished,
						counter))
				.parallel(new ParallelScannerContext(executor, pNumThreads, true))
				.forEach(CallableTool::callUnchecked);
		ExecutorServiceTool.shutdown(executor, Duration.ofSeconds(5));
		timer.add("inserted " + counter.get());

		//results
		String message = timer.toString() + " @" + timer.getItemsPerSecond(counter.get()) + "/s";
		logger.warn(message);
		return pageFactory.message(request, message);
	}

	private static class Html{

		public static ContainerTag makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Load Test - Insert"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

	private static class InsertBatchCallable implements Callable<Void>{

		private final StorageWriter<RandomValueKey,RandomValue> node;
		private final List<Integer> ids;
		private final boolean persistentPut;
		private final int logPeriod;
		private final AtomicLong lastBatchFinished;
		private final AtomicInteger counter;

		public InsertBatchCallable(
				StorageWriter<RandomValueKey,RandomValue> node,
				List<Integer> ids,
				boolean persistentPut,
				int logPeriod,
				AtomicLong lastBatchFinished,
				AtomicInteger counter){
			this.node = node;
			this.ids = ids;
			this.persistentPut = persistentPut;
			this.logPeriod = logPeriod;
			this.lastBatchFinished = lastBatchFinished;
			this.counter = counter;
		}

		@Override
		public Void call(){
			Scanner.of(ids)
					.map(RandomValue::new)
					.flush(databeans -> node.putMulti(databeans, new Config()
							.setPersistentPut(persistentPut)
							.setNumAttempts(10)))
					.forEach($ -> trackEachRow());
			return null;
		}

		private void trackEachRow(){
			int tot = counter.incrementAndGet();
			if(tot > 0 && tot % logPeriod == 0){
				long nanoTime = System.nanoTime();
				long durationMs = (nanoTime - lastBatchFinished.getAndSet(nanoTime)) / 1_000_000;
				double rps = logPeriod / (double) durationMs * 1_000;
				logger.warn("inserted {} @{}rps", NumberFormatter.addCommas(tot), NumberFormatter.addCommas(rps));
			}
		}
	}

}
