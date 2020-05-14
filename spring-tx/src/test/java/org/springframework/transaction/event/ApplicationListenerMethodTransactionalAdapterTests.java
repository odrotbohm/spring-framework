/*
 * Copyright 2002-2019 the original author or authors.
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

import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.event.TransactionalEventListenerMetadata.CompletionCallback;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Stephane Nicoll
 */
public class ApplicationListenerMethodTransactionalAdapterTests {

	@Test
	public void defaultPhase() {
		Method m = ReflectionUtils.findMethod(SampleEvents.class, "defaultPhase", String.class);
		assertPhase(m, TransactionPhase.AFTER_COMMIT);
	}

	@Test
	public void phaseSet() {
		Method m = ReflectionUtils.findMethod(SampleEvents.class, "phaseSet", String.class);
		assertPhase(m, TransactionPhase.AFTER_ROLLBACK);
	}

	@Test
	public void phaseAndClassesSet() {
		Method m = ReflectionUtils.findMethod(SampleEvents.class, "phaseAndClassesSet");
		assertPhase(m, TransactionPhase.AFTER_COMPLETION);
		supportsEventType(true, m, createGenericEventType(String.class));
		supportsEventType(true, m, createGenericEventType(Integer.class));
		supportsEventType(false, m, createGenericEventType(Double.class));
	}

	@Test
	public void valueSet() {
		Method m = ReflectionUtils.findMethod(SampleEvents.class, "valueSet");
		assertPhase(m, TransactionPhase.AFTER_COMMIT);
		supportsEventType(true, m, createGenericEventType(String.class));
		supportsEventType(false, m, createGenericEventType(Double.class));
	}
	
	@Test
	public void invokesCompletionCallbackOnSuccess() {

		Method m = ReflectionUtils.findMethod(SampleEvents.class, "defaultPhase", String.class);
		CapturingCompletionCallback callback = new CapturingCompletionCallback();
		PayloadApplicationEvent<Object> event = new PayloadApplicationEvent<Object>(this, new Object());

		ApplicationListenerMethodTransactionalAdapter adapter = createTestInstance(m);
		adapter.registerCompletionCallback(callback);

		runInTransaction(() -> adapter.onApplicationEvent(event), true);

		assertThat(callback.invoked).isTrue();
		assertThat(callback.event).isEqualTo(event);
		assertThat(callback.identifier).endsWith("SampleEvents.defaultPhase(class java.lang.String)");
	}
	
	@Test
	public void invokesErrorHandlerOnException() {

		Method m = ReflectionUtils.findMethod(SampleEvents.class, "throwing", String.class);
		PayloadApplicationEvent<String> event = new PayloadApplicationEvent<>(this, "event");
		CapturingErrorHandler errorHandler = new CapturingErrorHandler(false);

		ApplicationListenerMethodTransactionalAdapter adapter = createTestInstance(m);
		adapter.registerErrorCallback(errorHandler);

		runInTransaction(() -> adapter.onApplicationEvent(event), true);

		assertErrorHandled(errorHandler, m, event);
	}

	@Test
	public void rethrowsExceptionIfErrorHandlerReturnsIt() {

		Method m = ReflectionUtils.findMethod(SampleEvents.class, "throwing", String.class);
		PayloadApplicationEvent<String> event = new PayloadApplicationEvent<>(this, "event");
		CapturingErrorHandler errorHandler = new CapturingErrorHandler(true);

		ApplicationListenerMethodTransactionalAdapter adapter = createTestInstance(m);
		adapter.registerErrorCallback(errorHandler);

		assertThatExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> runInTransaction(() -> adapter.onApplicationEvent(event), true))
			.withMessage("event");

