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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Feature;
import org.springframework.context.annotation.FeatureConfiguration;
import org.springframework.context.config.InvalidSpecificationException;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;

/**
 * Test fixture for {@link MvcViewControllers} feature specification.
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public class MvcViewControllersTests {

	@Test
	public void testMvcViewControllers() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(MvcViewControllersFeature.class);
		ctx.refresh();
		SimpleControllerHandlerAdapter adapter = ctx.getBean(SimpleControllerHandlerAdapter.class);
		assertNotNull(adapter);
		SimpleUrlHandlerMapping handler = ctx.getBean(SimpleUrlHandlerMapping.class);
		assertNotNull(handler);
		Map<String, ?> urlMap = handler.getUrlMap();
		assertNotNull(urlMap);
		assertEquals(2, urlMap.size());
		ParameterizableViewController controller = (ParameterizableViewController) urlMap.get("/");
		assertNotNull(controller);
		assertEquals("home", controller.getViewName());
		controller = (ParameterizableViewController) urlMap.get("/account");
		assertNotNull(controller);
		assertNull(controller.getViewName());
	}

	@Test
	public void testEmptyPathViewController() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(EmptyPathViewControllersFeature.class);
		try {
			ctx.refresh();
		} catch (InvalidSpecificationException e) {
			assertTrue(e.getMessage().contains("not empty"));
		}
	}

	@FeatureConfiguration
	private static class MvcViewControllersFeature {

		@SuppressWarnings("unused")
		@Feature
		public MvcViewControllers mvcViewControllers() {
			return new MvcViewControllers("/", "home").viewController("/account");
		}

	}

	@FeatureConfiguration
	private static class EmptyPathViewControllersFeature {

		@SuppressWarnings("unused")
		@Feature
		public MvcViewControllers mvcViewControllers() {
			return new MvcViewControllers("/", "");
		}

	}

}

