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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import test.beans.ITestBean;
import test.beans.TestBean;

/**
 * Unit tests for {@link EarlyBeanReferenceProxyCreator}. Ensure that
 * {@link EarlyBeanReferenceProxy} objects behave properly.
 *
 * @author Chris Beams
 * @since 3.1
 */
public class EarlyBeanReferenceProxyTests {

	private DefaultListableBeanFactory bf;
	private EarlyBeanReferenceProxyStatus status;

	@Before
	public void setUp() {
		bf = new DefaultListableBeanFactory();
		status = new EarlyBeanReferenceProxyStatus();
	}

	@Test
	public void proxyToStringAvoidsEagerInstantiation() {
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		TestBean proxy = pc.createProxy(TestBean.class);

		assertThat(proxy.toString(), equalTo("EarlyBeanReferenceProxy for bean of type TestBean"));
	}

	@Test(expected=NoSuchBeanDefinitionException.class)
	public void proxyThrowsNoSuchBeanDefinitionExceptionWhenDelegatingMethodCallToNonExistentBean() {
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		TestBean proxy = pc.createProxy(TestBean.class);

		proxy.getName();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void proxyHashCodeMethodThrowsUnsupportedOperationException() {
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		TestBean proxy = pc.createProxy(TestBean.class);

		try {
			proxy.hashCode();
		} catch (UnsupportedOperationException ex) {
			assertThat(ex.getMessage().startsWith("equals() and hashCode() methods on"), is(true));
			throw ex;
		}
	}

	@Test(expected=UnsupportedOperationException.class)
	public void proxyEqualsMethodThrowsUnsupportedOperationException() {
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		TestBean proxy = pc.createProxy(TestBean.class);

		try {
			proxy.equals(new Object());
		} catch (UnsupportedOperationException ex) {
			assertThat(ex.getMessage().startsWith("equals() and hashCode() methods on"), is(true));
			throw ex;
		}
	}

	@Test
	public void proxyMethodsDelegateToTargetBeanCausingSingletonRegistrationIfNecessary() {
		bf.registerBeanDefinition("testBean",
				BeanDefinitionBuilder.rootBeanDefinition(TestBean.class)
				.addPropertyValue("name", "testBeanName").getBeanDefinition());
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		TestBean proxy = pc.createProxy(TestBean.class);

		assertThat(bf.containsBeanDefinition("testBean"), is(true));
		assertThat(bf.containsSingleton("testBean"), is(false));
		assertThat(proxy.getName(), equalTo("testBeanName"));
		assertThat(bf.containsSingleton("testBean"), is(true));
	}

	@Test
	public void beanAnnotatedMethodsReturnEarlyProxyAsWell() {

		bf.registerBeanDefinition("componentWithBeanMethod", new RootBeanDefinition(ComponentWithBeanMethod.class));
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		ComponentWithBeanMethod proxy = pc.createProxy(ComponentWithBeanMethod.class);
		status.createEarlyBeanReferenceProxies = true;

		ITestBean bean = proxy.aBeanMethod();
		assertThat(bean, instanceOf(EarlyBeanReferenceProxy.class));
		assertThat(bf.containsBeanDefinition("componentWithBeanMethod"), is(true));
		assertThat("calling a @Bean method on an EarlyBeanReferenceProxy object " +
				"should not cause its instantation/registration",
				bf.containsSingleton("componentWithBeanMethod"), is(false));

		Object obj = proxy.normalInstanceMethod();
		assertThat(bf.containsSingleton("componentWithBeanMethod"), is(true));
		assertThat(obj, not(instanceOf(EarlyBeanReferenceProxy.class)));
	}

	static class ComponentWithBeanMethod {
		@Bean
		public ITestBean aBeanMethod() {
			return new TestBean("foo");
		}

		public Object normalInstanceMethod() {
			return new Object();
		}
	}
}
