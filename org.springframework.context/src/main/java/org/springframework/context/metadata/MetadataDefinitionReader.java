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

package org.springframework.context.metadata;

import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinitionHolder;


/**
 * Interface for reading a populated {@link MetadataDefinition} object. Provides
 * a generic mechanism for handling container configuration metadata regardless of
 * origin in XML, annotations, or otherwise.
 *
 * <p>Population of a {@link MetadataDefinition} from XML or annotations will be
 * performed by a {@link org.springframework.beans.factory.config.BeanDefinitionParser}
 * or {@link org.springframework.context.annotation.AnnotationMetadataParser},
 * respectively.
 *
 * <p><em>Reading</em> usually implies acting against the metadata in such a way that
 * beans are registered against the Spring container.
 *
 * @author Chris Beams
 * @since 3.1
 * @see org.springframework.beans.factory.xml.BeanDefinitionParser
 * @see org.springframework.context.annotation.AnnotationMetadataParser
 * @see org.springframework.context.annotation.ComponentScanMetadataReader
 */
public interface MetadataDefinitionReader<M extends MetadataDefinition> {

	/**
	 * Read and act upon the given metadata.
	 * @return set of bean definitions registered while reading (if any) for
	 * tooling purposes. May return empty, never returns null.
	 */
	Set<BeanDefinitionHolder> read(M metadataDefinition);

}
