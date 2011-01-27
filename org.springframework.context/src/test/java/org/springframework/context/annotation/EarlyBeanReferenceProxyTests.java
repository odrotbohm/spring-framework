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

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodParameter;

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
	public void proxyToStringAvoidsEagerInstantiation() throws Exception {
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);

		TestBean proxy = (TestBean) pc.createProxy(descriptorFor(TestBean.class));

		assertThat(proxy.toString(), equalTo("EarlyBeanReferenceProxy for bean of type TestBean"));
	}

	@Test(expected=NoSuchBeanDefinitionException.class)
	public void proxyThrowsNoSuchBeanDefinitionExceptionWhenDelegatingMethodCallToNonExistentBean() throws Exception {
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		TestBean proxy = (TestBean) pc.createProxy(descriptorFor(TestBean.class));

		proxy.getName();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void proxyHashCodeMethodThrowsUnsupportedOperationException() throws Exception {
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		TestBean proxy = (TestBean) pc.createProxy(descriptorFor(TestBean.class));

		try {
			proxy.hashCode();
		} catch (UnsupportedOperationException ex) {
			assertThat(ex.getMessage().startsWith("equals() and hashCode() methods on"), is(true));
			throw ex;
		}
	}

	@Test(expected=UnsupportedOperationException.class)
	public void proxyEqualsMethodThrowsUnsupportedOperationException() throws Exception {
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		TestBean proxy = (TestBean) pc.createProxy(descriptorFor(TestBean.class));

		try {
			proxy.equals(new Object());
		} catch (UnsupportedOperationException ex) {
			assertThat(ex.getMessage().startsWith("equals() and hashCode() methods on"), is(true));
			throw ex;
		}
	}

	@Test
	public void proxyMethodsDelegateToTargetBeanCausingSingletonRegistrationIfNecessary() throws Exception {
		bf.registerBeanDefinition("testBean",
				BeanDefinitionBuilder.rootBeanDefinition(TestBean.class)
				.addPropertyValue("name", "testBeanName").getBeanDefinition());
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		TestBean proxy = (TestBean) pc.createProxy(descriptorFor(TestBean.class));

		assertThat(bf.containsBeanDefinition("testBean"), is(true));
		assertThat(bf.containsSingleton("testBean"), is(false));
		assertThat(proxy.getName(), equalTo("testBeanName"));
		assertThat(bf.containsSingleton("testBean"), is(true));
	}

	@Test
	public void beanAnnotatedMethodsReturnEarlyProxyAsWell() throws Exception {
		bf.registerBeanDefinition("componentWithInterfaceBeanMethod", new RootBeanDefinition(ComponentWithInterfaceBeanMethod.class));
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		ComponentWithInterfaceBeanMethod proxy = (ComponentWithInterfaceBeanMethod) pc.createProxy(descriptorFor(ComponentWithInterfaceBeanMethod.class));
		status.createEarlyBeanReferenceProxies = true;

		ITestBean bean = proxy.aBeanMethod();
		assertThat(bean, instanceOf(EarlyBeanReferenceProxy.class));
		assertThat(bf.containsBeanDefinition("componentWithInterfaceBeanMethod"), is(true));
		assertThat("calling a @Bean method on an EarlyBeanReferenceProxy object " +
				"should not cause its instantation/registration",
				bf.containsSingleton("componentWithInterfaceBeanMethod"), is(false));

		Object obj = proxy.normalInstanceMethod();
		assertThat(bf.containsSingleton("componentWithInterfaceBeanMethod"), is(true));
		assertThat(obj, not(instanceOf(EarlyBeanReferenceProxy.class)));
	}

	@Test
	public void beanAnnotatedMethodsWithInterfaceReturnTypeAreAllowed() throws Exception {
		bf.registerBeanDefinition("componentWithInterfaceBeanMethod", new RootBeanDefinition(ComponentWithInterfaceBeanMethod.class));
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		ComponentWithInterfaceBeanMethod proxy = (ComponentWithInterfaceBeanMethod) pc.createProxy(descriptorFor(ComponentWithInterfaceBeanMethod.class));
		status.createEarlyBeanReferenceProxies = true;

		ITestBean bean = proxy.aBeanMethod();
		assertThat(bean, instanceOf(EarlyBeanReferenceProxy.class));
	}

	@Test
	public void beanAnnotatedMethodsWithConcreteReturnTypeAreAllowed() throws Exception {
		bf.registerBeanDefinition("componentWithConcreteBeanMethod", new RootBeanDefinition(ComponentWithConcreteBeanMethod.class));
		EarlyBeanReferenceProxyCreator pc = new EarlyBeanReferenceProxyCreator(bf, status);
		ComponentWithConcreteBeanMethod proxy = (ComponentWithConcreteBeanMethod) pc.createProxy(descriptorFor(ComponentWithConcreteBeanMethod.class));
		status.createEarlyBeanReferenceProxies = true;

		TestBean bean = proxy.aBeanMethod();
		assertThat(bean, instanceOf(EarlyBeanReferenceProxy.class));
	}

	private DependencyDescriptor descriptorFor(Class<?> paramType) throws Exception {
		@SuppressWarnings("unused")
		class C {
			void m(TestBean p) { }
			void m(ComponentWithConcreteBeanMethod p) { }
			void m(ComponentWithInterfaceBeanMethod p) { }
		}

		Method targetMethod = C.class.getDeclaredMethod("m", new Class<?>[] { paramType });
		MethodParameter mp = new MethodParameter(targetMethod, 0);
		DependencyDescriptor dd = new DependencyDescriptor(mp, true, false);
		return dd;
	}


	static class ComponentWithConcreteBeanMethod {
		@Bean
		public TestBean aBeanMethod() {
			return new TestBean("concrete");
		}

		public Object normalInstanceMethod() {
			return new Object();
		}
	}


	static class ComponentWithInterfaceBeanMethod {
		@Bean
		public ITestBean aBeanMethod() {
			return new TestBean("interface");
		}

		public Object normalInstanceMethod() {
			return new Object();
		}
	}
}
