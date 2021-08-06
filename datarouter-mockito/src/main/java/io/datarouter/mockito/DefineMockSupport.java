/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.mockito.MockSettings;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;

public class DefineMockSupport{

	public static Module createMockModule(Class<?> testClass){
		return Scanner.of(ReflectionTool.getDeclaredFieldsIncludingAncestors(testClass))
				.exclude(field -> Modifier.isStatic(field.getModifiers()))
				.exclude(field -> Modifier.isFinal(field.getModifiers()))
				.exclude(field -> field.getAnnotation(DefineMock.class) == null)
				.listTo(DefineMockSupport::moduleMocking);
	}

	private static Module moduleMocking(List<Field> fields){
		return new AbstractModule(){
			@Override
			protected void configure(){
				for(Field field : fields){
					DefineMock annotation = field.getAnnotation(DefineMock.class);
					Class<?> type = field.getType();

					MockSettings mockSettings = Mockito.withSettings();
					if(annotation.extraInterfaces().length > 0){
						mockSettings.extraInterfaces(annotation.extraInterfaces());
					}
					if("".equals(annotation.name())){
						mockSettings.name(field.getName());
					}else{
						mockSettings.name(annotation.name());
					}
					mockSettings.defaultAnswer(annotation.answer());
					doBind(type, mockSettings);
				}
			}

			private <T> void doBind(Class<T> type, MockSettings mockSettings){
				T mock = Mockito.mock(type, mockSettings);
				bind(type).toInstance(mock);
			}
		};
	}

}
