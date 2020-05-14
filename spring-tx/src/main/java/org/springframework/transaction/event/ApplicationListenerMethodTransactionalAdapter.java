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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link GenericApplicationListener} adapter that delegates the processing of
 * an event to a {@link TransactionalEventListener} annotated method. Supports
 * the exact same features as any regular {@link EventListener} annotated method
 * but is aware of the transactional context of the event publisher.
 *
 * <p>Processing of {@link TransactionalEventListener} is enabled automatically
 * when Spring's transaction management is enabled. For other cases, registering
 * a bean of type {@link TransactionalEventListenerFactory} is required.
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @author Oliver Drotbohm
 * @since 4.2
 * @see ApplicationListenerMethodAdapter
 * @see TransactionalEventListener
 */
class ApplicationListenerMethodTransactionalAdapter extends ApplicationListenerMethodAdapter 
	implements TransactionalEventListenerMetadata {

	private final TransactionalEventListener annotation;
	
	private final List<CompletionCallback> completionCallbacks;
	
	private final List<ErrorHandler> errorCallbacks;


	public ApplicationListenerMethodTransactionalAdapter(String beanName, Class<?> targetClass, Method method) {
		super(beanName, targetClass, method);
		TransactionalEventListener ann = AnnotatedElementUtils.findMergedAnnotation(method, TransactionalEventListener.class);
		if (ann == null) {
			throw new IllegalStateException("No TransactionalEventListener annotation found on method: " + method);
		}
		this.annotation = ann;
		this.completionCallbacks = new ArrayList<>();
		this.errorCallbacks = new ArrayList<>();
	}


	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (TransactionSynchronizationManager.isSynchronizationActive() &&
				TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronization transactionSynchronization = createTransactionSynchronization(event);
			TransactionSynchronizationManager.registerSynchronization(transactionSynchronization);
		}
		else if (this.annotation.fallbackExecution()) {
			if (this.annotation.phase() == TransactionPhase.AFTER_ROLLBACK && logger.isWarnEnabled()) {
				logger.warn("Processing " + event + " as a fallback execution on AFTER_ROLLBACK phase");
			}
			processEvent(event);
		}
		else {
			// No transactional event execution at all
			if (logger.isDebugEnabled()) {
				logger.debug("No transaction is active - skipping " + event);
			}
		}
	}
	
	@Override
	public String getId() {
		return StringUtils.hasText(annotation.id()) ? annotation.id() : getDefaultId();
	}
	
	@Override
	public TransactionPhase getTransactionPhase() {
		return annotation.phase();
	}
	
	@Override
	public void registerCompletionCallback(CompletionCallback callback) {
		
		Assert.notNull(callback, "Completion callback must not be null!");
		this.completionCallbacks.add(callback);
	}
	
	@Override
	public void registerErrorCallback(ErrorHandler errorHandler) {
		
		Assert.notNull(errorHandler, "Error handler must not be null!");
		this.errorCallbacks.add(errorHandler);
	}

	private TransactionSynchronization createTransactionSynchronization(ApplicationEvent event) {
		return new TransactionSynchronizationEventAdapter(this, event, this.annotation.phase(),
				this.getId(), this.completionCallbacks, this.errorCallbacks);
	}

	private String getDefaultId() {
		return getDefaultIdFor(getMethod());
	}

	static String getDefaultIdFor(Method method) {

		Class<?> type = ClassUtils.getUserClass(method.getDeclaringClass());
		String methodName = ClassUtils.getQualifiedMethodName(method, type);
		String parameterTypes = StringUtils.arrayToDelimitedString(method.getParameterTypes(), ", ");
		
		return String.format("%s(%s)", methodName, parameterTypes);
	}

	private static class TransactionSynchronizationEventAdapter implements TransactionSynchronization {

		private final ApplicationListenerMethodAdapter listener;

		private final ApplicationEvent event;

		private final TransactionPhase phase;
		
		private final String identifier;
		
		private final List<CompletionCallback> completionCallbacks;
		
		private final List<ErrorHandler> errorHandlers;

		public TransactionSynchronizationEventAdapter(ApplicationListenerMethodAdapter listener,
				ApplicationEvent event, TransactionPhase phase, String identifier, List<CompletionCallback> completionCallbacks, List<ErrorHandler> errorHandlers) {

			this.listener = listener;
			this.event = event;
			this.phase = phase;
			this.identifier = identifier;
			this.completionCallbacks = completionCallbacks;
			this.errorHandlers = errorHandlers;
		}

		@Override
		public int getOrder() {
			return this.listener.getOrder();
		}

		@Override
		public void beforeCommit(boolean readOnly) {
			if (this.phase == TransactionPhase.BEFORE_COMMIT) {
				processEvent();
			}
		}

		@Override
		public void afterCompletion(int status) {
			if (this.phase == TransactionPhase.AFTER_COMMIT && status == STATUS_COMMITTED) {
				processEvent();
			}
			else if (this.phase == TransactionPhase.AFTER_ROLLBACK && status == STATUS_ROLLED_BACK) {
				processEvent();
			}
			else if (this.phase == TransactionPhase.AFTER_COMPLETION) {
				processEvent();
			}
		}

		protected void processEvent() {
			
			try {
			
				this.listener.processEvent(this.event);

				for (CompletionCallback callback : completionCallbacks) {
					callback.onCompletion(event, identifier);
				}
			
			} catch (RuntimeException t) {
				
				boolean exceptionHandled = false;
				
				for (ErrorHandler handler : errorHandlers) {
					
					RuntimeException result = handler.onError(event, identifier, t);
					
					if (result != null) {
						throw result;
					} else {
						exceptionHandled = true;
					}
				}
				
				if (!exceptionHandled) {
					throw t;
				}
			}
		}
	}
}
