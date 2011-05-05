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

package org.springframework.scheduling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

	/**
	 * Indicate the name of the {@code TaskScheduler} or {@code ScheduledExecutorService}
	 * bean to use for processing @{@link Scheduled} tasks.
	 * <p>By default a {@code TaskScheduler} or {@code ScheduledExecutoryService} bean
	 * will be looked up by type. In cases where two or more such beans are present in
	 * the container, this attribute may be used to distinguish which one should be used
	 * for annotation-driven task scheduling.
	 * <p>As another alternative to using this attribute, consider registering a
	 * {@link org.springframework.scheduling.config.ScheduledTaskRegistrar} bean and
	 * wiring the scheduler bean against it directly.
	 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SchedulingConfiguration.class)
public @interface EnableScheduling {

}
