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

import java.util.function.Consumer;

import org.springframework.lang.Nullable;

/**
 * 
 * @author odrotbohm
 * @since 5.2
 */
public class TransactionalEventListenerBuilder<T> {
	
	private Class<T> eventType;
	private String id;
	private @Nullable Consumer<T> listener;
	private TransactionPhase phase;
	
	private TransactionalEventListenerBuilder(Class<T> type, TransactionPhase phase) {
		
		this.eventType = type;
		this.listener = null;
		this.phase = phase;
	}

	public static <T> TransactionalEventListenerBuilder<T> forType(Class<T> type) {
		return new TransactionalEventListenerBuilder<>(type, TransactionPhase.AFTER_COMMIT);
	}
	
	public TransactionalEventListenerBuilder<T> phase(TransactionPhase phase) {
		
		this.phase = phase;
		return this;
	}
	
	public TransactionalEventListenerBuilder<T> listener(Consumer<T> listener, String id) {
		
		this.listener = listener;
		this.id = id;
		
		return this;
	}
	
	
	
	public TransactionalEventListenerMetadata build() {
		
		if (listener == null) {
			throw new IllegalStateException("No listener configured!");
		}
		
		return null;
	}
}
