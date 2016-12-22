package com.hotpads.datarouter.op.util;

import java.util.ArrayList;
import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.jmx.mbeanserver.NamedObject;

public class ResultMergeToolTests{

	@Test
	public void testSumIntegers(){
		Integer intA = null;
		Collection<Integer> ints = null;
		Assert.assertNull(ResultMergeTool.sum(intA,ints));
		ints = new ArrayList<>();
		Assert.assertNull(ResultMergeTool.sum(intA,ints));
		ints.add(1);
		ints.add(2);
		Assert.assertNull(ResultMergeTool.sum(intA,ints));
		intA = 3;
		Assert.assertEquals(ResultMergeTool.sum(intA, ints),6D);
	}

	public void testSumLongs(){
		Long longA = null;
		Collection<Long> longs = null;
		Assert.assertNull(ResultMergeTool.sum(longA,longs));
		longs = new ArrayList<>();
		Assert.assertNull(ResultMergeTool.sum(longA,longs));
		longs.add(1L);
		longs.add(2L);
		Assert.assertNull(ResultMergeTool.sum(longA,longs));
		longA = 3L;
		Assert.assertEquals(ResultMergeTool.sum(longA, longs),Long.valueOf(6));
	}

	public void testFirst(){
		ArrayList<String> otherObjects = null;
		Assert.assertNull(ResultMergeTool.first(null,otherObjects));
		otherObjects = new ArrayList<>();
		Assert.assertNull(ResultMergeTool.first(null, otherObjects));
		otherObjects.add("world");
		Assert.assertEquals(ResultMergeTool.first(null, otherObjects),"world");
		Assert.assertEquals(ResultMergeTool.first("hello", otherObjects),"hello");
	}

	public void testAppend(){
		//TODO
	}
}
