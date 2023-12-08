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
package io.datarouter.auth.model.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.permissionrequest.PermissionRequest;
import io.datarouter.util.time.ZonedDateFormatterTool;

public record PermissionRequestDto(
		String requestTime,
		Long requestTimeMs,
		String requestText,
		String resolutionTime,
		Long resolutionTimeMs,
		String resolution,
		String editor){

	public PermissionRequestDto(
			PermissionRequest request,
			ZoneId zoneId,
			Optional<HistoryChange> historyChange){
		this(
				request.getKey().getRequestTime().format(zoneId),
				request.getKey().getRequestTime().toEpochMilli(),
				request.getRequestText(),
				request.getResolutionTime()
						.map(instant -> ZonedDateFormatterTool.formatInstantWithZone(instant, zoneId))
						.orElse(null),
				request.getResolutionTime()
						.map(Instant::toEpochMilli)
						.orElse(null),
				historyChange.map(HistoryChange::changes)
						.orElse(null),
				historyChange
						.flatMap(HistoryChange::editor)
						.map(DatarouterUser::getUsername)
						.orElse(null));
	}

}
