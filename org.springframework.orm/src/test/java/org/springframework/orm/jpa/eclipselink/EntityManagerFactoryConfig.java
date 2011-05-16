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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.orm.jpa.EntityManagerFactoryBuilderContext;
import org.springframework.orm.jpa.JpaExceptionTranslator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBuilder;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;

@Configuration
public class EntityManagerFactoryConfig {

	@Autowired
	DataSource dataSource;

	@Bean
	public EntityManagerFactory entityManagerFactory() {
		return new LocalContainerEntityManagerFactoryBuilder(emfBuilderContext())
			.setPersistenceXmlLocation("org/springframework/orm/jpa/domain/persistence.xml")
			.setDataSource(dataSource)
			.setJpaVendorAdapter(
				new EclipseLinkJpaVendorAdapter()
					.setDatabase(Database.HSQL)
					.setShowSql(true)
					.setGenerateDdl(true)
			).buildEntityManagerFactory();
	}

	@Bean
	public EntityManagerFactoryBuilderContext emfBuilderContext() {
		return new EntityManagerFactoryBuilderContext();
	}

	@Bean
	public PersistenceExceptionTranslator exceptionTranslator() {
		return new JpaExceptionTranslator();
	}
}
