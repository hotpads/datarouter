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
package io.datarouter.loadtest.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.dd;
import static j2html.TagCreator.div;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.loadtest.service.LoadTestGetDao;
import io.datarouter.loadtest.storage.RandomValue;
import io.datarouter.loadtest.storage.RandomValueKey;
import io.datarouter.loadtest.util.LoadTestTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.util.concurrent.CallableTool;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class LoadTestGetHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(LoadTestGetHandler.class);

	private static final String P_num = "num";
	private static final String P_max = "max";
	private static final String P_numThreads = "numThreads";
	private static final String P_batchSize = "batchSize";
	private static final String P_logPeriod = "logPeriod";
	private static final String P_submitAction = "submitAction";

	private static final int DEFAULT_NUM = 100_000;
	private static final int DEFAULT_NUM_THREADS = 10;
	private static final int DEFAULT_BATCH_SIZE = 100;
	private static final int DEFAULT_LOG_PERIOD = 10_000;

	@Inject
	private LoadTestGetDao dao;
	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Handler(defaultHandler = true)
	private Mav get(
			@Param(P_num) Optional<String> num,
			@Param(P_max) Optional<String> max,
			@Param(P_numThreads) Optional<String> numThreads,
			@Param(P_batchSize) Optional<String> batchSize,
			@Param(P_logPeriod) Optional<String> logPeriod,
			@Param(P_submitAction) Optional<String> submitAction){
		var form = new HtmlForm()
				.withMethod("post");
		form.addTextField()
				.withDisplay("Num")
				.withName(P_num)
				.withPlaceholder("100,000")
				.withValue(num.orElse(null));
		form.addTextField()
				.withDisplay("Max")
				.withName(P_max)
				.withPlaceholder("10")
				.withValue(max.orElse(null));
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
				.withPlaceholder("1,0000")
				.withValue(logPeriod.orElse(null));
		form.addButton()
				.withDisplay("Run Get")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Load Test - Get")
					.withContent(Html.makeContent(form))
					.buildMav();
		}

		PhaseTimer timer = new PhaseTimer("get");
		//params
		int pNum = num
				.map(StringTool::nullIfEmpty)
				.map(number -> number.replaceAll(",", ""))
				.map(Integer::valueOf)
				.orElse(DEFAULT_NUM);
		int pMax = max
				.map(StringTool::nullIfEmpty)
				.map(number -> number.replaceAll(",", ""))
				.map(Integer::valueOf)
				.orElse(pNum);
		int pNumThreads = numThreads
				.map(StringTool::nullIfEmpty)
				.map(number -> number.replaceAll(",", ""))
				.map(Integer::valueOf)
				.orElse(DEFAULT_NUM_THREADS);
		int pBatchSize = batchSize
				.map(StringTool::nullIfEmpty)
				.map(number -> number.replaceAll(",", ""))
				.map(Integer::valueOf)
				.orElse(DEFAULT_BATCH_SIZE);
		int pLogPeriod = logPeriod
				.map(StringTool::nullIfEmpty)
				.map(number -> number.replaceAll(",", ""))
				.map(Integer::valueOf)
				.orElse(DEFAULT_LOG_PERIOD);

		//tracking
		AtomicInteger rowCounter = new AtomicInteger(0);
		AtomicLong lastBatchFinished = new AtomicLong(System.nanoTime());

		//execute
		int numBatches = LoadTestTool.numBatches(pNum, pBatchSize);
		ExecutorService executor = Executors.newFixedThreadPool(pNumThreads);
		Scanner.of(IntStream.range(0, numBatches).mapToObj(Integer::valueOf))
				.map(batchId -> LoadTestTool.makeRandomIdBatch(pNum, pMax, pBatchSize, batchId))
				.map(ids -> new GetBatchCallable(dao.getReaderNode(), ids, pLogPeriod, rowCounter, lastBatchFinished))
				.parallelUnordered(new Threads(executor, pNumThreads))
				.forEach(CallableTool::callUnchecked);
		ExecutorServiceTool.shutdown(executor, Duration.ofSeconds(5));
		timer.add("got " + rowCounter.get());

		var message = div(
				h2("Load Test Get Results"),
				div(
					h3("Results"),
					dl(
							dt("Total Time"), dd(timer.getElapsedString()),
							dt("Rows per second"), dd(timer.getItemsPerSecond(rowCounter.get()) + ""))),
				div(
					h3("Params"),
					dl(
							dt("Num"), dd(pNum + ""),
							dt("Max"), dd(pMax + ""),
							dt("Num Threads"), dd(pNumThreads + ""),
							dt("Batch Size"), dd(pBatchSize + ""),
							dt("Log Period"), dd(pLogPeriod + ""))))
				.withClass("container");
		logger.warn("total={}, rps={}, num={}, max={}, numThreads={} batchSize={}, logPeriod={}",
				timer.getElapsedString(),
				timer.getItemsPerSecond(rowCounter.get()),
				pNum,
				pMax,
				pNumThreads,
				pBatchSize,
				pLogPeriod);
		return pageFactory.message(request, message);
	}

	private static class Html{

		public static DivTag makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Load Test - Get"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

	private static class GetBatchCallable implements Callable<Void>{

		private final MapStorageReader<RandomValueKey,RandomValue> node;
		private final List<Integer> ids;
		private final int logPeriod;
		private final AtomicInteger rowCounter;
		private final AtomicLong lastBatchFinished;

		public GetBatchCallable(
				MapStorageReader<RandomValueKey,RandomValue> node,
				List<Integer> ids,
				int logPeriod,
				AtomicInteger rowCounter,
				AtomicLong lastBatchFinished){
			this.node = node;
			this.ids = ids;
			this.logPeriod = logPeriod;
			this.rowCounter = rowCounter;
			this.lastBatchFinished = lastBatchFinished;
		}

		@Override
		public Void call(){
			List<RandomValue> databeans = Scanner.of(ids)
					.map(RandomValueKey::new)
					.listTo(node::getMulti);
			//TODO track hits vs misses
			databeans.forEach($ -> trackEachRow());
			return null;
		}

		private void trackEachRow(){
			int numCompleted = rowCounter.incrementAndGet();
			if(numCompleted % logPeriod == 0){
				long duration = (System.nanoTime() - lastBatchFinished.get()) / 1_000_000;
				double rps = ((double) logPeriod) / duration * 1_000;
				logger.warn("got {} @{}rps", NumberFormatter.addCommas(numCompleted),
						NumberFormatter.addCommas(rps));
				lastBatchFinished.set(System.nanoTime());
			}
		}

	}

}
