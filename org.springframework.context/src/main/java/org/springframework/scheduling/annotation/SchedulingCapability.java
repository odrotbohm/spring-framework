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

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ContainerCapability;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class SchedulingCapability implements ContainerCapability {

	public void enable(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata) {
		Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableScheduling.class.getName());
		Assert.notEmpty(attributes,
				"@EnableScheduling annotation was not found on " + annotationMetadata.getClassName());

		if (registry.containsBeanDefinition(AnnotationConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			throw new IllegalStateException(
					"Only one ScheduledAnnotationBeanPostProcessor may exist within the context. " +
					"Did you declare @EnableScheduling more than once?");
		}

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(
				ScheduledAnnotationBeanPostProcessor.class);
		String schedulerName = (String)attributes.get("schedulerName");
		if (StringUtils.hasText(schedulerName)) {
			// setting a property *value* here instead of ref to enable lazy bean lookup
			// and give the declaring @Configuration class time to be post-processed by SABPP
			builder.addPropertyValue("scheduler", schedulerName);
		}
		registry.registerBeanDefinition(
				AnnotationConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME,
				builder.getBeanDefinition());
	}

}
