/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Metadata about a transactional event listener.
 * 
 * @author Oliver Drotbohm
 * @since 5.3
 * @see TransactionalEventListener
 * @see TransactionalEventListeners
 */
public interface TransactionalEventListenerMetadata
		extends ApplicationListener<ApplicationEvent> {

	/**
	 * Returns the identifier of the event listener to be able to refer to them
	 * individually.
	 * 
	 * @return will never be {@literal null}.
	 */
	String getId();

	/**
	 * Returns the {@link TransactionPhase} in which the listener will be
	 * invoked.
	 * 
	 * @return will never be {@literal null}.
	 */
	TransactionPhase getTransactionPhase();

	/**
	 * A callback to be invoked on successful event handling.
	 * 
	 * @param completion must not be {@literal null}.
	 */
	void registerCompletionCallback(CompletionCallback completion);

	/**
	 * Registers the given {@link ErrorHandler} as error callback on the event
	 * listener. Those callbacks are invoked if {@link RuntimeException}e are
	 * thrown during event processing. If the callback returns the exception in
	 * turn it will still be thrown. If {@literal null} is returned, we assume
	 * the callback has handled the exception.
	 * 
	 * @param errorHandler must not be {@literal null}.
	 */
	void registerErrorCallback(ErrorHandler errorHandler);

	/**
	 * Immediately process the given {@link ApplicationListener}. In contrast to
	 * {@link #onApplicationEvent(ApplicationEvent)}, a call to this method will
	 * directly process the given event without deferring it to the configured
	 * {@link TransactionPhase}.
	 * 
	 * @param event must not be {@literal null}.
	 */
	void processEvent(ApplicationEvent event);

	/**
	 * Callback to be invoked on successful event handling.
	 */
	interface CompletionCallback {

		/**
		 * Invoked after a successful transactional event handling. 
		 * 
		 * @param event the event that has been handled successfully.
		 * @param listenerIdentifier the identifier of the transactional
		 * {@link ApplicationListener}.
		 */
		void onCompletion(ApplicationEvent event, String listenerIdentifier);
	}

	/**
	 * Callback to be invoked if the transactional even handling or any of the
	 * registered {@link CompletionCallback}s fails.
	 */
	interface ErrorHandler {

		/**
		 * Invoked if an error occurred during a transactional event listener
		 * invocation or a subsequent {@link CompletionCallback} invocation.
		 * 
		 * @param event the event that has been failed to handle.
		 * @param listenerIdentifier the identifier of the transactional
		 * {@link ApplicationListener}
		 * @param exception the exception that occurred
		 * @return the exception if you wish it to propagate or {@literal null}
		 * to indicate the exception has been handled.
		 */
		RuntimeException onError(ApplicationEvent event, String listenerIdentifier,
				RuntimeException exception);
	}
}
