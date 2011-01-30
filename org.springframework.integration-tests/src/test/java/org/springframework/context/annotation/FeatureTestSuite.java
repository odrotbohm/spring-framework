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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.transaction.TxNamespaceHandlerTests;
import org.springframework.transaction.annotation.AnnotationTransactionNamespaceHandlerTests;
import org.springframework.transaction.annotation.TxAnnotationDrivenFeatureTests;
import org.springframework.transaction.config.AnnotationDrivenTests;

/**
 * Tests directly or indirectly related to {@link FeatureConfiguration} class and
 * {@link Feature} method processing.
 *
 * @author Chris Beams
 * @since 3.1
 */
@RunWith(Suite.class)
@SuiteClasses({
	EarlyBeanReferenceProxyCreatorTests.class,
	SimpleFeatureMethodProcessingTests.class,
	BeanFactoryAwareFeatureConfigurationTests.class,
	FeatureMethodBeanReferenceTests.class,
	FeatureMethodQualifiedBeanReferenceTests.class,
	FeatureConfigurationClassTests.class,
	FeatureMethodEarlyBeanProxyTests.class,
	FeatureConfigurationImportTests.class,
	FeatureConfigurationImportResourceTests.class,

	// context:component-scan related
	ComponentScanFeatureTests.class,

	// tx-related
	TxAnnotationDrivenFeatureTests.class,
	TxNamespaceHandlerTests.class,
	AnnotationTransactionNamespaceHandlerTests.class,
	AnnotationDrivenTests.class,
})
public class FeatureTestSuite {

}