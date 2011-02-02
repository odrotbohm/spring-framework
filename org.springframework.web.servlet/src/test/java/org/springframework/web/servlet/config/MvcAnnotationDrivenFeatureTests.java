package org.springframework.web.servlet.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Feature;
import org.springframework.context.annotation.FeatureConfiguration;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

/**
 * Integration tests for the {@link MvcAnnotationDriven} feature.
 *
 * @author Rossen Stoyanchev
 * @author Chris Beams
 * @since 3.1
 */
public class MvcAnnotationDrivenFeatureTests {

	@Test
	public void testMessageCodesResolver() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(MvcFeature.class, MvcBeans.class);
		ctx.refresh();
		AnnotationMethodHandlerAdapter adapter = ctx.getBean(AnnotationMethodHandlerAdapter.class);
		assertNotNull(adapter);
		Object initializer = new DirectFieldAccessor(adapter).getPropertyValue("webBindingInitializer");
		assertNotNull(initializer);
		MessageCodesResolver resolver = ((ConfigurableWebBindingInitializer) initializer).getMessageCodesResolver();
		assertNotNull(resolver);
		assertEquals("test.foo.bar", resolver.resolveMessageCodes("foo", "bar")[0]);
	}

}

@FeatureConfiguration
class MvcFeature {
	@Feature
	public MvcAnnotationDriven annotationDriven(MvcBeans mvcBeans) {
		return new MvcAnnotationDriven()
			.conversionService(mvcBeans.conversionService())
			.messageCodesResolver(mvcBeans.messageCodesResolver())
			.validator(mvcBeans.validator());
	}
}

@Configuration
class MvcBeans {
	@Bean
	public FormattingConversionService conversionService() {
		return new DefaultFormattingConversionService();
	}
	@Bean
	public Validator validator() {
		return new LocalValidatorFactoryBean();
	}
	@Bean MessageCodesResolver messageCodesResolver() {
		return new TestMessageCodesResolver();
	}
}

