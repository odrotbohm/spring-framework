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

import javax.persistence.PersistenceException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;

/**
 * {@link PersistenceExceptionTranslator} capable of translating
 * JPA {@link PersistenceException} instances to Spring's
 * {@link DataAccessException} hierarchy.
 *
 * <p>When configuring the Spring container via XML, note that this translator is
 * automatically used internally by {@code *EntityManagerFactoryBean} types. When
 * configuring the container with {@code @Configuration} classes, a {@code @Bean}
 * of this type must be registered manually.
 *
 * @author Chris Beams
 * @since 3.1
 * @see EntityManagerFactoryBuilder
 * @see org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
 */
public class JpaExceptionTranslator implements PersistenceExceptionTranslator {

	private final JpaDialect jpaDialect;

	public JpaExceptionTranslator() {
		this(null);
	}

	public JpaExceptionTranslator(JpaDialect jpaDialect) {
		this.jpaDialect = jpaDialect;
	}

	/**
	 * Implementation of the PersistenceExceptionTranslator interface, as
	 * autodetected by Spring's PersistenceExceptionTranslationPostProcessor.
	 * <p>Uses the dialect's conversion if possible; otherwise falls back to
	 * standard JPA exception conversion.
	 * @see org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
	 * @see JpaDialect#translateExceptionIfPossible
	 * @see EntityManagerFactoryUtils#convertJpaAccessExceptionIfPossible
	 */
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		return (this.jpaDialect != null ? this.jpaDialect.translateExceptionIfPossible(ex) :
				EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible(ex));
	}

}
