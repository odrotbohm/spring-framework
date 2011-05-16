/*
 * Copyright 2002-2009 the original author or authors.
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

import java.lang.reflect.InvocationHandler;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.jdbc.datasource.lookup.SingleDataSourceLookup;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.persistenceunit.SmartPersistenceUnitInfo;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.beans.factory.FactoryBean} that creates a JPA
 * {@link javax.persistence.EntityManagerFactory} according to JPA's standard
 * <i>container</i> bootstrap contract. This is the most powerful way to set
 * up a shared JPA EntityManagerFactory in a Spring application context;
 * the EntityManagerFactory can then be passed to JPA-based DAOs via
 * dependency injection. Note that switching to a JNDI lookup or to a
 * {@link org.springframework.orm.jpa.LocalEntityManagerFactoryBean}
 * definition is just a matter of configuration!
 *
 * <p>As with {@link LocalEntityManagerFactoryBean}, configuration settings
 * are usually read in from a <code>META-INF/persistence.xml</code> config file,
 * residing in the class path, according to the general JPA configuration contract.
 * However, this FactoryBean is more flexible in that you can override the location
 * of the <code>persistence.xml</code> file, specify the JDBC DataSources to link to,
 * etc. Furthermore, it allows for pluggable class instrumentation through Spring's
 * {@link org.springframework.instrument.classloading.LoadTimeWeaver} abstraction,
 * instead of being tied to a special VM agent specified on JVM startup.
 *
 * <p>Internally, this FactoryBean parses the <code>persistence.xml</code> file
 * itself and creates a corresponding {@link javax.persistence.spi.PersistenceUnitInfo}
 * object (with further configuration merged in, such as JDBC DataSources and the
 * Spring LoadTimeWeaver), to be passed to the chosen JPA
 * {@link javax.persistence.spi.PersistenceProvider}. This corresponds to a
 * local JPA container with full support for the standard JPA container contract.
 *
 * <p>The exposed EntityManagerFactory object will implement all the interfaces of
 * the underlying native EntityManagerFactory returned by the PersistenceProvider,
 * plus the {@link EntityManagerFactoryInfo} interface which exposes additional
 * metadata as assembled by this FactoryBean.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 * @see #setPersistenceXmlLocation
 * @see #setJpaProperties
 * @see #setJpaVendorAdapter
 * @see #setLoadTimeWeaver
 * @see #setDataSource
 * @see EntityManagerFactoryInfo
 * @see LocalEntityManagerFactoryBean
 * @see org.springframework.orm.jpa.support.SharedEntityManagerBean
 * @see javax.persistence.spi.PersistenceProvider#createContainerEntityManagerFactory
 */
public class LocalContainerEntityManagerFactoryBean extends AbstractLocalContainerEntityManagerFactoryCreator
		implements EntityManagerFactoryBeanOperations, ResourceLoaderAware, LoadTimeWeaverAware {

	@Override
	protected InvocationHandler getEMFProxyInvocationHandler() {
		return new ManagedEntityManagerFactoryInvocationHandler(this);
	}

	/**
	 * Return the singleton EntityManagerFactory.
	 */
	public EntityManagerFactory getObject() {
		return this.entityManagerFactory;
	}

	public Class<? extends EntityManagerFactory> getObjectType() {
		return (this.entityManagerFactory != null ? this.entityManagerFactory.getClass() : EntityManagerFactory.class);
	}

	public boolean isSingleton() {
		return true;
	}

	private EntityManagerFactory entityManagerFactory;


	/**
	 * Close the EntityManagerFactory on bean factory shutdown.
	 */
	public void destroy() {
		if (logger.isInfoEnabled()) {
			logger.info("Closing JPA EntityManagerFactory for persistence unit '" + getPersistenceUnitName() + "'");
		}
		this.entityManagerFactory.close();
	}

	public final void afterPropertiesSet() throws PersistenceException {
		initialize();
		determineEMFInterfaces(this.nativeEntityManagerFactory);

		// Wrap the EntityManagerFactory in a factory implementing all its interfaces.
		// This allows interception of createEntityManager methods to return an
		// application-managed EntityManager proxy that automatically joins
		// existing transactions.
		this.entityManagerFactory = createEntityManagerFactoryProxy(this.nativeEntityManagerFactory);
	}

}
