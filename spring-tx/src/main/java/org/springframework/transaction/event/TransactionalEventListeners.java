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

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;

/**
 * First-class collection to work with transactional event listeners, i.e.
 * {@link ApplicationListener} instances that implement
 * {@link TransactionalEventListenerMetadata}.
 *
 * @author Oliver Drotbohm
 * @since 5.3
 * @see TransactionalEventListener
 * @see TransactionalEventListenerMetadata
 */
public class TransactionalEventListeners {

	private final List<TransactionalEventListenerMetadata> listeners;

	/**
	 * Creates a new {@link TransactionalEventListeners} instance by filtering
	 * all elements implementing {@link TransactionalEventListenerMetadata}.
	 *
	 * @param listeners must not be {@literal null}.
	 */
	public TransactionalEventListeners(Collection<ApplicationListener<?>> listeners) {

		Assert.notNull(listeners, "ApplicationListeners must not be null!");

		this.listeners = listeners.stream()
				.filter(	TransactionalEventListenerMetadata.class::isInstance)
				.map(TransactionalEventListenerMetadata.class::cast)
				.sorted(AnnotationAwareOrderComparator.INSTANCE)
				.collect(Collectors.toList());
	}

	private TransactionalEventListeners(
			List<TransactionalEventListenerMetadata> listeners) {
		this.listeners = listeners;
	}

	/**
	 * Returns all {@link TransactionalEventListeners} for the given
	 * {@link TransactionPhase}.
	 *
	 * @param phase must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public TransactionalEventListeners forPhase(TransactionPhase phase) {

		Assert.notNull(phase, "TransactionPhase must not be null!");

		List<TransactionalEventListenerMetadata> collect = listeners.stream()
				.filter(it -> it.getTransactionPhase().equals(phase))
				.collect(Collectors.toList());

		return new TransactionalEventListeners(collect);
	}

	/**
	 * Invokes the given {@link Consumer} for all transactional event listeners.
	 *
	 * @param callback must not be {@literal null}.
	 */
	public void forEach(Consumer<TransactionalEventListenerMetadata> callback) {

		Assert.notNull(callback, "Callback must not be null!");

		listeners.forEach(callback);
	}

	/**
	 * Executes the given consumer only if there are actual listeners available.
	 * 
	 * @param metadata must not be {@literal null}.
	 */
	public void ifPresent(Consumer<Stream<TransactionalEventListenerMetadata>> metadata) {
		
		Assert.notNull(metadata, "Callback must not be null!");

		if (!listeners.isEmpty()) {
			metadata.accept(listeners.stream());
		}
	}

	/**
	 * Returns all transactional event listeners.
	 *
	 * @return will never be {@literal null}.
	 */
	public Stream<TransactionalEventListenerMetadata> stream() {
		return listeners.stream();
	}

	/**
	 * Invokes the given {@link Consumer} for the listener with the given
	 * identifier.
	 *
	 * @param identifier must not be {@literal null} or empty.
	 * @param callback must not be {@literal null}.
	 */
	public void doWithListener(String identifier,
			Consumer<TransactionalEventListenerMetadata> callback) {

		Assert.hasText(identifier, "Identifier must not be null or empty!");
		Assert.notNull(callback, "Callback must not be null!");

		listeners.stream()
				.filter(it -> it.getId().equals(identifier))
				.findFirst()
				.ifPresent(callback);
	}
}
