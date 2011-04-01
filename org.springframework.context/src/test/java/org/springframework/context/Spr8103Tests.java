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

	static class MySingleton {
		public void setName(String name) { }
	}
}