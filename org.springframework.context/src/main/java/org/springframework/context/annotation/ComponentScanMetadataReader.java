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

import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.metadata.MetadataDefinitionReader;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.TypeFilter;


/**
 * TODO SPR-7194: document
 * 
 * @author Chris Beams
 * @since 3.1
 */
public class ComponentScanMetadataReader implements MetadataDefinitionReader<ComponentScanMetadata> {

	private final BeanDefinitionRegistry registry;
	private final ResourceLoader resourceLoader;
	private final Environment environment;
	private BeanDefinitionDefaults beanDefinitionDefaults;
	private String[] autowireCandidatePatterns;

	public ComponentScanMetadataReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader, Environment environment) {
		this.registry = registry;
		this.resourceLoader = resourceLoader;
		this.environment = environment;
	}

	public void setBeanDefinitionDefaults(BeanDefinitionDefaults beanDefinitionDefaults) {
		this.beanDefinitionDefaults = beanDefinitionDefaults;
	}

	public BeanDefinitionDefaults getBeanDefinitionDefaults() {
		return beanDefinitionDefaults;
	}

	public void setAutowireCandidatePatterns(String[] autowireCandidatePatterns) {
		this.autowireCandidatePatterns = autowireCandidatePatterns;
	}

	public String[] getAutowireCandidatePatterns() {
		return autowireCandidatePatterns;
	}

	/**
	 * Configure a {@link ClassPathBeanDefinitionScanner} based on the content of
	 * the given metadata and perform actual scanning and bean definition registration.
	 * @return the set of bean definitions scanned and registered (never {@code null})
	 */
	public Set<BeanDefinitionHolder> read(ComponentScanMetadata metadata) {
		ClassPathBeanDefinitionScanner scanner = metadata.getUseDefaultFilters() == null ?
			new ClassPathBeanDefinitionScanner(this.registry) :
			new ClassPathBeanDefinitionScanner(this.registry, metadata.getUseDefaultFilters());

		scanner.setResourceLoader(this.resourceLoader);
		scanner.setEnvironment(this.environment);

		if (this.beanDefinitionDefaults != null) {
			scanner.setBeanDefinitionDefaults(this.beanDefinitionDefaults);
		}
		if (this.autowireCandidatePatterns != null) {
			scanner.setAutowireCandidatePatterns(this.autowireCandidatePatterns);
		}

		if (metadata.getResourcePattern() != null) {
			scanner.setResourcePattern(metadata.getResourcePattern());
		}
		if (metadata.getBeanNameGenerator() != null) {
			scanner.setBeanNameGenerator(metadata.getBeanNameGenerator());
		}
		if (metadata.getIncludeAnnotationConfig() != null) {
			scanner.setIncludeAnnotationConfig(metadata.getIncludeAnnotationConfig());
		}
		if (metadata.getScopeMetadataResolver() != null) {
			scanner.setScopeMetadataResolver(metadata.getScopeMetadataResolver());
		}
		if (metadata.getScopedProxyMode() != null) {
			scanner.setScopedProxyMode(metadata.getScopedProxyMode());
		}
		if (metadata.getIncludeFilters() != null) {
			for (TypeFilter filter : metadata.getIncludeFilters()) {
				scanner.addIncludeFilter(filter);
			}
		}
		if (metadata.getExcludeFilters() != null) {
			for (TypeFilter filter : metadata.getExcludeFilters()) {
				scanner.addExcludeFilter(filter);
			}
		}

		return scanner.doScan(metadata.getBasePackages());
	}
}