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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.config.ExecutorContext;
import org.springframework.context.config.FeatureSpecificationExecutor;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} that parses the {@code annotation-driven} element 
 * to configure a Spring MVC web application. 
 *
 * @author Rossen Stoyanchev
 * @since 3.0
 * @see MvcAnnotationDriven
 * @see MvcAnnotationDrivenExecutor
 */
class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

	/**
	 * Parses the {@code <mvc:annotation-driven/>} tag.
	 */
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		MvcAnnotationDriven spec = createSpecification(element, parserContext);
		FeatureSpecificationExecutor executor = BeanUtils.instantiateClass(spec.executorType(),
				FeatureSpecificationExecutor.class);
		executor.execute(spec, createExecutorContext(parserContext));
		return null;
	}

	private MvcAnnotationDriven createSpecification(Element element, ParserContext parserContext) {
		MvcAnnotationDriven spec = new MvcAnnotationDriven();
		if (element.hasAttribute("conversion-service")) {
			String conversionService = element.getAttribute("conversion-service");
			spec.conversionService(conversionService);
		}
		if (element.hasAttribute("validator")) {
			spec.validator(element.getAttribute("validator"));
		}
		if (element.hasAttribute("message-codes-resolver")) {
			spec.messageCodesResolver(element.getAttribute("message-codes-resolver"));
		}
		// TODO
		//		Element convertersElement = DomUtils.getChildElementByTagName(element, "message-converters");
		//		if (converters != null) {
		//			ManagedList<BeanDefinitionHolder> messageConverters = new ManagedList<BeanDefinitionHolder>();
		//			messageConverters.setSource(source);
		//			for (Element converter : DomUtils.getChildElementsByTagName(convertersElement, "bean")) {
		//				BeanDefinitionHolder beanDef = parserContext.getDelegate().parseBeanDefinitionElement(converter);
		//				beanDef = parserContext.getDelegate().decorateBeanDefinitionIfRequired(converter, beanDef);
		//				messageConverters.add(beanDef);
		//			}
		//			return messageConverters;
		//		} else {
		//					
		//		}
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
