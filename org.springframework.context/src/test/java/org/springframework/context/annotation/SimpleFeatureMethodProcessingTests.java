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

package org.springframework.context.annotation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.context.FeatureSpecification;

import test.beans.TestBean;

/**
 * Simple tests to ensure that @Feature methods are processed.
 *
 * @author Chris Beams
 * @since 3.1
 */
public class SimpleFeatureMethodProcessingTests {

	@Test
	public void test() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class);
		ctx.refresh();
		assertThat(ctx.getBean("testBean", TestBean.class).getName(), equalTo("foo"));
	}

	@FeatureConfiguration
	static class Config {
		@Feature
		public FeatureSpecification f() {
			return new BeanRegisteringSpecification("testBean", new TestBean("foo"));
		}
	}

}
