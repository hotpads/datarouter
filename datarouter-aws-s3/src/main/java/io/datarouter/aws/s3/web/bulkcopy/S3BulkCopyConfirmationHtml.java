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
package io.datarouter.aws.s3.web.bulkcopy;

import static j2html.TagCreator.b;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.pre;
import static j2html.TagCreator.strong;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.client.S3ClientManager;
import io.datarouter.aws.s3.web.S3Html;
import io.datarouter.aws.s3.web.bulkcopy.S3BulkCopyHandler.S3BulkCopyHandlerConfirmationParams;
import io.datarouter.aws.s3.web.bulkcopy.S3BulkCopyHandler.S3BulkCopyHandlerLinks;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.file.BucketAndKey;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public record S3BulkCopyConfirmationHtml(
		DatarouterClients clients,
		S3ClientManager s3ClientManager,
		S3BulkCopyHandlerLinks s3BulkCopyHandlerLinks,
		S3BulkCopyHandlerConfirmationParams params){

	public DivTag makeAll(){
		return div(
				makeSummaryDiv(),
				br(),
				makeExampleDiv(),
				br(),
				makeConfirmationButtonDiv());
	}

	private DivTag makeSummaryDiv(){
		var tableTag = table(
				tr(
						td(b("Datarouter Client Name"))
						.withStyle("width:200px"),
						td(params.client)),
				tr(
						td(b("From Bucket")),
						td(params.fromBucket)),
				tr(
						td(b("From Key Prefix")),
						td(params.fromPrefix)),
				tr(
						td(b("To Bucket")),
						td(params.toBucket)),
				tr(
						td(b("To Key Prefix")),
						td(params.toPrefix)))
				.withClasses("table table-sm table-striped border")
				.withStyle("width:600px;");
		return div(
				h5("Summary"),
				tableTag);
	}

	private DivTag makeExampleDiv(){
		DatarouterS3Client s3Client = getS3Client(params.client);
		BucketAndKey exampleFrom = s3Client.scan(params.toFromToBucketAndPrefix())
				.map(s3Object -> new BucketAndKey(params.fromBucket, s3Object.key()))
				.findFirst()
				.orElseThrow();
		BucketAndKey exampleTo = S3BulkCopyHandler.toNewLocation(
				exampleFrom,
				params.toFromToBucketAndPrefix(),
				params.toToBucketAndPrefix());
		var fromToDiv = div()
				.with(div(strong("From:")))
				.with(div(pre(String.format("%s/%s", exampleFrom.bucket(), exampleFrom.key()))))
				.with(div(strong("To:")))
				.with(div(pre(String.format("%s/%s", exampleTo.bucket(), exampleTo.key()))))
				.withStyle("font-size:1.1em;");
		return div(
				h5("Example"),
				fromToDiv);
	}

	private DivTag makeConfirmationButtonDiv(){
		ATag button = S3Html.makeDangerButton(
				"Confirm and Copy",
				s3BulkCopyHandlerLinks.performCopy(params.toPerformCopyParams()));
		return div(
				h5("Confirm"),
				button);
	}

	// TODO move somewhere more generic?
	private DatarouterS3Client getS3Client(String clientName){
		ClientId clientId = clients.getClientId(clientName);
		return s3ClientManager.getClient(clientId);
	}

	@Singleton
	public static class S3BulkCopyConfirmationHtmlFactory{
		@Inject
		private DatarouterClients clients;
		@Inject
		private S3ClientManager s3ClientManager;
		@Inject
		private S3BulkCopyHandlerLinks s3BulkCopyHandlerLinks;

		public S3BulkCopyConfirmationHtml create(S3BulkCopyHandlerConfirmationParams params){
			return new S3BulkCopyConfirmationHtml(clients, s3ClientManager, s3BulkCopyHandlerLinks, params);
		}
	}

}
