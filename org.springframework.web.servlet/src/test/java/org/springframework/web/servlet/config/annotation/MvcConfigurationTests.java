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

package org.springframework.web.servlet.config.annotation;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMethodAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMethodExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

/**
 * A test fixture with an {@link MvcConfiguration} and a mock {@link MvcConfigurer} for verifying delegation.
 * 
 * @author Rossen Stoyanchev
 */
public class MvcConfigurationTests {

	private MvcConfiguration mvcConfiguration;

	private MvcConfigurer configurer;
	
	@Before
	public void setUp() {
		configurer = EasyMock.createMock(MvcConfigurer.class);
		mvcConfiguration = new MvcConfiguration();
		mvcConfiguration.setConfigurers(Arrays.asList(configurer));
	}
	
	@Test
	public void annotationHandlerAdapter() {
		Capture<LocalValidatorFactoryBean> validator = new Capture<LocalValidatorFactoryBean>();
		Capture<FormattingConversionService> conversionService = new Capture<FormattingConversionService>();
		Capture<List<HandlerMethodArgumentResolver>> resolvers = new Capture<List<HandlerMethodArgumentResolver>>();
		Capture<List<HttpMessageConverter<?>>> converters = new Capture<List<HttpMessageConverter<?>>>();

		expect(configurer.getCustomValidator()).andReturn(null);
		configurer.configureValidator(capture(validator));
		configurer.registerFormatters(capture(conversionService));
		configurer.addCustomArgumentResolvers(capture(resolvers));
		configurer.configureMessageConverters(capture(converters));
		replay(configurer);

		RequestMappingHandlerMethodAdapter adapter = mvcConfiguration.annotationHandlerAdapter();

		// TODO: use getBindingInitializer() to check conversionService, validator
		
		assertEquals(0, resolvers.getValue().size());
		assertTrue(converters.getValue().size() > 0);
		assertEquals(converters.getValue(), adapter.getMessageConverters());
		
		verify(configurer);
	}

	@Test
	public void getCustomValidator() {
		expect(configurer.getCustomValidator()).andReturn(new LocalValidatorFactoryBean());
		replay(configurer);

		mvcConfiguration.validator();
		
		verify(configurer);
	}
	
	@Test
	public void configureValidator() {
		Capture<LocalValidatorFactoryBean> validator = new Capture<LocalValidatorFactoryBean>();

		expect(configurer.getCustomValidator()).andReturn(null);
		configurer.configureValidator(capture(validator));
		replay(configurer);

		mvcConfiguration.validator();

		verify(configurer);
	}

	@Test
	public void handlerExceptionResolver() throws Exception {
		Capture<List<HttpMessageConverter<?>>> converters = new Capture<List<HttpMessageConverter<?>>>();
		Capture<List<HandlerExceptionResolver>> exceptionResolvers = new Capture<List<HandlerExceptionResolver>>();

		configurer.configureMessageConverters(capture(converters));
		configurer.configureHandlerExceptionResolvers(capture(exceptionResolvers));
		replay(configurer);

		mvcConfiguration.handlerExceptionResolver();

		assertEquals(3, exceptionResolvers.getValue().size());
		assertTrue(exceptionResolvers.getValue().get(0) instanceof RequestMappingHandlerMethodExceptionResolver);
		assertTrue(exceptionResolvers.getValue().get(1) instanceof ResponseStatusExceptionResolver);
		assertTrue(exceptionResolvers.getValue().get(2) instanceof DefaultHandlerExceptionResolver);
		assertTrue(converters.getValue().size() > 0);
		
		verify(configurer);
	}
	
}
