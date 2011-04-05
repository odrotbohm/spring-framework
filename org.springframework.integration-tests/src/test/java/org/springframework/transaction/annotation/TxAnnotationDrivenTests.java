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

package org.springframework.transaction.annotation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.config.AdviceMode;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.config.AnnotationDrivenTx;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;

/**
 * Integration tests for @TxAnnotationDriven, designed for use
 * with @Configuration classes.
 *
 * @author Chris Beams
 * @since 3.1
 */
public class TxAnnotationDrivenTests {

	@Test
	public void repositoryIsNotTxProxy() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class);
		ctx.refresh();

		try {
			assertTxProxying(ctx);
			fail("expected exception");
		} catch (AssertionError ex) {
			assertThat(ex.getMessage(), equalTo("FooRepository is not a TX proxy"));
		}
	}

	@Test
	public void repositoryIsTxProxy_withDefaultTxManagerName() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class, DefaultTxManagerNameConfig.class);
		ctx.refresh();

		assertTxProxying(ctx);
	}

	@Test
	public void repositoryIsTxProxy_withCustomTxManagerName() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class, CustomTxManagerNameConfig.class);
		ctx.refresh();

		assertTxProxying(ctx);
	}

	@Test(expected=NoSuchBeanDefinitionException.class)
	public void repositoryIsTxProxy_withBogusTxManagerName() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class, BogusTxManagerNameConfig.class);
		ctx.refresh();

		assertTxProxying(ctx);
	}

	@Ignore @Test // TODO SPR-8207
	public void repositoryIsTxProxy_withNonConventionalTxManagerName_fallsBackToByTypeLookup() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class, NonConventionalTxManagerNameConfig.class);
		ctx.refresh();

		assertTxProxying(ctx);
	}

	@Test
	public void repositoryIsClassBasedTxProxy() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class, ProxyTargetClassTxConfig.class);
		ctx.refresh();

		assertTxProxying(ctx);
		assertThat(AopUtils.isCglibProxy(ctx.getBean(FooRepository.class)), is(true));
	}

	@Test
	public void repositoryUsesAspectJAdviceMode() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class, AspectJTxConfig.class);
		try {
			ctx.refresh();
		} catch (Exception ex) {
			// this test is a bit fragile, but gets the job done, proving that an
			// attempt was made to look up the AJ aspect. It's due to classpath issues
			// in .integration-tests that it's not found.
			assertThat(ex.getMessage().endsWith("org.springframework.transaction.aspectj.AnnotationTransactionAspect"), is(true));
		}
	}

	private void assertTxProxying(AnnotationConfigApplicationContext ctx) {
		FooRepository repo = ctx.getBean(FooRepository.class);

		boolean isTxProxy = false;
		if (AopUtils.isAopProxy(repo)) {
			for (Advisor advisor : ((Advised)repo).getAdvisors()) {
				if (advisor instanceof BeanFactoryTransactionAttributeSourceAdvisor) {
					isTxProxy = true;
					break;
				}
			}
		}
		assertTrue("FooRepository is not a TX proxy", isTxProxy);

		// trigger a transaction
		repo.findAll();
	}


	@Configuration
	@AnnotationDrivenTx
	static class DefaultTxManagerNameConfig {
		@Bean
		PlatformTransactionManager transactionManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}
	}


	@Configuration
	@AnnotationDrivenTx(transactionManager="txManager")
	static class CustomTxManagerNameConfig {
		@Bean
		PlatformTransactionManager txManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}
	}


	@Configuration
	@AnnotationDrivenTx(transactionManager="typoManager")
	static class BogusTxManagerNameConfig {
		@Bean
		PlatformTransactionManager txManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}
	}


	@Configuration
	@AnnotationDrivenTx
	static class NonConventionalTxManagerNameConfig {
		@Bean
		PlatformTransactionManager txManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}
	}


	@Configuration
	@AnnotationDrivenTx(proxyTargetClass=true)
	static class ProxyTargetClassTxConfig {
		@Bean
		PlatformTransactionManager transactionManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}
	}


	@Configuration
	@AnnotationDrivenTx(mode=AdviceMode.ASPECTJ)
	static class AspectJTxConfig {
		@Bean
		PlatformTransactionManager transactionManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}
	}


	@Configuration
	static class Config {
		@Bean
		FooRepository fooRepository() {
			JdbcFooRepository repos = new JdbcFooRepository();
			repos.setDataSource(dataSource());
			return repos;
		}

		@Bean
		DataSource dataSource() {
			return new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.HSQL)
				.build();
		}
	}


	interface FooRepository {
		List<Object> findAll();
	}


	@Repository
	static class JdbcFooRepository implements FooRepository {

		public void setDataSource(DataSource dataSource) {
		}

		@Transactional
		public List<Object> findAll() {
			return Collections.emptyList();
		}
	}
}
