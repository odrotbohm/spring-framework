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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests covering the use of @PostContstruct within @Configuration classes
 *
 * @author Chris Beams
 * @since 3.1
 */
public class ConfigurationClassPostConstructTests {
	
	@Test
	public void foo() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config1.class);
		ctx.refresh();
	}

	@Test
	public void succeed() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config1.class, Config2.class);
		ctx.refresh();

		assertions(ctx);

		Config2 config2 = ctx.getBean(Config2.class);
		assertThat(config2.testBean, is(ctx.getBean(TestBean.class)));
	}

	@Test
	public void fail() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config2.class, Config1.class);
		ctx.refresh();

		assertions(ctx);
	}

	@Test
	public void workaround() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config3.class, Config1.class);
		ctx.refresh();

		assertions(ctx);
	}


	private void assertions(AnnotationConfigApplicationContext ctx) {
		Config1 config1 = ctx.getBean(Config1.class);
		TestBean testBean = ctx.getBean(TestBean.class);
		assertThat(config1.beanMethodCallCount, is(1));
		assertThat(testBean.getAge(), is(2));
	}


	@Configuration
	static class Config1 {

		int beanMethodCallCount = 0;

		@PostConstruct
		public void init() {
			System.out.println("ConfigurationClassPostConstructTests.Config1.init()");
			//beanMethod().setAge(beanMethod().getAge() + 1); // age == 2
			beanMethod();
		}

		@Bean
		public TestBean beanMethod() {
			System.out.println("ConfigurationClassPostConstructTests.Config1.beanMethod()");
			beanMethodCallCount++;
			TestBean testBean = new TestBean();
			testBean.setAge(1);
			return testBean;
		}
	}


	@Configuration
	static class Config2 {
		TestBean testBean;

		@Autowired
		void setTestBean(TestBean testBean) {
			this.testBean = testBean;
		}
	}

	@Configuration
	static class Config3 {
		TestBean testBean;

		@Autowired
		void setTestBean(Config1 config1) {
			this.testBean = config1.beanMethod();
		}
	}
}
