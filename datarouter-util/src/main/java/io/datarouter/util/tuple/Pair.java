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
package io.datarouter.util.tuple;

import java.io.Serializable;
import java.util.Objects;

import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings("serial")
public class Pair<L,R> implements Serializable{

	protected L left;
	protected R right;

	public Pair(L left, R right){
		this.left = left;
		this.right = right;
	}

	public Pair(){}

	public boolean areLeftAndRightEqual(){
		@SuppressWarnings("unlikely-arg-type")
		boolean result = Objects.equals(left, right);
		return result;
	}

	public L getLeft(){
		return left;
	}

	public void setLeft(L left){
		this.left = left;
	}

	public R getRight(){
		return right;
	}

	public void setRight(R right){
		this.right = right;
	}

	public Pair<R,L> reversed(){
		return new Pair<>(right, left);
	}

	@Override
	public String toString(){
		return "(" + String.valueOf(left) + ", " + String.valueOf(right) + ")";
	}

	@Override
	public boolean equals(Object other){
		if(other == null || !(other instanceof Pair)){
			return false;
		}
		Pair<?,?> pair = (Pair<?,?>) other;

		return (pair.getLeft() != null && pair.getLeft().equals(getLeft())
				|| pair.getLeft() == null && getLeft() == null)
				&& (pair.getRight() != null && pair.getRight().equals(getRight())
						|| pair.getRight() == null && getRight() == null);
	}

	@Override
	public int hashCode(){
		return Objects.hash(left, right);
	}

	public static class Tests{
		@Test
		public void testEquals(){
			Assert.assertNotEquals("a string", new Pair<>(null, null));
			Assert.assertNotEquals("a", new Pair<>("a", "a"));
			Assert.assertNotEquals("a", new Pair<>(null, "a"));
			Assert.assertNotEquals("a", new Pair<>("a", null));

			Assert.assertEquals(new Pair<>(null, null), new Pair<>(null, null));
			Assert.assertEquals(new Pair<>(null, "a"), new Pair<>(null, "a"));
			Assert.assertEquals(new Pair<>("a", null), new Pair<>("a", null));
			Assert.assertEquals(new Pair<>("a", "a"), new Pair<>("a", "a"));
			Assert.assertEquals(new Pair<>("a", "b"), new Pair<>("a", "b"));

			Assert.assertNotEquals(new Pair<>("c", "b"), new Pair<>("a", "b"));
			Assert.assertNotEquals(new Pair<>("a", "c"), new Pair<>("a", "b"));
			Assert.assertNotEquals(new Pair<>("a", "a"), new Pair<>("a", "b"));
			Assert.assertNotEquals(new Pair<>("b", "b"), new Pair<>("a", "b"));
		}
	}

}
