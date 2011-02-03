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
package org.springframework.web.servlet.config;

import org.springframework.context.config.AbstractFeatureSpecification;
import org.springframework.context.config.FeatureSpecificationExecutor;
import org.springframework.context.config.InvalidSpecificationException;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * Specifies the Spring MVC "resources" container feature. The
 * feature provides the following fine-grained configuration:
 *  
 * <ul>
 * 	<li>{@link ResourceHttpRequestHandler} to serve static resources from a
 * 		list of web-root relative, classpath, or other locations.
 * 	<li>{@link SimpleUrlHandlerMapping} to map the above request handler to a
 * 		a specific path pattern (e.g. "/resources/**").
 * 	<li>{@link HttpRequestHandlerAdapter} to enable the DispatcherServlet to be 
 * 		able to invoke the above request handler. 
 * </ul>
 *
 * @author Rossen Stoynchev
 * @since 3.1
 */
public class MvcResources extends AbstractFeatureSpecification {

	private static final Class<? extends FeatureSpecificationExecutor> EXECUTOR_TYPE = MvcResourcesExecutor.class;

	private Object[] locations;

	private String mapping;

	private Object cachePeriod;
	
	private Object order = Ordered.LOWEST_PRECEDENCE - 1;

	/**
	 * Creates an MvcResources specification instance.
	 * See alternate constructor defined here if you prefer to use the Spring
	 * {@link Resource} type for specifying locations.
	 * 
	 * @param mapping - the URL path pattern within the current Servlet context to 
	 * 		use to identify resource requests (e.g. "/resources/**").
	 * 
	 * @param locations - resource locations to serve static content from specified 
	 * 		as a list of Spring {@link Resource} patterns. Each location must point 
	 * 		to a valid directory. Locations will be checked in the order specified. 
	 * 		For example if "/" and "classpath:/META-INF/public-web-resources/" are
	 * 		configured resources will be served from the Web root and from any JAR on 
	 * 		the classpath  that contains a /META-INF/public-web-resources/ directory,
	 * 		with resources under the Web root taking precedence.
	 */
	public MvcResources(String mapping, String... locations) {
		super(EXECUTOR_TYPE);
		this.locations = locations;
		this.mapping = mapping;
	}

	/**
	 * Creates an MvcResources specification instance. See alternate constructor 
	 * defined here if you prefer to use String-based path patterns.
	 * 
	 * @param mapping - the URL path pattern within the current Servlet context to 
	 * 		use to identify resource requests (e.g. "/resources/**").
	 * 
	 * @param locations - resource locations to serve static content from specified 
	 * 		as a list of Spring {@link Resource} patterns. Each location must point 
	 * 		to a valid directory. Locations will be checked in the order specified. 
	 */
	public MvcResources(String mapping, Resource... locations) {
		super(EXECUTOR_TYPE);
		this.locations = locations;
		this.mapping = mapping;
	}

	/**
	 * The period of time resources should be cached for in seconds.
	 * The default is to not send any cache headers but rather to rely on 
	 * last-modified timestamps only. 
	 * Set this to 0 in order to send cache headers that prevent caching, 
	 * or to a positive number of seconds in order to send cache headers 
	 * with the given max-age value.
	 * 
	 * @param cachePeriod the cache period in seconds
	 */
	public MvcResources cachePeriod(Integer cachePeriod) {
		this.cachePeriod = cachePeriod;
		return this;
	}

	/**
	 * Specify a cachePeriod as a String. An alternative to {@link #cachePeriod(Integer)}. 
	 * The String must represent an Integer after placeholder and SpEL expression 
	 * resolution. 
	 * 
	 * @param cachePeriod the cache period in seconds
	 */
	public MvcResources cachePeriod(String cachePeriod) {
		this.cachePeriod = cachePeriod;
		return this;
	}

	/**
	 * Specify a cachePeriod as a String. An alternative to {@link #cachePeriod(Integer)}. 
	 * The String must represent an Integer after placeholder and SpEL expression 
	 * resolution. 
	 * Sets the order for the SimpleUrlHandlerMapping used to match resource
	 * requests relative to order value for other HandlerMapping instances
	 * such as the {@link DefaultAnnotationHandlerMapping} used to match 
	 * controller requests. 
	 * 
	 * @param order the order to use. The default value is 
	 * 		{@link Ordered#LOWEST_PRECEDENCE} - 1.
	 */
	public MvcResources order(Integer order) {
		this.order = order;
		return this;
	}

	/**
	 * Specify an order as a String. An alternative to {@link #order(Integer)}. 
	 * The String must represent an Integer after placeholder and SpEL expression 
	 * resolution. 
	 * 
	 * @param order the order to use. The default value is 
	 * 		{@link Ordered#LOWEST_PRECEDENCE} - 1.
	 */
	public MvcResources order(String order) {
		this.order = order;
		return this;
	}

	// Package private accessors

	Object cachePeriod() {
		return cachePeriod;
	}

	Object[] locations() {
		return this.locations;
	}

	String mapping() {
		return mapping;
	}
	
	Object order() {
		return order;
	}

	public void validate() throws InvalidSpecificationException {
		if (!StringUtils.hasText(mapping)) {
			throw new InvalidSpecificationException("A mapping is required.");
		}
		if (locations == null || locations.length == 0) {
			throw new InvalidSpecificationException("Location(s) are required. ");
		}
	}

}
