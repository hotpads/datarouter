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
package io.datarouter.gcp.gcs.util;

import java.util.Map;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.google.cloud.storage.Storage.PredefinedAcl;

public class GcsToAwsConstants{
	public static final Map<PredefinedAcl,CannedAccessControlList> GCS_TO_S3_ACL_MAP = Map.of(
			PredefinedAcl.AUTHENTICATED_READ, CannedAccessControlList.AuthenticatedRead,
			PredefinedAcl.ALL_AUTHENTICATED_USERS, CannedAccessControlList.AuthenticatedRead,
			PredefinedAcl.PRIVATE, CannedAccessControlList.Private,
			PredefinedAcl.PROJECT_PRIVATE, CannedAccessControlList.Private,
			PredefinedAcl.PUBLIC_READ, CannedAccessControlList.PublicRead,
			PredefinedAcl.PUBLIC_READ_WRITE, CannedAccessControlList.PublicReadWrite,
			PredefinedAcl.BUCKET_OWNER_FULL_CONTROL, CannedAccessControlList.BucketOwnerFullControl,
			PredefinedAcl.BUCKET_OWNER_READ, CannedAccessControlList.BucketOwnerRead);
}
