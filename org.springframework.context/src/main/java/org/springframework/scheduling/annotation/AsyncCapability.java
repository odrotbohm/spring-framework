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

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ContainerCapability;
import org.springframework.context.config.AdviceMode;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.scheduling.config.AnnotationDrivenBeanDefinitionParser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class AsyncCapability implements ContainerCapability {

	public static final String ASYNC_EXECUTION_ASPECT_CLASS_NAME =
			"org.springframework.scheduling.aspectj.AnnotationAsyncExecutionAspect";

	public void enable(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata) {
		if (registry.containsBeanDefinition(AsyncAnnotationBeanPostProcessor.DEFAULT_BEAN_NAME)) {
			throw new IllegalStateException(
					"Only one AsyncAnnotationBeanPostProcessor may exist within the context. " +
					"Did you declare @EnableAsync more than once?");
		}

		Map<String, Object> asyncCapableAttributes = annotationMetadata.getAnnotationAttributes(EnableAsync.class.getName());
		Assert.notEmpty(asyncCapableAttributes,
				"@EnableAsync annotation was not found on " + annotationMetadata.getClassName());

		if ((AdviceMode) asyncCapableAttributes.get("mode") == AdviceMode.ASPECTJ) {
			registerAsyncExecutionAspect(asyncCapableAttributes, registry);
		} else {
			RootBeanDefinition def = new RootBeanDefinition(AsyncAnnotationBeanPostProcessor.class);
			def.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			MutablePropertyValues pvs = new MutablePropertyValues();
			@SuppressWarnings("unchecked")
			Class<? extends Annotation> asyncAnnotation =
				(Class<? extends Annotation>) asyncCapableAttributes.get("annotation");
			if (!asyncAnnotation.equals(AnnotationUtils.getDefaultValue(EnableAsync.class, "annotation"))) {
				pvs.add("asyncAnnotationType", asyncAnnotation);
			}
			String executorBeanName = (String) asyncCapableAttributes.get("executorName");
			if (StringUtils.hasText(executorBeanName)) {
				pvs.add("executor", new RuntimeBeanReference(executorBeanName));
			}
			if (((Boolean)asyncCapableAttributes.get("proxyTargetClass"))) {
				pvs.addPropertyValue("proxyTargetClass", true);
			}
			pvs.addPropertyValue("order", asyncCapableAttributes.get("order"));
			def.setPropertyValues(pvs);
			registry.registerBeanDefinition(AsyncAnnotationBeanPostProcessor.DEFAULT_BEAN_NAME, def);
		}
	}

	private void registerAsyncExecutionAspect(Map<String, Object> asyncCapableAttributes, BeanDefinitionRegistry registry) {
		if (!registry.containsBeanDefinition(AnnotationDrivenBeanDefinitionParser.ASYNC_EXECUTION_ASPECT_BEAN_NAME)) {
			BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
					ASYNC_EXECUTION_ASPECT_CLASS_NAME);

			builder.setFactoryMethod("aspectOf");
			String executor = (String)asyncCapableAttributes.get("executorName");
			if (StringUtils.hasText(executor)) {
				builder.addPropertyReference("executor", executor);
			}
		}
	}
}
