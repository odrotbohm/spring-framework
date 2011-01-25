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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

class EarlyBeanReferenceProxyCreator {

	private final ConfigurableListableBeanFactory beanFactory;
	private final EarlyBeanReferenceProxyStatus earlyBeanReferenceProxyStatus;

	public EarlyBeanReferenceProxyCreator(ConfigurableListableBeanFactory beanFactory, EarlyBeanReferenceProxyStatus earlyBeanReferenceProxyStatus) {
		this.beanFactory = beanFactory;
		this.earlyBeanReferenceProxyStatus = earlyBeanReferenceProxyStatus;
	}

	@SuppressWarnings("unchecked")
	public <T> T createProxy(Class<T> proxyType) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(proxyType);
		enhancer.setInterfaces(new Class<?>[] {EarlyBeanReferenceProxy.class});
		enhancer.setCallbacks(new Callback[] {
			new EarlyBeanReferenceProxyMethodInterceptor(this.beanFactory, this.earlyBeanReferenceProxyStatus),
			new ToStringInterceptor(),
			new EqualsAndHashCodeInterceptor(),
			new TargetBeanDelegatingMethodInterceptor(this.beanFactory)
		});
		enhancer.setCallbackFilter(new CallbackFilter() {
			public int accept(Method method) {
				if (AnnotationUtils.findAnnotation(method, Bean.class) != null) {
					return 0;
				}
				if (method.getName().equals("toString")) {
					return 1;
				}
				if (method.getName().equals("hashCode") || method.getName().equals("equals")) {
					return 2;
				}
				return 3;
			}
		});
		return (T)enhancer.create();
	}

	static class ToStringInterceptor implements MethodInterceptor {

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			return String.format("EarlyBeanReferenceProxy for bean of type %s", obj.getClass().getSuperclass().getSimpleName());
		}

	}

	static class EqualsAndHashCodeInterceptor implements MethodInterceptor {

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			throw new UnsupportedOperationException("equals() and hashCode() methods on [%s] should not be called as " +
					"doing so will cause premature instantiation of the target bean object. This may have occurred " +
					"because the proxied object was added to a collection.");
		}

	}

	static class TargetBeanDelegatingMethodInterceptor implements MethodInterceptor {

		private final BeanFactory beanFactory;

		public TargetBeanDelegatingMethodInterceptor(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			/* TODO: logging?
			System.out.printf("TargetBeanDelegatingMethodInterceptor.intercept(): attempting to retreive object of " +
					"type %s and to delegate call to method %s() made against %s\n",
					obj.getClass().getSuperclass(), method.getName(), this);
			*/
			return method.invoke(beanFactory.getBean(obj.getClass().getSuperclass()), args);
		}

	}

	static class EarlyBeanReferenceProxyMethodInterceptor implements MethodInterceptor {

		private final ConfigurableListableBeanFactory beanFactory;
		private final EarlyBeanReferenceProxyStatus earlyBeanReferenceProxyStatus;

		public EarlyBeanReferenceProxyMethodInterceptor(ConfigurableListableBeanFactory beanFactory, EarlyBeanReferenceProxyStatus earlyBeanReferenceProxyStatus) {
			this.beanFactory = beanFactory;
			this.earlyBeanReferenceProxyStatus = earlyBeanReferenceProxyStatus;
		}

		public Object intercept(Object obj, final Method beanMethod, Object[] args, MethodProxy proxy) throws Throwable {
			Assert.state(earlyBeanReferenceProxyStatus.createEarlyBeanReferenceProxies == true,
					"EarlyBeanReferenceProxyStatus must be true when intercepting a method call");
			final Class<?> returnType = beanMethod.getReturnType();
			if (!returnType.isInterface()) {
				//return new EarlyBeanReferenceProxyCreator(this.beanFactory).createProxy(beanMethod.getReturnType());
				throw new ProxyCreationException(String.format(
						"@Bean method %s.%s() is referenced from within a @Feature method, therefore " +
						"its return type must be an interface in order to allow for an early bean reference " +
						"proxy to be created. Either modify the return type accordingly, or do not reference " +
						"this bean method within @Feature methods.", beanMethod.getDeclaringClass().getSimpleName(), beanMethod.getName()));
			}
			Object proxiedBean = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {returnType,EarlyBeanReferenceProxy.class}, new InvocationHandler() {
				public Object invoke(Object proxiedBean, Method targetMethod, Object[] targetMethodArgs) throws Throwable {
					if (targetMethod.getName().equals("toString")) {
						return String.format("EarlyBeanReferenceProxy for %s object returned from @Bean method %s.%s()",
								returnType.getSimpleName(), beanMethod.getDeclaringClass().getSimpleName(), beanMethod.getName());
					}
					earlyBeanReferenceProxyStatus.createEarlyBeanReferenceProxies = false;
					Object actualBean = beanFactory.getBean(beanMethod.getName());
					earlyBeanReferenceProxyStatus.createEarlyBeanReferenceProxies = true;
					return targetMethod.invoke(actualBean, targetMethodArgs);
				}
			});
			return proxiedBean;
		}
	}
}

