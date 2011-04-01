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

package org.springframework.context;

import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class Spr8103Tests {

	/*
	 * The following results in throwing
	 *
	 *     org.springframework.beans.factory.BeanInitializationException: Could not load properties;
	 *     nested exception is java.io.FileNotFoundException: class path resource [does/not/exist]
	 *     cannot be opened because it does not exist
	 *
	 * This is as expected.
	 */
	@Test(expected=BeanInitializationException.class)
	public void repro() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		bf.registerBeanDefinition("ppc", BeanDefinitionBuilder
				.rootBeanDefinition(PropertyPlaceholderConfigurer.class)
				.addPropertyValue("location", "classpath:/does/not/exist")
				.getBeanDefinition()
				);
		bf.registerBeanDefinition("singleton", BeanDefinitionBuilder
				.rootBeanDefinition(MySingleton.class)
				.addPropertyValue("name", "${something}")
				.getBeanDefinition());

		new GenericApplicationContext(bf).refresh();
	}

	/*
	 * Fails with FileNotFoundException as root cause - this is what we
	 * would want and expect.
	 */
	@Test
	public void repro2() {
		new GenericXmlApplicationContext(
				new ClassPathResource("SPR8103Tests-server-context.xml", Spr8103Tests.class));
	}

	static class MySingleton {
		public void setName(String name) { }
	}


	static class Bean1Impl {

		public Object someStringValue;
		public Object refBean2;

		public void start() {
		}

		public Bean1Impl() {
		}

		public void setSomeStringValue(Object arg0) {
		}

		public void setRefBean2(Object arg0) {
		}

	}


	static class Bean2Impl {

		public Long someLongValue;

		public Bean2Impl() {
		}

		public void setSomeLongValue(Long arg0) {
			System.out.println(arg0);
		}

	}

}