/**
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mockito.Answers;

/**
 * Similar to org.mockito.Mock, for use with testng+guice.
 *
 * <pre>
 *   &#064;Guice(moduleFactory = MyLibraryModuleFactory.class)
 *   public class MyLibraryIntegrationTests{
 *
 *       &#064;BindMock
 *       &#064;Inject
 *       private MyDependency myDependency;
 *       &#064;Inject
 *       private MyLibrary myLibrary;
 *
 *       &#064;Test
 *       public void test(){
 *           Mockito.doReturn(true).when(myDependency).foo();
 *           myLibrary.bar();
 *       }
 *   }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DefineMock{

	Answers answer() default Answers.RETURNS_DEFAULTS;
	String name() default "";
	Class<?>[] extraInterfaces() default {};

}