		assertErrorHandled(errorHandler, m, event);
	}
	
	@Test
	public void throwsExceptionIfNoErrorHandlerRegistered() {
		
		Method m = ReflectionUtils.findMethod(SampleEvents.class, "throwing", String.class);
		CapturingCompletionCallback callback = new CapturingCompletionCallback();
		PayloadApplicationEvent<String> event = new PayloadApplicationEvent<>(this, "event");

		ApplicationListenerMethodTransactionalAdapter adapter = createTestInstance(m);
		adapter.registerCompletionCallback(callback);

		assertThatExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> runInTransaction(() -> adapter.onApplicationEvent(event), true))
			.withMessage("event");

		assertThat(callback.invoked).isFalse();
		assertThat(callback.event).isNull();
		assertThat(callback.identifier).isNull();
	}
	
	@Test
	public void usesAnnotatedIdentifier() {

		Method m = ReflectionUtils.findMethod(SampleEvents.class, "identified", String.class);
		CapturingCompletionCallback callback = new CapturingCompletionCallback();
		PayloadApplicationEvent<String> event = new PayloadApplicationEvent<>(this, "event");

		ApplicationListenerMethodTransactionalAdapter adapter = createTestInstance(m);
		adapter.registerCompletionCallback(callback);

		runInTransaction(() -> adapter.onApplicationEvent(event), true);

		assertThat(callback.invoked).isTrue();
		assertThat(callback.event).isEqualTo(event);
		assertThat(callback.identifier).endsWith("identifier");
	}

	private void assertPhase(Method method, TransactionPhase expected) {
		assertThat(method).as("Method must not be null").isNotNull();
		TransactionalEventListener annotation =
				AnnotatedElementUtils.findMergedAnnotation(method, TransactionalEventListener.class);
		assertThat(annotation.phase()).as("Wrong phase for '" + method + "'").isEqualTo(expected);
	}

	private void supportsEventType(boolean match, Method method, ResolvableType eventType) {
		ApplicationListenerMethodAdapter adapter = createTestInstance(method);
		assertThat(adapter.supportsEventType(eventType)).as("Wrong match for event '" + eventType + "' on " + method).isEqualTo(match);
	}

	private ApplicationListenerMethodTransactionalAdapter createTestInstance(Method m) {
		
		return new ApplicationListenerMethodTransactionalAdapter("test", SampleEvents.class, m) {
			
			@Override
			protected Object getTargetBean() {
				return new SampleEvents();
			}
		};
	}

	private ResolvableType createGenericEventType(Class<?> payloadType) {
		return ResolvableType.forClassWithGenerics(PayloadApplicationEvent.class, payloadType);
	}

	private static void assertErrorHandled(CapturingErrorHandler handler, Method method, Object event) {

		assertThat(handler.invoked).isTrue();
		assertThat(handler.event).isEqualTo(event);
		assertThat(handler.identifier).isEqualTo(ApplicationListenerMethodTransactionalAdapter.getDefaultIdFor(method));
	}

	static class SampleEvents {

		@TransactionalEventListener
		public void defaultPhase(String data) {
		}

		@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
		public void phaseSet(String data) {
		}

		@TransactionalEventListener(classes = {String.class, Integer.class},
				phase = TransactionPhase.AFTER_COMPLETION)
		public void phaseAndClassesSet() {
		}

		@TransactionalEventListener(String.class)
		public void valueSet() {
		}
		
		@TransactionalEventListener
		public void throwing(String data) {
			throw new RuntimeException(data);
		}

		@TransactionalEventListener(id = "identifier")
		public void identified(String data) {
		}
	}

	static class CapturingCompletionCallback implements CompletionCallback {
		
		boolean invoked = false;
		ApplicationEvent event;
		String identifier;
		
		@Override
		public void onCompletion(ApplicationEvent event, String listenerIdentifier) {
			
			this.invoked = true;
			this.event = event;
			this.identifier = listenerIdentifier;
		}
	}
	
	static class CapturingErrorHandler implements org.springframework.transaction.event.TransactionalEventListenerMetadata.ErrorHandler {
		
		final boolean returnException;
		
		boolean invoked = false;
		ApplicationEvent event;
		String identifier;
		
		public CapturingErrorHandler(boolean returnException) {
			this.returnException = returnException;
		}
		
		@Override
		public RuntimeException onError(ApplicationEvent event, String listenerIdentifier,
				RuntimeException exception) {
			
			this.invoked = true;
			this.event = event;
			this.identifier = listenerIdentifier;
			
			return returnException ? exception : null;
		}
	}
	
	private static void runInTransaction(Runnable runnable, boolean commit) {
		
		TransactionSynchronizationManager.setActualTransactionActive(true);
		TransactionSynchronizationManager.initSynchronization();
		
		try {
			
			runnable.run();
			
			if (commit) {
			
				TransactionSynchronizationManager.getSynchronizations().forEach(it -> {
					it.afterCommit();
					it.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
				});
				
			} else {
				
				TransactionSynchronizationManager.getSynchronizations().forEach(it -> {
					it.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
				});
			}
			
		} finally {
			TransactionSynchronizationManager.clearSynchronization();
			TransactionSynchronizationManager.setActualTransactionActive(false);
		}
	}
}
