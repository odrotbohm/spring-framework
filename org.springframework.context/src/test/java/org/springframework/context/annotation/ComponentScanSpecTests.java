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

import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.context.InvalidSpecificationException;

/**
 * Unit tests for {@link ComponentScanSpec}.
 * 
 * @author Chris Beams
 * @since 3.1
 */
public class ComponentScanSpecTests {

	@Test
	public void verificationFailsWithoutBasePackages() {
		ComponentScanSpec spec = new ComponentScanSpec();
		try {
			spec.validate();
			fail("expected exception on verification");
		} catch (InvalidSpecificationException ex) {
			// expected
		}

		spec.addBasePackage("org.some.pkg");
		spec.validate(); // success
	}

}