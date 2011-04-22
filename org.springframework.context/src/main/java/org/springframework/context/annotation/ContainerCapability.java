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

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Interface representing an optional capability of the Spring
 * container that may be enabled at the user's request through
 * an annotation such as @{@link ComponentScan}.
 *
 * @author Chris Beams
 * @since 3.1
 * @see Enable
 * @see ComponentScan
 */
public interface ContainerCapability {

	/**
	 * Enable this container capability, usually resulting in registration
	 * of bean definitions against the given registry. Settings for the
	 * capability are available through the given annotation metadata.
	 * @param registry the bean definition registry
	 * @param annotationMetadata annotation metadata for the annotated class
	 */
	void enable(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata);

}