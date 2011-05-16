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

package org.springframework.orm.jpa;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.util.Assert;

public class LocalContainerEntityManagerFactoryBuilder extends
		AbstractLocalContainerEntityManagerFactoryCreator<LocalContainerEntityManagerFactoryBuilder> {

	public LocalContainerEntityManagerFactoryBuilder(EntityManagerFactoryBuilderContext ctx) {
		this(ctx.getBeanClassLoader(), ctx.getResourceLoader(), ctx.getLoadTimeWeaver());
	}

	public LocalContainerEntityManagerFactoryBuilder(ClassLoader beanClassLoader, ResourceLoader resourceLoader) {
		this(beanClassLoader, resourceLoader, null);
	}

	public LocalContainerEntityManagerFactoryBuilder(ClassLoader beanClassLoader, ResourceLoader resourceLoader, LoadTimeWeaver loadTimeWeaver) {
		Assert.notNull(beanClassLoader, "bean ClassLoader must not be null");
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");

		this.setBeanClassLoader(beanClassLoader);
		this.setResourceLoader(resourceLoader);
		this.setLoadTimeWeaver(loadTimeWeaver);
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected EntityManagerFactory createEntityManagerFactoryProxy(EntityManagerFactory emf) {
		FullerManagedEntityManagerFactoryInvocationHandler handler = getEMFProxyInvocationHandler();

		final Set<Class> ifcsAll = new HashSet<Class>();
		ifcsAll.addAll(emfInterfaces);
		ifcsAll.addAll(handler.getInterfaces());

		return (EntityManagerFactory) Proxy.newProxyInstance(
				getBeanClassLoader(),
				ifcsAll.toArray(new Class[ifcsAll.size()]),
				handler);
	}

	public EntityManagerFactory buildEntityManagerFactory() {
		initialize();
		determineEMFInterfaces(this.nativeEntityManagerFactory);
		return createEntityManagerFactoryProxy(this.nativeEntityManagerFactory);
	}

	@SuppressWarnings("rawtypes")
	private static class FullerManagedEntityManagerFactoryInvocationHandler extends ManagedEntityManagerFactoryInvocationHandler {
		private final Set<Class> ifcsBuilder = new HashSet<Class>();

		public FullerManagedEntityManagerFactoryInvocationHandler(AbstractEntityManagerFactoryCreator emfb) {
			super(emfb);
			ifcsBuilder.add(EntityManagerFactoryInfo.class);
			//ifcsBuilder.add(ResourceLoaderAware.class);
			//ifcsBuilder.add(LoadTimeWeaverAware.class);
			//ifcsBuilder.add(BeanClassLoaderAware.class);
			ifcsBuilder.add(BeanFactoryAware.class);
			ifcsBuilder.add(BeanNameAware.class);
			//ifcsBuilder.add(InitializingBean.class);
			ifcsBuilder.add(DisposableBean.class);
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			for (Class<?> ifc : ifcsBuilder) {
				if (method.getName().equals("getEMFCreator")) {
					return this.entityManagerFactoryBean;
				}
				if (ifc.isAssignableFrom(method.getDeclaringClass())) {
					return method.invoke(this.entityManagerFactoryBean, args);
				}
			}
			return super.invoke(proxy, method, args);
		}

		public Set<Class> getInterfaces() {
			return ifcsBuilder;
		}
	}

	@Override
	protected FullerManagedEntityManagerFactoryInvocationHandler getEMFProxyInvocationHandler() {
		return new FullerManagedEntityManagerFactoryInvocationHandler(this);
	}
}
