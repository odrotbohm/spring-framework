/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Unit tests for the @Enable annotation.
 *
 * @author Chris Beams
 * @since 3.1
 */
public class EnableAnnotationTests {

	@After
	public void tearDown() {
		InvocationRecordingCapability.enabled = false;
	}

	@Test
	public void withRecordingCapabilityEnabled() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(RecordingCapabilityEnabled.class);
		ctx.refresh();

		assertThat(InvocationRecordingCapability.enabled, is(true));
	}


	@EnableRecording
	@Configuration
	static class RecordingCapabilityEnabled {
	}


	@Test
	public void withRecordingCapabilityEnabledViaImport() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ImportsCapabilityAnnotatedConfiguration.class);
		ctx.refresh();

		assertThat(InvocationRecordingCapability.enabled, is(true));
	}


	@Configuration
	@Import(RecordingCapabilityEnabled.class)
	static class ImportsCapabilityAnnotatedConfiguration {
	}


	static class InvocationRecordingCapability implements ContainerCapability {
		static boolean enabled = false;

		public void enable(BeanDefinitionRegistry registry, AnnotationMetadata metadata) {
			enabled = true;
		}
	}


	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Enable(InvocationRecordingCapability.class)
	public @interface EnableRecording {
	}
}
