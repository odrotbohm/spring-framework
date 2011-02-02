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

package org.springframework.web.servlet.config;

import java.util.Arrays;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.config.ExecutorContext;
import org.springframework.context.config.FeatureSpecificationExecutor;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser} that parses a
 * {@code resources} element. 
 *
 * @author Keith Donald
 * @author Jeremy Grelle
 * @author Rossen Stoyanchev
 * @since 3.0.4
 * @see ResourcesSpec
 * @see ResourcesSpecExecutor
 */
class ResourcesBeanDefinitionParser implements BeanDefinitionParser {

	/**
	 * Parses the {@code <mvc:resources/>} tag
	 */
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		ResourcesSpec spec = createSpecification(element, parserContext);
		FeatureSpecificationExecutor executor = BeanUtils.instantiateClass(spec.executorType(),
				FeatureSpecificationExecutor.class);
		executor.execute(spec, createExecutorContext(parserContext));
		return null;
	}

	private ResourcesSpec createSpecification(Element element, ParserContext parserContext) {
		String[] locations = StringUtils.commaDelimitedListToStringArray(element.getAttribute("location"));
		ResourcesSpec spec = new ResourcesSpec(Arrays.asList(locations), element.getAttribute("mapping"));
		if (element.hasAttribute("cache-period")) {
			spec.cachePeriod(Integer.valueOf(element.getAttribute("cache-period")));
		}
		if (element.hasAttribute("order")) {
			spec.order(Integer.valueOf(element.getAttribute("order")));
		}
		spec.setSource(parserContext.extractSource(element));
		spec.setSourceName(element.getTagName());
		return spec;
	}

	/**
	 * Adapt the given ParserContext instance into an ExecutorContext.
	 *
	 * TODO SPR-7420: consider unifying the two through a superinterface.
	 * TODO SPR-7420: create a common ParserContext-to-ExecutorContext adapter util
	 */
	private ExecutorContext createExecutorContext(ParserContext parserContext) {
		ExecutorContext executorContext = new ExecutorContext();
		executorContext.setRegistry(parserContext.getRegistry());
		executorContext.setRegistrar(parserContext);
		return executorContext;
	}

}
