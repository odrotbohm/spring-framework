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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

class EarlyBeanReferenceProxyCreator {

	private final ConfigurableListableBeanFactory beanFactory;
	private final EarlyBeanReferenceProxyStatus earlyBeanReferenceProxyStatus;

	public EarlyBeanReferenceProxyCreator(ConfigurableListableBeanFactory beanFactory, EarlyBeanReferenceProxyStatus earlyBeanReferenceProxyStatus) {
		this.beanFactory = beanFactory;
		this.earlyBeanReferenceProxyStatus = earlyBeanReferenceProxyStatus;
	}

	public Object createProxy(Class<?> paramType) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(paramType);
		enhancer.setInterfaces(new Class<?>[] {EarlyBeanReferenceProxy.class});
		enhancer.setCallbacks(new Callback[] {
			new EarlyBeanReferenceProxyMethodInterceptor(this.beanFactory, this.earlyBeanReferenceProxyStatus),
			new UnsupportedOperationInterceptor()
		});
		enhancer.setCallbackFilter(new CallbackFilter() {
			public int accept(Method method) {
				return (AnnotationUtils.findAnnotation(method, Bean.class) != null ? 0 : 1);
			}
		});
		return enhancer.create();
	}

}

class UnsupportedOperationInterceptor implements MethodInterceptor {

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		throw new UnsupportedOperationException();
	}
	
}

class EarlyBeanReferenceProxyMethodInterceptor implements MethodInterceptor {
	
	private final ConfigurableListableBeanFactory beanFactory;
	private final EarlyBeanReferenceProxyStatus earlyBeanReferenceProxyStatus;

	public EarlyBeanReferenceProxyMethodInterceptor(ConfigurableListableBeanFactory beanFactory, EarlyBeanReferenceProxyStatus earlyBeanReferenceProxyStatus) {
		this.beanFactory = beanFactory;
		this.earlyBeanReferenceProxyStatus = earlyBeanReferenceProxyStatus;
	}

	public Object intercept(Object obj, final Method beanMethod, Object[] args, MethodProxy proxy) throws Throwable {
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