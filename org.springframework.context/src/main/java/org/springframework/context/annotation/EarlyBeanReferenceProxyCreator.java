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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

class EarlyBeanReferenceProxyCreator {

	static final String FINAL_CLASS_ERROR_MESSAGE =
		"Cannot create subclass proxy for bean type %s because it is a final class. " +
		"Make the class non-final or inject the bean by interface rather than by concrete class.";

	static final String MISSING_NO_ARG_CONSTRUCTOR_ERROR_MESSAGE =
		"Cannot create subclass proxy for bean type %s because it does not have a no-arg constructor. " +
		"Add a no-arg constructor or attempt to inject the bean by interface rather than by concrete class.";

	static final String PRIVATE_NO_ARG_CONSTRUCTOR_ERROR_MESSAGE =
		"Cannot create subclass proxy for bean type %s because its no-arg constructor is private. " +
		"Increase the visibility of the no-arg constructor or attempt to inject the bean by interface rather " +
		"than by concrete class.";


	private final AutowireCapableBeanFactory beanFactory;


	public EarlyBeanReferenceProxyCreator(AutowireCapableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public Object createProxy(DependencyDescriptor descriptor) {
		TargetBeanDereferencingInterceptor interceptor =
			new ResolveDependencyTargetBeanDereferencingInterceptor(descriptor, this.beanFactory);

		return doCreateProxy(interceptor);
	}

	private Object doCreateProxy(TargetBeanDereferencingInterceptor targetBeanDereferencingInterceptor) {
		Enhancer enhancer = new Enhancer();
		Class<?> targetBeanType = targetBeanDereferencingInterceptor.getTargetBeanType();
		if (targetBeanType.isInterface()) {
			enhancer.setSuperclass(Object.class);
			enhancer.setInterfaces(new Class<?>[] {targetBeanType, EarlyBeanReferenceProxy.class});
		} else {
			assertClassIsProxyCapable(targetBeanType);
			enhancer.setSuperclass(targetBeanType);
			enhancer.setInterfaces(new Class<?>[] {EarlyBeanReferenceProxy.class});
		}
		enhancer.setCallbacks(new Callback[] {
			new BeanMethodInterceptor(this, this.beanFactory),
			new ObjectMethodsInterceptor(),
			targetBeanDereferencingInterceptor,
			new TargetBeanDelegatingMethodInterceptor()
		});
		enhancer.setCallbackFilter(new CallbackFilter() {
			public int accept(Method method) {
				if (AnnotationUtils.findAnnotation(method, Bean.class) != null) {
					return 0;
				}
				if (ReflectionUtils.isObjectMethod(method)) {
					return 1;
				}
				if (method.getName().equals("dereferenceTargetBean")) {
					return 2;
				}
				return 3;
			}
		});
		return enhancer.create();
	}

	private static void assertClassIsProxyCapable(Class<?> beanType) {
		if ((beanType.getModifiers() & Modifier.FINAL) != 0) {
			throw new ProxyCreationException(String.format(FINAL_CLASS_ERROR_MESSAGE, beanType.getName()));
		}
		try {
			// attempt to retrieve the no-arg constructor for the class
			Constructor<?> noArgCtor = beanType.getDeclaredConstructor();
			if ((noArgCtor.getModifiers() & Modifier.PRIVATE) != 0) {
				throw new ProxyCreationException(String.format(PRIVATE_NO_ARG_CONSTRUCTOR_ERROR_MESSAGE, beanType.getName()));
			}
		} catch (NoSuchMethodException ex) {
			throw new ProxyCreationException(String.format(MISSING_NO_ARG_CONSTRUCTOR_ERROR_MESSAGE, beanType.getName()));
		}
	}


	/**
	 * Interceptor for methods declared by java.lang.Object()
	 */
	private static class ObjectMethodsInterceptor implements MethodInterceptor {

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			if (method.getName().equals("toString")) {
				return String.format("EarlyBeanReferenceProxy for bean of type %s", obj.getClass().getSuperclass().getSimpleName());
			}
			if (method.getName().equals("hashCode") || method.getName().equals("equals")) {
				throw new UnsupportedOperationException("equals() and hashCode() methods on [%s] should not be called as " +
						"doing so will cause premature instantiation of the target bean object. This may have occurred " +
						"because the proxied object was added to a collection.");
			}
			return method.invoke(obj, args);
		}

	}

	/**
	 * Interceptor that dereferences the target bean for the proxy by calling
	 * {@link AutowireCapableBeanFactory#resolveDependency(DependencyDescriptor, String)}.
	 * @see EarlyBeanReferenceProxy#dereferenceTargetBean()
	 */
	private static class ResolveDependencyTargetBeanDereferencingInterceptor implements TargetBeanDereferencingInterceptor {
		private final DependencyDescriptor descriptor;
		private final AutowireCapableBeanFactory beanFactory;

		public ResolveDependencyTargetBeanDereferencingInterceptor(DependencyDescriptor descriptor, AutowireCapableBeanFactory beanFactory) {
			this.descriptor = descriptor;
			this.beanFactory = beanFactory;
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			return beanFactory.resolveDependency(descriptor, null); // TODO: is 'null' for beanName appropriate?
		}

		public Class<?> getTargetBeanType() {
			return this.descriptor.getDependencyType();
		}

	}


	/**
	 * Interceptor that dereferences the target bean for the proxy by calling {@link BeanFactory#getBean(String)}.
	 * @see EarlyBeanReferenceProxy#dereferenceTargetBean()
	 */
	private static class ByNameLookupTargetBeanInterceptor implements TargetBeanDereferencingInterceptor {
		private final Method beanMethod;
		private final BeanFactory beanFactory;

		public ByNameLookupTargetBeanInterceptor(Method beanMethod, BeanFactory beanFactory) {
			this.beanMethod = beanMethod;
			this.beanFactory = beanFactory;
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			return beanFactory.getBean(beanMethod.getName()); // TODO: deal with aliases / alternate bean name
		}

		public Class<?> getTargetBeanType() {
			return beanMethod.getReturnType();
		}

	}


	/**
	 * Interceptor that dereferences the target bean for the proxy and delegates the
	 * current method call to it.
	 * @see TargetBeanDereferencingInterceptor
	 */
	private static class TargetBeanDelegatingMethodInterceptor implements MethodInterceptor {

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			/* TODO: logging?
			System.out.printf("TargetBeanDelegatingMethodInterceptor.intercept(): attempting to retreive object of " +
					"type %s and to delegate call to method %s() made against %s\n",
					obj.getClass().getSuperclass(), method.getName(), this);
			*/
			Object targetBean = ((EarlyBeanReferenceProxy)obj).dereferenceTargetBean();
			return method.invoke(targetBean, args);
		}

	}


	private static class BeanMethodInterceptor implements MethodInterceptor {

		private final EarlyBeanReferenceProxyCreator proxyCreator;
		private final BeanFactory beanFactory;

		public BeanMethodInterceptor(EarlyBeanReferenceProxyCreator proxyCreator, BeanFactory beanFactory) {
			this.proxyCreator = proxyCreator;
			this.beanFactory = beanFactory;
		}

		public Object intercept(Object obj, final Method beanMethod, Object[] args, MethodProxy proxy) throws Throwable {
			TargetBeanDereferencingInterceptor interceptor =
				new ByNameLookupTargetBeanInterceptor(beanMethod, this.beanFactory);
			return proxyCreator.doCreateProxy(interceptor);
		}
	}


	private static interface TargetBeanDereferencingInterceptor extends MethodInterceptor {

		Class<?> getTargetBeanType();

	}

}

