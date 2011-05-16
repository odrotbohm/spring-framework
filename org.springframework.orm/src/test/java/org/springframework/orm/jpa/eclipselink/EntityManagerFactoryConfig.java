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

package org.springframework.orm.jpa.eclipselink;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.orm.jpa.JpaExceptionTranslator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBuilder;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;

@Configuration
public class EntityManagerFactoryConfig implements BeanClassLoaderAware, ResourceLoaderAware, LoadTimeWeaverAware {

	@Autowired
	DataSource dataSource;
	private ResourceLoader resourceLoader;
	private LoadTimeWeaver loadTimeWeaver;
	private ClassLoader beanClassLoader;

	/*
	@Bean(name="entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean fromFactory() {
		LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
		fb.setPersistenceXmlLocation("org/springframework/orm/jpa/domain/persistence.xml");
		fb.setDataSource(dataSource);
		fb.setJpaVendorAdapter(
			new EclipseLinkJpaVendorAdapter()
				.setDatabase(Database.HSQL)
				.setShowSql(true)
				.setGenerateDdl(true));
		return fb;
	}
	*/

	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	@Bean(name="entityManagerFactory")
	public EntityManagerFactory fromBuilder() {
		LocalContainerEntityManagerFactoryBuilder builder = new LocalContainerEntityManagerFactoryBuilder();
		builder.setPersistenceXmlLocation("org/springframework/orm/jpa/domain/persistence.xml");
		builder.setDataSource(dataSource);
		if (loadTimeWeaver != null) {
			builder.setLoadTimeWeaver(loadTimeWeaver);
		}
		builder.setBeanClassLoader(beanClassLoader);
		builder.setResourceLoader(resourceLoader);
		builder.setJpaVendorAdapter(
			new EclipseLinkJpaVendorAdapter()
				.setDatabase(Database.HSQL)
				.setShowSql(true)
				.setGenerateDdl(true)
		);
		return builder.buildEntityManagerFactory();
	}

	@Bean
	public PersistenceExceptionTranslator pet() {
		return new JpaExceptionTranslator();
	}
}
