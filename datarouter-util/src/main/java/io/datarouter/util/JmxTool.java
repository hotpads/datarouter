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
package io.datarouter.util;

import java.lang.management.ManagementFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class JmxTool{

	public static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();

	public static ObjectName newObjectName(String string){
		try{
			return new ObjectName(string);
		}catch(MalformedObjectNameException e){
			throw new RuntimeException(e);
		}
	}

	public static Object getAttribute(ObjectName objectName, String attribute){
		try{
			return SERVER.getAttribute(objectName, attribute);
		}catch(InstanceNotFoundException | AttributeNotFoundException | ReflectionException | MBeanException e){
			throw new RuntimeException(e);
		}
	}

	public static Object createMBean(String className, ObjectName objectName){
		try{
			return SERVER.createMBean(className, objectName);
		}catch(NotCompliantMBeanException | InstanceAlreadyExistsException | MBeanException | ReflectionException e){
			throw new RuntimeException(e);
		}
	}

}
