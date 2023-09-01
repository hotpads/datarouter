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
package io.datarouter.auth.service.deprovisioning;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Executes the {@link UserDeprovisioningStrategy} and {@link UserDeprovisioningListeners} configured in
 * DatarouterAuthPlugin
 */
@Singleton
public class UserDeprovisioningService{
	private static final Logger logger = LoggerFactory.getLogger(UserDeprovisioningService.class);

	@Inject
	private UserDeprovisioningStrategy userDeprovisioningServiceStrategy;
	@Inject
	private UserDeprovisioningListeners listeners;

	/**
	 * executes {@link UserDeprovisioningStrategy#flagUsers(List, Optional)}, preceded and proceeded respectively by
	 * each {@link UserDeprovisioningListener#onFlagUsers(List)} and
	 * {@link UserDeprovisioningListener#onFlaggedUsers(List)} from {@link UserDeprovisioningListeners}
	 * @param usernames to flag
	 * @param editorUsername (if manually triggered)
	 * @return returns the successfully flagged usernames
	 */
	public final List<String> flagUsers(List<String> usernames, Optional<String> editorUsername){
		return executeStrategyAndNotifyListeners(userDeprovisioningServiceStrategy::flagUsers, usernames,
				editorUsername, $ -> $::onFlagUsers, $ -> $::onFlaggedUsers);
	}

	/**
	 * executes {@link UserDeprovisioningStrategy#deprovisionUsers(List, Optional)}, preceded and proceeded respectively
	 * by each {@link UserDeprovisioningListener#onDeprovisionUsers(List)} and
	 * {@link UserDeprovisioningListener#onDeprovisionedUsers(List)} from {@link UserDeprovisioningListeners}
	 * @param usernames to deprovision
	 * @param editorUsername (if manually triggered)
	 * @return returns the successfully deprovisioned usernames
	 */
	public final List<String> deprovisionUsers(List<String> usernames, Optional<String> editorUsername){
		return executeStrategyAndNotifyListeners(userDeprovisioningServiceStrategy::deprovisionUsers, usernames,
				editorUsername, $ -> $::onDeprovisionUsers, $ -> $::onDeprovisionedUsers);
	}

	/**
	 * executes {@link UserDeprovisioningStrategy#restoreUsers(List, Optional)}, preceded and proceeded respectively by
	 * each {@link UserDeprovisioningListener#onRestoreUsers(List)} and
	 * {@link UserDeprovisioningListener#onRestoredUsers(List)} from {@link UserDeprovisioningListeners}
	 * @param usernames to restore, which were previously deprovisioned using
	 * {@link UserDeprovisioningStrategy#deprovisionUsers(List, Optional)}
	 * @param editorUsername (if manually triggered)
	 * @return returns the successfully restored usernames
	 */
	public final List<String> restoreUsers(List<String> usernames, Optional<String> editorUsername){
		return executeStrategyAndNotifyListeners(userDeprovisioningServiceStrategy::restoreUsers, usernames,
				editorUsername, $ -> $::onRestoreUsers, $ -> $::onRestoredUsers);
	}

	private List<String> executeStrategyAndNotifyListeners(
			BiFunction<List<String>,Optional<String>,List<String>> strategy,
			List<String> usernames,
			Optional<String> editorUsername,
			Function<UserDeprovisioningListener,Consumer<List<String>>> preListenerSelector,
			Function<UserDeprovisioningListener,Consumer<List<String>>> postListenerSelector){
		notifyListeners(preListenerSelector, usernames);
		List<String> result = strategy.apply(usernames, editorUsername);
		notifyListeners(postListenerSelector, usernames);
		return result;
	}

	private void notifyListeners(Function<UserDeprovisioningListener,Consumer<List<String>>> methodSelector,
			List<String> usernames){
		AtomicInteger numErrors = new AtomicInteger();
		List<UserDeprovisioningListener> listenerList = listeners.get();
		listenerList.forEach(listener -> {
			try{
				methodSelector.apply(listener).accept(usernames);
			}catch(RuntimeException e){
				logger.error("Failed to notify UserDeprovisioningListener", e);
				numErrors.incrementAndGet();
			}
		});
		logger.info("Notified size={} UserDeprovisioningListeners with numErrors={}", listenerList.size(), numErrors
				.get());
	}

}
