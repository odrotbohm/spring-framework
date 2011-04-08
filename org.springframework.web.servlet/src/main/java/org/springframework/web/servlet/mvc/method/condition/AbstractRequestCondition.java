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

package org.springframework.web.servlet.mvc.method.condition;

/**
 * Abstract base class for {@link RequestCondition} that provides a standard {@link Comparable} implementation.
 *
 * @author Arjen Poutsma
 * @since 3.1
 */
public abstract class AbstractRequestCondition implements RequestCondition {

	private final int weight;

	protected AbstractRequestCondition(int weight) {
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}

	public int compareTo(RequestCondition o) {
		AbstractRequestCondition other = (AbstractRequestCondition) o;
		return other.weight - this.weight;
	}

}
