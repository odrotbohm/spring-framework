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

package org.springframework.transaction.config;

import java.util.Map;

import org.springframework.context.annotation.FeatureAnnotationParser;
import org.springframework.context.config.AdviceMode;
import org.springframework.context.config.FeatureSpecification;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

/**
 * {@link FeatureAnnotationParser} implementation that reads attributes from a
 * {@link TxAnnotationDrivenSpecification @TxAnnotationDriven} annotation into a
 * {@link TxAnnotationDrivenSpecification} which can in turn be executed by
 * {@link TxAnnotationDrivenExecutor}. {@link AnnotationDrivenBeanDefinitionParser}
 * serves the same role for the {@code <tx:annotation-driven>} XML element.
 *
 * @author Chris Beams
 * @since 3.1
 * @see TxAnnotationDrivenSpecification
 * @see TxAnnotationDrivenSpecification
 * @see TxAnnotationDrivenExecutor
 * @see AnnotationDrivenBeanDefinitionParser
 */
final class TxAnnotationDrivenParser implements FeatureAnnotationParser {

	public FeatureSpecification parse(AnnotationMetadata metadata) {
		Map<String, Object> attribs = metadata.getAnnotationAttributes(TxAnnotationDriven.class.getName(), true);
		Assert.notNull(attribs, String.format("@TxAnnotationDriven annotation not found " +
				"while parsing metadata for class [%s].", metadata.getClassName()));

		return new TxAnnotationDrivenSpecification((String)attribs.get("transactionManager"))
			.mode((AdviceMode) attribs.get("mode"))
			.proxyTargetClass((Boolean) attribs.get("proxyTargetClass"))
			.source(metadata.getClassName())
			.sourceName(metadata.getClassName());
	}

}
