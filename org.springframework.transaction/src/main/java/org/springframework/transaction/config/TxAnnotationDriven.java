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

import org.springframework.aop.config.ProxySpecification;
import org.springframework.context.AbstractFeatureSpecification;
import org.springframework.context.InvalidSpecificationException;
import org.springframework.context.SpecificationExecutor;
import org.springframework.context.annotation.ProxyType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * TODO SPR-7420: document
 *
 * @author Chris Beams
 * @since 3.1
 */
public class TxAnnotationDriven extends AbstractFeatureSpecification implements ProxySpecification {

	private static final Class<? extends SpecificationExecutor> DEFAULT_EXECUTOR_TYPE = TxAnnotationDrivenExecutor.class;

	private Object txManager;

	private Integer order = null;

	private boolean proxyTargetClass = false;

	private ProxyType proxyType = ProxyType.SPRINGAOP;

	private Boolean exposeProxy = false;

	static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";

	/**
	 * Create a TxAnnotationDriven specification assumes the presence of a
	 * {@link PlatformTransactionManager} bean named {@value #DEFAULT_TRANSACTION_MANAGER_BEAN_NAME}.
	 *
	 * <p>See the alternate constructors defined here if your transaction manager does not follow
	 * this default naming or you wish to refer to it by bean instance rather than by bean name.
	 * @see #TxAnnotationDriven(String)
	 * @see #TxAnnotationDriven(PlatformTransactionManager)
	 */
	public TxAnnotationDriven() {
		this(DEFAULT_TRANSACTION_MANAGER_BEAN_NAME);
	}

	/**
	 * Create a new TxAnnotationDriven specification that will use the specified transaction
	 * manager.
	 *
	 * @param txManager name of {@link PlatformTransactionManager} bean, ${placeholder}
	 * resolving to a bean name, or SpEL #{expression} resolving to bean name or bean instance.
	 * If {@code null}, falls back to default value of {@value #DEFAULT_TRANSACTION_MANAGER_BEAN_NAME}.
	 */
	public TxAnnotationDriven(String txManager) {
		super(DEFAULT_EXECUTOR_TYPE);
		this.txManager = txManager != null ? txManager : DEFAULT_TRANSACTION_MANAGER_BEAN_NAME;
	}

	/**
	 * Create a new TxAnnotationDriven specification that will use the specified transaction
	 * manager.
	 *
	 * @param txManager the {@link PlatformTransactionManager} bean to use. Must not be {@code null}.
	 */
	public TxAnnotationDriven(PlatformTransactionManager txManager) {
		super(DEFAULT_EXECUTOR_TYPE);
		Assert.notNull(txManager, "transaction manager must not be null");
		this.txManager = txManager;
	}

	/**
	 * Return the transaction manager to use.  May be a {@link PlatformTransactionManager} instance
	 * or a String representing the bean name, a placeholder resolving to the bean name, or a SpEL
	 * expression that resolves the bean name or bean instance.
	 */
	public Object transactionManager() {
		return this.txManager;
	}

	/**
	 * Set the type of transaction proxy to create: Spring AOP proxies or AspectJ-style weaving.
	 * The default is {@link ProxyType#SPRINGAOP}.
	 */
	public TxAnnotationDriven proxyType(ProxyType proxyType) {
		this.proxyType = proxyType;
		return this;
	}

	/**
	 * Return the type of proxy to create.
	 */
	public ProxyType proxyType() {
		return this.proxyType;
	}

	/**
	 * {@inheritDoc}
	 * <p>Note: Class-based proxies require the {@link Transactional @Transactional}
	 * annotation to be defined on the concrete class. Annotations in interfaces will
	 * not work in that case (they will rather only work with interface-based proxies)!
	 */
	public TxAnnotationDriven proxyTargetClass(Boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
		return this;
	}

	public Boolean proxyTargetClass() {
		return this.proxyTargetClass;
	}

	public TxAnnotationDriven exposeProxy(Boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
		return this;
	}

	public Boolean exposeProxy() {
		return this.exposeProxy;
	}

	/**
	 * Indicate the ordering of the execution of the transaction advisor
	 * when multiple advice executes at a specific joinpoint. The default is
	 * {@code null}, indicating that default ordering should be used.
	 */
	public TxAnnotationDriven order(Integer order) {
		this.order = order;
		return this;
	}

	/**
	 * Return the ordering of the execution of the transaction advisor
	 * when multiple advice executes at a specific joinpoint. May return
	 * {@code null}, indicating that default ordering should be used.
	 */
	public Integer order() {
		return this.order;
	}

	/**
	 * {@inheritDoc}
	 * <p>This implementation is a no-op, i.e. it is impossible to create
	 * an invalid {@code TxAnnotationDriven} instance via its API.
	 */
	public void validate() throws InvalidSpecificationException {
	}

}