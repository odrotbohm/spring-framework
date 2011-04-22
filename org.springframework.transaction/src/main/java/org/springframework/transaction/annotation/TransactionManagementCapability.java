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

package org.springframework.transaction.annotation;

import java.util.Map;

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ContainerCapability;
import org.springframework.context.config.AdviceMode;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.StringUtils;

/**
 * Enables Spring's transaction management capability by parsing
 * the * @{@link EnableTransactionManagement} annotation and
 * registering infrastructure bean definitions as appropriate based
 * on the metadata within.
 *
 * @author Chris Beams
 * @since 3.1
 * @see EnableTransactionManagement
 * @see org.springframework.scheduling.config.AnnotationDrivenBeanDefinitionParser
 */
public class TransactionManagementCapability implements ContainerCapability {

	/**
	 * The bean name of the internally managed transaction advisor (used when mode == PROXY).
	 */
	public static final String TRANSACTION_ADVISOR_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAdvisor";

	/**
	 * The bean name of the internally managed transaction aspect (used when mode == ASPECTJ).
	 */
	public static final String TRANSACTION_ASPECT_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAspect";

	/**
	 * The class name of the AspectJ transaction management aspect.
	 */
	public static final String TRANSACTION_ASPECT_CLASS_NAME =
			"org.springframework.transaction.aspectj.AnnotationTransactionAspect";


	public void enable(BeanDefinitionRegistry registry, AnnotationMetadata metadata) {
		// TODO: guard against multiple calls to enable() within the same container
		// see SchedulingCapability for an example.

		Map<String, Object> transactionCapableAttributes = metadata.getAnnotationAttributes(EnableTransactionManagement.class.getName());
		AdviceMode mode = (AdviceMode)transactionCapableAttributes.get("mode");
		switch (mode) {
			case ASPECTJ:
				registerTransactionAspect(transactionCapableAttributes, registry);
				break;
			case PROXY:
				AopAutoProxyConfigurer.configureAutoProxyCreator(transactionCapableAttributes, registry);
				break;
			default:
				throw new IllegalArgumentException(
						String.format("AdviceMode %s is not supported", mode));
		}
	}

	private void registerTransactionAspect(Map<String, Object> transactionCapableAttributes, BeanDefinitionRegistry registry) {
		if (!registry.containsBeanDefinition(TRANSACTION_ASPECT_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition();
			def.setBeanClassName(TRANSACTION_ASPECT_CLASS_NAME);
			def.setFactoryMethodName("aspectOf");
			String txManagerBeanName = (String)transactionCapableAttributes.get("transactionManagerName");
			if (StringUtils.hasText(txManagerBeanName)) {
				def.getPropertyValues().add("transactionManagerBeanName", txManagerBeanName);
			}
			registry.registerBeanDefinition(TRANSACTION_ASPECT_BEAN_NAME, def);
		}
	}


	/**
	 * Inner class to just introduce an AOP framework dependency when actually in proxy mode.
	 */
	private static class AopAutoProxyConfigurer {

		public static void configureAutoProxyCreator(Map<String, Object> transactionCapableAttributes, BeanDefinitionRegistry registry) {
			AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
			if (((Boolean)transactionCapableAttributes.get("proxyTargetClass"))) {
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}

			if (!registry.containsBeanDefinition(TRANSACTION_ADVISOR_BEAN_NAME)) {

				// Create the TransactionAttributeSource definition.
				RootBeanDefinition sourceDef = new RootBeanDefinition(AnnotationTransactionAttributeSource.class);
				sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				String sourceName = BeanDefinitionReaderUtils.generateBeanName(sourceDef, registry);
				registry.registerBeanDefinition(sourceName, sourceDef);

				// Create the TransactionInterceptor definition.
				RootBeanDefinition interceptorDef = new RootBeanDefinition(TransactionInterceptor.class);
				interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				String txManagerBeanName = (String)transactionCapableAttributes.get("transactionManager");
				if (StringUtils.hasText(txManagerBeanName)) {
					interceptorDef.getPropertyValues().add("transactionManagerBeanName", txManagerBeanName);
				}
				interceptorDef.getPropertyValues().add("transactionAttributeSource", new RuntimeBeanReference(sourceName));
				String interceptorName = BeanDefinitionReaderUtils.generateBeanName(interceptorDef, registry);
				registry.registerBeanDefinition(interceptorName, interceptorDef);

				// Create the TransactionAttributeSourceAdvisor definition.
				RootBeanDefinition advisorDef = new RootBeanDefinition(BeanFactoryTransactionAttributeSourceAdvisor.class);
				advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				advisorDef.getPropertyValues().add("transactionAttributeSource", new RuntimeBeanReference(sourceName));
				advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);
				int order = (Integer)transactionCapableAttributes.get("order");
				if (order != Ordered.NOT_ORDERED) {
					advisorDef.getPropertyValues().add("order", order);
				}
				registry.registerBeanDefinition(TRANSACTION_ADVISOR_BEAN_NAME, advisorDef);
			}
		}
	}

}
