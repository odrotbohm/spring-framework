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
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Enable;
import org.springframework.context.config.AdviceMode;
import org.springframework.core.Ordered;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Enable(AsyncCapability.class)
public @interface EnableAsync {

	/**
	 * Indicate the 'async' annotation type to be detected at either class
	 * or method level. By default, both the {@link Async} annotation and
	 * the EJB 3.1 <code>javax.ejb.Asynchronous</code> annotation will be
	 * detected. <p>This setter property exists so that developers can provide
	 * their own (non-Spring-specific) annotation type to indicate that a method
	 * (or all methods of a given class) should be invoked asynchronously.
	 */
	Class<? extends Annotation> annotation() default Annotation.class;

	/**
	 * Indicate the name of the {@link java.util.Executor} bean to use
	 * when invoking asynchronous methods. If not provided, an instance
	 * of {@link org.springframework.core.task.SimpleAsyncTaskExecutor}
	 * will be used by default.
	 */
	String executorName() default "";

	/**
	 * Indicate whether class-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies. The default is {@code false}
	 *
	 * <p>Note: Class-based proxies require the async {@link #annotation()}
	 * to be defined on the concrete class. Annotations in interfaces will
	 * not work in that case (they will rather only work with interface-based proxies)!
	 */
	boolean proxyTargetClass() default false;

	/**
	 * Indicate how async advice should be applied.
	 * The default is {@link AdviceMode#PROXY}.
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * Indicate the order in which the
	 * {@link org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor}
	 * should be applied. Defaults to Order.LOWEST_PRIORITY in order to run
	 * after all other post-processors, so that it can add an advisor to
	 * existing proxies rather than double-proxy.
	 */
	int order() default Ordered.LOWEST_PRECEDENCE;
}
