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

import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.core.type.MethodMetadata;

/**
 * Represents a {@link Configuration} class method marked with the
 * {@link Feature} annotation.
 *
 * @author Chris Beams
 * @since 3.1
 * @see ConfigurationClass
 * @see ConfigurationClassParser
 * @see ConfigurationClassBeanDefinitionReader
 */
final class FeatureMethod extends ConfigurationMethod {

	public FeatureMethod(MethodMetadata metadata, ConfigurationClass configurationClass) {
		super(metadata, configurationClass);
	}

	@Override
	public void validate(ProblemReporter problemReporter) {
		// TODO SPR-8126: think about validation for @Feature methods. non-void, etc.
		// shouldn't be many constraints, however.  could even be static, final, as
		// there is no cglib in the mix.
	}

}
