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
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Integration tests for processing ComponentScan-annotated Configuration
 * classes.
 *
 * @author Chris Beams
 * @since 3.1
 */
public class ComponentScanAnnotationIntegrationTests {
	@Test
	public void controlScan() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.scan(example.scannable._package.class.getPackage().getName());
		ctx.refresh();
		assertThat("control scan for example.scannable package failed to register FooServiceImpl bean",
				ctx.containsBean("fooServiceImpl"), is(true));
	}

	@Test
	public void viaContextRegistration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ComponentScanAnnotatedConfig.class);
		ctx.refresh();
		ctx.getBean(ComponentScanAnnotatedConfig.class);
		ctx.getBean(TestBean.class);
		assertThat("config class bean not found", ctx.containsBeanDefinition("componentScanAnnotatedConfig"), is(true));
		assertThat("@ComponentScan annotated @Configuration class registered directly against " +
				"AnnotationConfigApplicationContext did not trigger component scanning as expected",
				ctx.containsBean("fooServiceImpl"), is(true));
	}

	@Test
	public void viaContextRegistration_WithValueAttribute() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ComponentScanAnnotatedConfig_WithValueAttribute.class);
		ctx.refresh();
		ctx.getBean(ComponentScanAnnotatedConfig_WithValueAttribute.class);
		ctx.getBean(TestBean.class);
		assertThat("config class bean not found", ctx.containsBeanDefinition("componentScanAnnotatedConfig_WithValueAttribute"), is(true));
		assertThat("@ComponentScan annotated @Configuration class registered directly against " +
				"AnnotationConfigApplicationContext did not trigger component scanning as expected",
				ctx.containsBean("fooServiceImpl"), is(true));
	}

	@Test
	public void viaBeanRegistration() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		bf.registerBeanDefinition("componentScanAnnotatedConfig",
				genericBeanDefinition(ComponentScanAnnotatedConfig.class).getBeanDefinition());
		bf.registerBeanDefinition("configurationClassPostProcessor",
				genericBeanDefinition(ConfigurationClassPostProcessor.class).getBeanDefinition());
		GenericApplicationContext ctx = new GenericApplicationContext(bf);
		ctx.refresh();
		ctx.getBean(ComponentScanAnnotatedConfig.class);
		ctx.getBean(TestBean.class);
		assertThat("config class bean not found", ctx.containsBeanDefinition("componentScanAnnotatedConfig"), is(true));
		assertThat("@ComponentScan annotated @Configuration class registered " +
				"as bean definition did not trigger component scanning as expected",
				ctx.containsBean("fooServiceImpl"), is(true));
	}

	@Test
	public void invalidComponentScanDeclaration_noPackagesSpecified() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ComponentScanWithNoPackagesConfig.class);
		try {
			ctx.refresh();
			fail("Expected exception when parsing @ComponentScan definition that declares no packages");
		} catch (BeanDefinitionParsingException ex) {
			assertThat(ex.getMessage(), containsString("@ComponentScan must declare either 'value' or 'packageOf'"));
		}
	}
}


@Configuration
@ComponentScan(packageOf=example.scannable._package.class)
class ComponentScanAnnotatedConfig {
	@Bean
	public TestBean testBean() {
		return new TestBean();
	}
}

@Configuration
@ComponentScan("example.scannable")
class ComponentScanAnnotatedConfig_WithValueAttribute {
	@Bean
	public TestBean testBean() {
		return new TestBean();
	}
}

@Configuration
@ComponentScan
class ComponentScanWithNoPackagesConfig { }