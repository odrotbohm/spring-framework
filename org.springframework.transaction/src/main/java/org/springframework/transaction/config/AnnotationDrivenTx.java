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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.FeatureAnnotation;
import org.springframework.context.config.AdviceMode;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Chris Beams
 * @since 3.1
 */
@Documented
@FeatureAnnotation(parser=TxAnnotationDrivenParser.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnotationDrivenTx {
	/**
	 * Set the name of the {@link PlatformTransactionManager} bean to use.
	 * The default is {@link TxAnnotationDriven#DEFAULT_TRANSACTION_MANAGER_BEAN_NAME
	 * "transactionManager"}.
	 *
	 * <p>TODO SPR-8207 document by-type fallback and rarity of need for
	 * transactionManager attribute
	 */
	String transactionManager() default TxAnnotationDriven.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME;

	/**
	 * Indicate how transactional advice should be applied.
	 * The default is {@link TxAnnotationDriven#DEFAULT_ADVICE_MODE AdviceMode.PROXY}.
	 * @see AdviceMode
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * Indicate whether class-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies. The default is
	 * {@link TxAnnotationDriven#DEFAULT_PROXY_TRANSACTION_CLASS false}.
	 *
	 * <p>Note: Class-based proxies require the {@link Transactional @Transactional}
	 * annotation to be defined on the concrete class. Annotations in interfaces will
	 * not work in that case (they will rather only work with interface-based proxies)!
	 */
	boolean proxyTargetClass() default TxAnnotationDriven.DEFAULT_PROXY_TRANSACTION_CLASS;

}
