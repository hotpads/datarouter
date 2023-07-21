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
package io.datarouter.aws.s3.config;

import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterAwsS3Plugin extends BaseWebPlugin{

	public static final String NAME = "Datarouter S3";
	private static final DatarouterAwsS3Paths PATHS = new DatarouterAwsS3Paths();

	public DatarouterAwsS3Plugin(){
		addRouteSet(DatarouterAwsS3RouteSet.class);
		addDatarouterGithubDocLink("datarouter-aws-s3");
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.DATA,
				PATHS.datarouter.clients.awsS3.bulkCopy.form,
				"S3 - Bulk Copy");
	}

	@Override
	protected void configure(){
	}

}
