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

import java.util.Map;

import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link AnnotationMetadataParser} implementation responsible for parsing metadata
 * from {@link ComponentScan} annotations into the more general form of
 * {@link ComponentScanMetadata} which can in turn be consumed by a
 * {@link ComponentScanMetadataReader} for actual parsing and bean registration.
 * {@link ComponentScanBeanDefinitionParser} provides serves as the XML counterpart
 * to this component.
 *
 * @author Chris Beams
 * @since 3.1
 * @see ComponentScan
 * @see ComponentScanMetadataReader
 * @see ComponentScanBeanDefinitionParser
 */
public class ComponentScanAnnotationMetadataParser implements AnnotationMetadataParser {

	private static final String BASE_PACKAGE_ATTRIBUTE = "value";

	private static final String PACKAGE_OF_ATTRIBUTE = "packageOf";

	private static final String RESOURCE_PATTERN_ATTRIBUTE = "resourcePattern";

	private static final String USE_DEFAULT_FILTERS_ATTRIBUTE = "useDefaultFilters";

	private static final String NAME_GENERATOR_ATTRIBUTE = "nameGenerator";

	private static final String SCOPE_RESOLVER_ATTRIBUTE = "scopeResolver";

	private static final String SCOPED_PROXY_ATTRIBUTE = "scopedProxy";

	private static final String EXCLUDE_FILTER_ATTRIBUTE = "excludeFilters";

	private static final String INCLUDE_FILTER_ATTRIBUTE = "includeFilters";

	private static final String FILTER_TYPE_ATTRIBUTE = "type";

	private final ProblemReporter problemReporter;


	public ComponentScanAnnotationMetadataParser(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	/**
	 * @return whether the given metadata includes {@link ComponentScan} information
	 */
	public boolean accepts(AnnotationMetadata metadata) {
		return metadata.hasAnnotation(ComponentScan.class.getName());
	}

	/**
	 * Parse {@link ComponentScan} information from the the given metadata
	 * and return a populated {@link ComponentScanMetadata}.
	 * @throws IllegalArgumentException if ComponentScan attributes are not present in metadata
	 * @see #accepts
	 */
	public ComponentScanMetadata parse(AnnotationMetadata metadata) {
		Map<String, Object> componentScanAttributes =
			metadata.getAnnotationAttributes(ComponentScan.class.getName(), true);

		Assert.notNull(componentScanAttributes,
				String.format("ComponentScan annotation not found while parsing " +
						"metadata for class [%s]. Use accepts(metadata) before " +
						"calling parse(metadata)", metadata.getClassName()));

		ComponentScanMetadata componentScanMetadata = new ComponentScanMetadata();

		String[] packageOfClasses = (String[])componentScanAttributes.get(PACKAGE_OF_ATTRIBUTE);
		String[] basePackages = (String[])componentScanAttributes.get(BASE_PACKAGE_ATTRIBUTE);
		if (packageOfClasses.length == 0 && basePackages.length == 0) {
			this.problemReporter.fatal(new InvalidComponentScanProblem(metadata.getClassName()));
		}
		for (String className : packageOfClasses) {
			componentScanMetadata.addBasePackage(className.substring(0, className.lastIndexOf('.')));
		}
		for (String pkg : basePackages) {
			componentScanMetadata.addBasePackage(pkg);
		}

		componentScanMetadata.setResourcePattern((String)componentScanAttributes.get(RESOURCE_PATTERN_ATTRIBUTE));
		componentScanMetadata.setUseDefaultFilters((Boolean)componentScanAttributes.get(USE_DEFAULT_FILTERS_ATTRIBUTE));
		componentScanMetadata.setBeanNameGenerator(instantiateUserDefinedStrategy(
				(String)componentScanAttributes.get(NAME_GENERATOR_ATTRIBUTE), BeanNameGenerator.class, ClassUtils.getDefaultClassLoader()));

		return componentScanMetadata;
	}

	/**
	 * TODO SPR-7194: duplicated from {@link ComponentScanBeanDefinitionParser}
	 */
	@SuppressWarnings("unchecked")
	private <T> T instantiateUserDefinedStrategy(String className, Class<T> strategyType, ClassLoader classLoader) {
		Object result = null;
		try {
			result = classLoader.loadClass(className).newInstance();
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Class [" + className + "] for strategy [" +
					strategyType.getName() + "] not found", ex);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("Unable to instantiate class [" + className + "] for strategy [" +
					strategyType.getName() + "]. A zero-argument constructor is required", ex);
		}

		if (!strategyType.isAssignableFrom(result.getClass())) {
			throw new IllegalArgumentException("Provided class name must be an implementation of " + strategyType);
		}
		return (T)result;
	}



	private static class InvalidComponentScanProblem extends Problem {
		public InvalidComponentScanProblem(String className) {
			super("@ComponentScan must declare either 'value' or 'packageOf' attributes",
					new Location(new DescriptiveResource(className)));
		}
	}
}
