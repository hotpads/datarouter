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
package io.datarouter.testng.parallel;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class BaseParallelExecutionTest{
	private static final Logger logger = LoggerFactory.getLogger(BaseParallelExecutionTest.class);

	@BeforeClass
	public void beforeClass(){
		print(getClass().getSimpleName() + " tests");
	}

	@AfterClass
	public void afterClass(){
		print("");
	}

	@BeforeMethod
	public void beforeMethod(Method method){
		print("Start - " + method.getName());
	}

	@AfterMethod
	public void afterMethod(Method method){
		print("End - " + method.getName());
	}

	@Test(enabled = false)
	public void test1(){
		print("Execute - test1");
		sleep();
	}

	@Test(enabled = false)
	public void test2(){
		print("Execute - test2");
		sleep();
	}

	@Test(enabled = false)
	public void test3(){
		print("Execute - test3");
		sleep();
	}

	@Test(enabled = false)
	public void test4(){
		print("Execute - test4");
		sleep();
	}

	@Test(enabled = false)
	public void test5(){
		print("Execute - test5");
		sleep();
	}

	private static void sleep(){
		try{
			Thread.sleep(2_000);
		}catch(InterruptedException e){
			// ignore
		}
	}

	private void print(String str){
		logger.warn(str);
	}

}
