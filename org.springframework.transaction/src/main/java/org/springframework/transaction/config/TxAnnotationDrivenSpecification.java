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

import org.springframework.beans.factory.parsing.ProblemCollector;
import org.springframework.context.config.AbstractFeatureSpecification;
import org.springframework.context.config.AdviceMode;
import org.springframework.context.config.FeatureSpecificationExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Specifies the configuration of Spring's <em>annotation driven transaction
 * management</em> feature. Package-private and for internal use by
 * {@code <tx:annotation-driven/>} element and
 * {@link TxAnnotationDriven @TxAnnotationDriven} annotation.
 *
 * @author Chris Beams
 * @since 3.1
 */
final class TxAnnotationDrivenSpecification extends AbstractFeatureSpecification {

	public static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";

	public static final AdviceMode DEFAULT_ADVICE_MODE = AdviceMode.PROXY;

	public static final boolean DEFAULT_PROXY_TRANSACTION_CLASS = false;

	private static final Class<? extends FeatureSpecificationExecutor> EXECUTOR_TYPE = TxAnnotationDrivenExecutor.class;

	private Object txManager = null;

	private Object order = null;

	private Boolean proxyTargetClass = DEFAULT_PROXY_TRANSACTION_CLASS;

	private Object mode = DEFAULT_ADVICE_MODE;

	/**
	 * Create a {@code TxAnnotationDriven} specification assumes the presence of a
	 * {@link PlatformTransactionManager} bean named {@value #DEFAULT_TRANSACTION_MANAGER_BEAN_NAME}.
	 *
	 * <p>See the alternate constructors defined here if your transaction manager does
	 * not follow this default naming or you wish to refer to it by bean instance rather
	 * than by bean name.
	 * @see #TxAnnotationDriven(String)
	 * @see #TxAnnotationDriven(PlatformTransactionManager)
	 */
	public TxAnnotationDrivenSpecification() {
		this(DEFAULT_TRANSACTION_MANAGER_BEAN_NAME);
	}

	/**
	 * Create a new {@code TxAnnotationDriven} specification that will use the specified
	 * transaction manager bean name.
	 *
	 * @param txManagerBeanName name of {@link PlatformTransactionManager} bean or a
	 * ${placeholder} or SpEL #{expression} resolving to bean name. If {@code null},
	 * falls back to default value of {@value #DEFAULT_TRANSACTION_MANAGER_BEAN_NAME}.
	 */
	public TxAnnotationDrivenSpecification(String txManagerBeanName) {
		super(EXECUTOR_TYPE);
		this.txManager = txManagerBeanName != null ?
				txManagerBeanName :
				DEFAULT_TRANSACTION_MANAGER_BEAN_NAME;
	}

	/**
	 * Create a new TxAnnotationDriven specification that will use the specified transaction
	 * manager.
	 *
	 * @param txManager the {@link PlatformTransactionManager} bean to use. Must not be {@code null}.
	 */
	public TxAnnotationDrivenSpecification(PlatformTransactionManager txManager) {
		super(EXECUTOR_TYPE);
		Assert.notNull(txManager, "transaction manager must not be null");
		this.txManager = txManager;
	}

	/**
	 * Return the transaction manager to use.  May be a {@link PlatformTransactionManager}
	 * instance or a String representing the bean name or a placeholder or SpEL expression
	 * that resolves to the bean name.
	 */
	Object transactionManager() {
		return this.txManager;
	}

	/**
	 * Indicate how transactional advice should be applied.
	 * @see AdviceMode
	 */
	public TxAnnotationDrivenSpecification mode(AdviceMode mode) {
		this.mode = mode;
		return this;
	}

	/**
	 * Indicate how transactional advice should be applied.
	 * @param name matching one of the labels in the AdviceMode enum; 
	 * placeholder and SpEL expressions are not allowed.
	 * @see AdviceMode
	 */
	TxAnnotationDrivenSpecification mode(String mode) {
		if (StringUtils.hasText(mode)) {
			this.mode = mode;
		}
		return this;
	}

	/**
	 * Return how transactional advice should be applied.
	 */
	AdviceMode mode() {
		if (this.mode instanceof AdviceMode) {
			return (AdviceMode)this.mode;
		}
		if (this.mode instanceof String) {
			return ObjectUtils.caseInsensitiveValueOf(AdviceMode.values(), (String)this.mode);
		}
		// TODO SPR-7420: deal with in validate & raise problem
		throw new IllegalStateException(
				"invalid type for field 'mode' (must be of type AdviceMode or String): "
				+ this.mode.getClass().getName());
	}

	/**
	 * Indicate whether class-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies.
	 *
	 * <p>Note: Class-based proxies require the {@link Transactional @Transactional}
	 * annotation to be defined on the concrete class. Annotations in interfaces will
	 * not work in that case (they will rather only work with interface-based proxies)!
	 */
	public TxAnnotationDrivenSpecification proxyTargetClass(Boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
		return this;
	}

	/**
	 * Return whether class-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies.
	 */
	Boolean proxyTargetClass() {
		return this.proxyTargetClass;
	}

	/**
	 * Indicate the ordering of the execution of the transaction advisor
	 * when multiple advice executes at a specific joinpoint. The default is
	 * {@code null}, indicating that default ordering should be used.
	 */
	public TxAnnotationDrivenSpecification order(int order) {
		this.order = order;
		return this;
	}

	/**
	 * Indicate the ordering of the execution of the transaction advisor
	 * when multiple advice executes at a specific joinpoint. The default is
	 * {@code null}, indicating that default ordering should be used.
	 */
	public TxAnnotationDrivenSpecification order(String order) {
		if (StringUtils.hasText(order)) {
			this.order = order;
		}
		return this;
	}

	/**
	 * Return the ordering of the execution of the transaction advisor
	 * when multiple advice executes at a specific joinpoint. May return
	 * {@code null}, indicating that default ordering should be used.
	 */
	Object order() {
		return this.order;
	}

	@Override
	protected void doValidate(ProblemCollector problems) {
		if (this.mode instanceof String) {
			if (!ObjectUtils.containsConstant(AdviceMode.values(), (String)this.mode)) {
				problems.error("no such mode name: " + this.mode);
			}
		}
	}

}