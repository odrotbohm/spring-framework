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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.weaving.LoadTimeWeaverAware;

public class LocalContainerEntityManagerFactoryBuilder extends
		LocalContainerEntityManagerFactoryBean {

//	@Override
//	protected void determineEMFInterfaces(EntityManagerFactory emf) {
//		super.determineEMFInterfaces(emf);
//		final Set<Class<?>> ifcsEmf = new HashSet<Class<?>>();
//		ifcsEmf.add(EntityManagerFactory.class);
//		ifcsEmf.add(EntityManagerFactoryInfo.class);
//		ifcsEmf.add(Serializable.class);
//
//		emfInterfaces.addAll(ifcsEmf);
//		emfInterfaces.addAll(ifcsBuilder);
//
//	}
	@Override
	@SuppressWarnings("rawtypes")
	protected EntityManagerFactory createEntityManagerFactoryProxy(EntityManagerFactory emf) {
		final Set<Class> ifcsBuilder = new HashSet<Class>();
		ifcsBuilder.add(EntityManagerFactoryInfo.class);
		//ifcsBuilder.add(ResourceLoaderAware.class);
		//ifcsBuilder.add(LoadTimeWeaverAware.class);
		//ifcsBuilder.add(BeanClassLoaderAware.class);
		ifcsBuilder.add(BeanFactoryAware.class);
		ifcsBuilder.add(BeanNameAware.class);
		//ifcsBuilder.add(InitializingBean.class);
		ifcsBuilder.add(DisposableBean.class);

		final Set<Class> ifcsAll = new HashSet<Class>();
		ifcsAll.addAll(emfInterfaces);
		ifcsAll.addAll(ifcsBuilder);

		return (EntityManagerFactory) Proxy.newProxyInstance(
				getBeanClassLoader(),
				ifcsAll.toArray(new Class[ifcsAll.size()]),
				new FullerManagedEntityManagerFactoryInvocationHandler(this, ifcsBuilder.toArray(new Class[ifcsBuilder.size()])));
	}

	public EntityManagerFactory buildEntityManagerFactory() {
		initialize();
		determineEMFInterfaces(this.nativeEntityManagerFactory);
		return createEntityManagerFactoryProxy(this.nativeEntityManagerFactory);
	}

	@SuppressWarnings("rawtypes")
	private static class FullerManagedEntityManagerFactoryInvocationHandler extends ManagedEntityManagerFactoryInvocationHandler {
		private final Class[] ifcsBuilder;

		public FullerManagedEntityManagerFactoryInvocationHandler(AbstractEntityManagerFactoryBean emfb, Class[] ifcsBuilder) {
			super(emfb);
			this.ifcsBuilder = ifcsBuilder;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			for (Class<?> ifc : ifcsBuilder) {
				if (method.getName().equals("getEMFCreator")) {
					return this.entityManagerFactoryBean;
				}
				if (ifc.isAssignableFrom(method.getDeclaringClass())) {
					System.err.println(method);
					return method.invoke(this.entityManagerFactoryBean, args);
				}
			}
			return super.invoke(proxy, method, args);
		}
	}
}
