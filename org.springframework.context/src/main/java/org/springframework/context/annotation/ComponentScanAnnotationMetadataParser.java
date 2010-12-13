/*
 * Copyright 2002-2010 the original author or authors.
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
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;


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

	public boolean accepts(AnnotationMetadata metadata) {
		return metadata.hasAnnotation(ComponentScan.class.getName());
	}

	public ComponentScanDefinition parse(AnnotationMetadata metadata) {
		Map<String, Object> componentScanAttributes =
			metadata.getAnnotationAttributes(ComponentScan.class.getName(), true);

		Assert.notNull(componentScanAttributes,
				String.format("ComponentScan annotation not found while parsing " +
						"metadata for class [%s]. Use accepts(metadata) before " +
						"calling parse(metadata)", metadata.getClassName()));

		ComponentScanDefinition definition = new ComponentScanDefinition();

		String[] packageOfClasses = (String[])componentScanAttributes.get(PACKAGE_OF_ATTRIBUTE);
		String[] basePackages = (String[])componentScanAttributes.get(BASE_PACKAGE_ATTRIBUTE);
		if (packageOfClasses.length == 0 && basePackages.length == 0) {
			this.problemReporter.fatal(new InvalidComponentScanProblem(metadata.getClassName()));
		}
		for (String className : packageOfClasses) {
			definition.addBasePackage(className.substring(0, className.lastIndexOf('.')));
		}
		for (String pkg : basePackages) {
			definition.addBasePackage(pkg);
		}

		definition.setResourcePattern((String)componentScanAttributes.get(RESOURCE_PATTERN_ATTRIBUTE));
		definition.setUseDefaultFilters((Boolean)componentScanAttributes.get(USE_DEFAULT_FILTERS_ATTRIBUTE));

		return definition;
	}


	private static class InvalidComponentScanProblem extends Problem {
		public InvalidComponentScanProblem(String className) {
			super("@ComponentScan must declare either 'value' or 'packageOf' attributes",
					new Location(new DescriptiveResource(className)));
		}
	}
}
