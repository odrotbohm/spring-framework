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

package org.springframework.context.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Type;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.mock.env.MockEnvironment;


/**
 * Unit tests for {@link ComponentScanDefinitionReader}
 * 
 * @author Chris Beams
 */
public class ComponentScanDefinitionReaderTests {
	@Test
	public void name() {
		//new ComponentScanDefinitionReader();
	}

	@Test
	public void assignable() {
		assertTrue(Type[].class.isAssignableFrom(Class[].class));
		Object foo = new Class[] { };
		assertTrue(foo instanceof Type[]);
	}

	@Test
	public void test() throws IOException {
		MetadataReader reader = new SimpleMetadataReaderFactory().getMetadataReader(Example.class.getName());
		ComponentScanDefinition componentScanDefinition =
			new ComponentScanAnnotationMetadataParser(EasyMock.createNiceMock(ProblemReporter.class)).parse(reader.getAnnotationMetadata());
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		Environment env = new MockEnvironment();
		new ComponentScanDefinitionReader(bf, resourceLoader, env).read(componentScanDefinition);
		assertThat(bf.containsBean("fooServiceImpl"), is(true));
	}
}

@ComponentScan("example.scannable")
class Example { }