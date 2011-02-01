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

import java.util.ArrayList;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.config.AbstractFeatureSpecification;
import org.springframework.context.config.FeatureSpecificationExecutor;
import org.springframework.context.config.InvalidSpecificationException;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Specifies the configuration of Spring's <em>component-scanning</em> feature.
 * May be used directly within a {@link Feature @Feature} method, or indirectly
 * through the {@link ComponentScan @ComponentScan} annotation.
 *
 * @author Chris Beams
 * @since 3.1
 * @see ComponentScan
 * @see ComponentScanAnnotationProcessor
 * @see ComponentScanBeanDefinitionParser
 * @see ComponentScanExecutor
 */
public final class ComponentScanSpec extends AbstractFeatureSpecification {

	private static final Class<? extends FeatureSpecificationExecutor> EXECUTOR_TYPE = ComponentScanExecutor.class;

	private Boolean includeAnnotationConfig = null;
	private String resourcePattern = null;
	private String[] basePackages = null;
	private Boolean useDefaultFilters = null;
	private BeanNameGenerator beanNameGenerator = null;
	private ScopeMetadataResolver scopeMetadataResolver = null;
	private ScopedProxyMode scopedProxyMode = null;
	private TypeFilter[] includeFilters = null;
	private TypeFilter[] excludeFilters = null;

	/**
	 * Package-visible constructor for use by {@link ComponentScanBeanDefinitionParser}.
	 * End users should always call String... or Class<?>... constructors to specify
	 * base packages.
	 *
	 * @see #validate()
	 */
	ComponentScanSpec() {
		super(EXECUTOR_TYPE);
	}

	public ComponentScanSpec(Class<?>... basePackageClasses) {
		this(packagesFor(basePackageClasses));
	}

	public ComponentScanSpec(String... basePackages) {
		this();
		basePackages(basePackages);
	}

	public void validate() throws InvalidSpecificationException {
		if(ObjectUtils.isEmpty(this.basePackages)) {
			throw new InvalidSpecificationException("At least one base package must be specified");
		}
	}

	public ComponentScanSpec includeAnnotationConfig(Boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
		return this;
	}

	Boolean includeAnnotationConfig() {
		return this.includeAnnotationConfig;
	}

	public ComponentScanSpec resourcePattern(String resourcePattern) {
		this.resourcePattern = resourcePattern;
		return this;
	}

	String resourcePattern() {
		return resourcePattern;
	}

	public ComponentScanSpec basePackages(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		this.basePackages = basePackages;
		return this;
	}

	void addBasePackage(String basePackage) {
		this.basePackages = (this.basePackages == null) ?
			new String[] { basePackage } :
			StringUtils.addStringToArray(this.basePackages, basePackage);
	}

	String[] basePackages() {
		return basePackages;
	}

	public ComponentScanSpec useDefaultFilters(Boolean useDefaultFilters) {
		this.useDefaultFilters = useDefaultFilters;
		return this;
	}

	Boolean useDefaultFilters() {
		return this.useDefaultFilters;
	}

	public ComponentScanSpec beanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
		return this;
	}

	BeanNameGenerator beanNameGenerator() {
		return this.beanNameGenerator;
	}

	public ComponentScanSpec scopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver = scopeMetadataResolver;
		return this;
	}

	ScopeMetadataResolver scopeMetadataResolver() {
		return this.scopeMetadataResolver;
	}

	public ComponentScanSpec scopedProxyMode(ScopedProxyMode scopedProxyMode) {
		this.scopedProxyMode = scopedProxyMode;
		return this;
	}

	ScopedProxyMode scopedProxyMode() {
		return this.scopedProxyMode;
	}

	public ComponentScanSpec includeFilters(TypeFilter... includeFilters) {
		this.includeFilters = includeFilters;
		return this;
	}

	void addIncludeFilter(TypeFilter includeFilter) {
		this.includeFilters = (this.includeFilters == null) ?
			new TypeFilter[] { includeFilter } :
			ObjectUtils.addObjectToArray(this.includeFilters, includeFilter);
	}

	TypeFilter[] includeFilters() {
		return this.includeFilters;
	}

	public ComponentScanSpec excludeFilters(TypeFilter... excludeFilters) {
		this.excludeFilters = excludeFilters;
		return this;
	}

	void addExcludeFilter(TypeFilter excludeFilter) {
		this.excludeFilters = (this.excludeFilters == null) ?
			new TypeFilter[] { excludeFilter } :
			ObjectUtils.addObjectToArray(this.excludeFilters, excludeFilter);
	}

	TypeFilter[] excludeFilters() {
		return this.excludeFilters;
	}

	static ComponentScanSpec withBasePackages(String... basePackages) {
		return new ComponentScanSpec(basePackages);
	}

	private static String[] packagesFor(Class<?>[] classes) {
		ArrayList<String> packages = new ArrayList<String>();
		for (Class<?> clazz : classes) {
			packages.add(clazz.getPackage().getName());
		}
		return packages.toArray(new String[packages.size()]);
	}

}
