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

import org.springframework.context.AbstractFeatureSpecification;
import org.springframework.context.FeatureSpecificationExecutor;
import org.springframework.context.InvalidSpecificationException;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;

/**
 * TODO
 * 
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public class MvcAnnotationDriven extends AbstractFeatureSpecification {

	private static final Class<? extends FeatureSpecificationExecutor> EXECUTOR_TYPE = MvcAnnotationDrivenExecutor.class;

	private Object conversionService;

	private Object validator;

	private Object messageCodesResolver;

	/**
	 * Creates an MvcAnnotationDriven specification.
	 */
	public MvcAnnotationDriven() {
		super(EXECUTOR_TYPE);
	}

	/**
	 * <p> The ConversionService bean instance to use for type conversion during 
	 * field binding. This is not required input. It only needs to be provided 
	 * explicitly if custom converters or formatters need to be configured.
	 * 
	 * <p> If not provided, a default FormattingConversionService is registered 
	 * that contains converters to/from standard JDK types. In addition, full 
	 * support for date/time formatting will be installed if the Joda Time 
	 * library is present on the classpath.
	 * 
	 * @param conversionService the ConversionService instance to use
	 */
	public MvcAnnotationDriven conversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
		return this;
	}

	/**
	 * <p> The ConversionService to use for type conversion during field binding. 
	 * This is an alternative to {@link #conversionService(ConversionService)} 
	 * allowing you to provide a bean name rather than a bean instance.
	 * 
	 * @param conversionService the ConversionService bean name
	 */
	public MvcAnnotationDriven conversionService(String conversionService) {
		this.conversionService = conversionService;
		return this;
	}

	Object conversionService() {
		return this.conversionService;
	}

	/**
	 * <p> The Validator bean instance to use to validate Controller model objects.
	 * This is not required input. It only needs to be specified explicitly if 
	 * a custom Validator needs to be configured.
	 * 
	 * <p> If not specified, JSR-303 validation will be installed if a JSR-303 
	 * provider is present on the classpath.
	 * 
	 * @param validator the Validator bean instance
	 */
	public MvcAnnotationDriven validator(Validator validator) {
		this.validator = validator;
		return this;
	}

	/**
	 * <p> The Validator bean instance to use to validate Controller model objects.
	 * This is an alternative to {@link #validator(Validator)} allowing you to 
	 * provide a bean name rather than a bean instance.
	 * 
	 * @param validator the Validator bean name
	 */
	public MvcAnnotationDriven validator(String validator) {
		this.validator = validator;
		return this;
	}

	Object validator() {
		return this.validator;
	}

	/**
	 * <p> The MessageCodesResolver to use to build message codes from data binding 
	 * and validation error codes. This is not required input. If not specified 
	 * the DefaultMessageCodesResolver is used.
	 * 
	 * @param messageCodesResolver the MessageCodesResolver bean instance
	 */
	public MvcAnnotationDriven messageCodesResolver(MessageCodesResolver messageCodesResolver) {
		this.messageCodesResolver = messageCodesResolver;
		return this;
	}

	/**
	 * <p> The MessageCodesResolver to use to build message codes from data binding 
	 * and validation error codes. This is an alternative to 
	 * {@link #messageCodesResolver(MessageCodesResolver)} allowing you to provide 
	 * a bean name rather than a bean instance.
	 * 
	 * @param messageCodesResolver the MessageCodesResolver bean name
	 */
	public MvcAnnotationDriven messageCodesResolver(String messageCodesResolver) {
		this.messageCodesResolver = messageCodesResolver;
		return this;
	}

	Object messageCodesResolver() {
		return this.messageCodesResolver;
	}

	public void validate() throws InvalidSpecificationException {
	}

}
