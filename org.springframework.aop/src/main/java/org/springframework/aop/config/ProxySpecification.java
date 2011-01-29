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

package org.springframework.aop.config;

import org.springframework.aop.framework.AopContext;

/**
 * Represents the subset of configuration information common to spring container
 * features that deal with AOP proxying, such as transaction management, AspectJ
 * auto-proxying, etc.
 *
 * <p>Designed for implementation by {@code FeatureSpecification} classes; as such,
 * methods do not follow get/set JavaBeans-style naming conventions. Rather, accessor
 * methods return Object, with the assumption that this return type will be narrowed
 * through covariant return types in the implementing class, which will return its 'this'
 * reference.
 * 
 * @author Chris Beams
 * @since 3.1
 */
public interface ProxySpecification {

	/**
	 * Return whether class-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies.
	 * @see #proxyTargetClass(Boolean)
	 */
	Boolean proxyTargetClass();

	/**
	 * Indicate whether class-based (CGLIB) proxies to be created. By default,
	 * standard Java interface-based proxies are created. Setting this value
	 * to true requires that CGLIB is available on the application's runtime
	 * classpath.
	 */
	Object proxyTargetClass(Boolean proxyTargetClass);

	/**
	 * Return whether proxies should be exposed by the AOP framework as a
	 * ThreadLocal for retrieval via the {@link AopContext} class.
	 * @see #exposeProxy(Boolean)
	 */
	Boolean exposeProxy();

	/**
	 * Indicate that the proxy should be exposed by the AOP framework as a
	 * ThreadLocal for retrieval via the {@link AopContext} class. Off by default,
	 * i.e. no guarantees that AopContext access will work.
	 */
	Object exposeProxy(Boolean exposeProxy);

}
