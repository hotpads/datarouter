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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.loadtest.service.LoadTestScanDao;
import io.datarouter.loadtest.storage.RandomValue;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;

public class LoadTestScanHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(LoadTestScanHandler.class);

	private static final String P_num = "num";
	private static final String P_batchSize = "batchSize";
	private static final String P_submitAction = "submitAction";

	private static final int DEFAULT_NUM = 100_000;
	private static final int DEFAULT_BATCH_SIZE = 1000;

	@Inject
	private LoadTestScanDao dao;
	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Handler(defaultHandler = true)
	private Mav scan(
			@Param(P_num) Optional<String> num,
			@Param(P_batchSize) Optional<String> batchSize,
			@Param(P_submitAction) Optional<String> submitAction){

		var form = new HtmlForm()
				.withMethod("post");
		form.addTextField()
				.withDisplay("Num")
				.withName(P_num)
				.withPlaceholder("100,000")
				.withValue(num.orElse(null));
		form.addTextField()
				.withDisplay("Batch Size")
				.withName(P_batchSize)
				.withPlaceholder("100")
				.withValue(batchSize.orElse(null));
		form.addButton()
				.withDisplay("Run Scan")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Load Test - Scan")
					.withContent(Html.makeContent(form))
					.buildMav();
		}
		PhaseTimer timer = new PhaseTimer("scan");

		//params
		int pNum = num
				.map(StringTool::nullIfEmpty)
				.map(number -> number.replaceAll(",", ""))
				.map(Integer::valueOf)
				.orElse(DEFAULT_NUM);
		int pBatchSize = batchSize
				.map(StringTool::nullIfEmpty)
				.map(number -> number.replaceAll(",", ""))
				.map(Integer::valueOf)
				.orElse(DEFAULT_BATCH_SIZE);

		//tracking
		AtomicInteger rowCounter = new AtomicInteger(0);
		AtomicLong lastBatchFinished = new AtomicLong(System.nanoTime());

		//execute
		dao.scan(pBatchSize, pNum)
				.forEach(randomValue -> trackEachRow(rowCounter, lastBatchFinished, randomValue));
		timer.add("scanned " + rowCounter.get());

		//results
		DomContent message = div(
				h2("Load Test Scan Results"),
				div(
					h3("Results"),
					dl(
							dt("Total Time"), dd(timer.getElapsedString()),
							dt("Rows per second"), dd(timer.getItemsPerSecond(rowCounter.get()) + ""))),
				div(
					h3("Params"),
					dl(
							dt("Num"), dd(pNum + ""),
							dt("Batch Size"), dd(pBatchSize + ""))))
				.withClass("container");
		logger.warn("total={}, rps={}, num={}, batchSize={}",
				timer.getElapsedString(),
				timer.getItemsPerSecond(rowCounter.get()),
				pNum,
				pBatchSize);
		return pageFactory.message(request, message);
	}

	private static class Html{

		public static DivTag makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Load Test - Scan"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

	private static void trackEachRow(AtomicInteger rowCounter, AtomicLong lastBatchFinished, RandomValue lastRow){
		int count = rowCounter.incrementAndGet();
		int logBatchSize = 1000;
		if(count % logBatchSize == 0){
			long durationNs = System.nanoTime() - lastBatchFinished.get();
			double rpNs = (double)logBatchSize / (double) durationNs;
			double rps = rpNs * 1000 * 1000 * 1000;
			logger.warn("scanned {} @{}rps from {}", NumberFormatter.addCommas(rowCounter),
					NumberFormatter.addCommas(rps), NumberFormatter.addCommas(lastRow.getK()));
			lastBatchFinished.set(System.nanoTime());
		}
	}

}
