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

package org.springframework.scheduling.annotation;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

public class ScheduledConfigurationClassTests {

	@Test
	public void withfixedRateTask() throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(FixedRateTaskConfig.class);
		ctx.refresh();

		Thread.sleep(100);
		assertThat(ctx.getBean(AtomicInteger.class).get(), greaterThanOrEqualTo(10));
		ctx.close();
	}


	@Configuration
	static class FixedRateTaskConfig {

		@Bean
		public AtomicInteger counter() {
			return new AtomicInteger();
		}

		@Scheduled(fixedRate=10)
		public void task() {
			counter().incrementAndGet();
		}
	}


	@Test
	public void withExplicitScheduler() throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ExplicitSchedulerConfig.class);
		ctx.refresh();

		Thread.sleep(100);
		assertThat(ctx.getBean(AtomicInteger.class).get(), greaterThanOrEqualTo(10));
		assertThat(ctx.getBean(ExplicitSchedulerConfig.class).threadName, startsWith("explicitScheduler-"));
		ctx.close();
	}


	@Configuration
	static class ExplicitSchedulerConfig {

		String threadName;

		@Bean
		public TaskScheduler taskScheduler() {
			ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
			scheduler.setThreadNamePrefix("explicitScheduler-");
			return scheduler;
		}

		@Bean
		public AtomicInteger counter() {
			return new AtomicInteger();
		}

		@Scheduled(fixedRate=10)
		public void task() {
			threadName = Thread.currentThread().getName();
			counter().incrementAndGet();
		}
	}


	@Test(expected=IllegalStateException.class)
	public void withExplicitSchedulerAmbiguity() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(AmbiguousExplicitSchedulerConfig.class);
		try {
			ctx.refresh();
		} catch (IllegalStateException ex) {
			assertThat(ex.getMessage(), startsWith("More than one TaskScheduler"));
			throw ex;
		}
	}


	@Configuration
	static class AmbiguousExplicitSchedulerConfig {

		String threadName;

		@Bean
		public TaskScheduler taskScheduler1() {
			ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
			scheduler.setThreadNamePrefix("explicitScheduler1");
			return scheduler;
		}

		@Bean
		public TaskScheduler taskScheduler2() {
			ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
			scheduler.setThreadNamePrefix("explicitScheduler2");
			return scheduler;
		}

		@Bean
		public AtomicInteger counter() {
			return new AtomicInteger();
		}

		@Scheduled(fixedRate=10)
		public void task() {
			threadName = Thread.currentThread().getName();
			counter().incrementAndGet();
		}
	}


	@Test
	public void withExplicitScheduledTaskRegistrar() throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ExplicitScheduledTaskRegistrarConfig.class);
		ctx.refresh();

		Thread.sleep(100);
		assertThat(ctx.getBean(AtomicInteger.class).get(), greaterThanOrEqualTo(10));
		assertThat(ctx.getBean(ExplicitScheduledTaskRegistrarConfig.class).threadName, startsWith("explicitScheduler1"));
		ctx.close();
	}


	@Configuration
	static class ExplicitScheduledTaskRegistrarConfig {

		String threadName;

		@Bean
		public ScheduledTaskRegistrar taskRegistrar() {
			ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
			taskRegistrar.setScheduler(taskScheduler1());
			return taskRegistrar;
		}

		@Bean
		public TaskScheduler taskScheduler1() {
			ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
			scheduler.setThreadNamePrefix("explicitScheduler1");
			return scheduler;
		}

		@Bean
		public TaskScheduler taskScheduler2() {
			ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
			scheduler.setThreadNamePrefix("explicitScheduler2");
			return scheduler;
		}

		@Bean
		public AtomicInteger counter() {
			return new AtomicInteger();
		}

		@Scheduled(fixedRate=10)
		public void task() {
			threadName = Thread.currentThread().getName();
			counter().incrementAndGet();
		}
	}


	@Test(expected=IllegalStateException.class)
	public void withAmbiguousTaskSchedulersButNoActualTasks() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(MultipleSchedulerBeansWithoutScheduledTasks.class);
		ctx.refresh();
	}

	@Test
	@Ignore // there is currently no way to customize the set of annotation post processors used
	public void SABPPMayBeDisabledToAvoidAmbiguousTaskSchedulersException() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(MultipleSchedulerBeansWithoutScheduledTasks.class);
		ctx.refresh();
		ctx.getBean("explicitSchedler1");
		ctx.getBean("explicitSchedler2");
	}


	@Configuration
	static class MultipleSchedulerBeansWithoutScheduledTasks {

		@Bean
		public TaskScheduler taskScheduler1() {
			ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
			scheduler.setThreadNamePrefix("explicitScheduler1");
			return scheduler;
		}

		@Bean
		public TaskScheduler taskScheduler2() {
			ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
			scheduler.setThreadNamePrefix("explicitScheduler2");
			return scheduler;
		}
	}

}
