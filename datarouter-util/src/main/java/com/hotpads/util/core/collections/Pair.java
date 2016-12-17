package com.hotpads.util.core.collections;

import java.io.Serializable;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("serial")
public class Pair<L,R> implements Serializable{

	protected L left;
	protected R right;

	public Pair(L left, R right){
		this.left = left;
		this.right = right;
	}

	public Pair(){}

	@Deprecated // use new Pair<>(..)
	public static <T,V> Pair<T,V> create(T left, V right){
		return new Pair<>(left, right);
	}

	public boolean areLeftAndRightEqual(){
		return Objects.equals(left, right);
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

	@Override
	public String toString(){
		return "(" + left.toString() + ", " + right.toString() + ")";
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
			Assert.assertNotEquals(new Pair<>(null, null), "a string");
			Assert.assertNotEquals(new Pair<>("a", "a"), "a");
			Assert.assertNotEquals(new Pair<>(null, "a"), "a");
			Assert.assertNotEquals(new Pair<>("a", null), "a");

			Assert.assertEquals(new Pair<>(null, null), new Pair<>(null, null));
			Assert.assertEquals(new Pair<>(null, "a"), new Pair<>(null, "a"));
			Assert.assertEquals(new Pair<>("a", null), new Pair<>("a", null));
			Assert.assertEquals(new Pair<>("a", "a"), new Pair<>("a", "a"));
			Assert.assertEquals(new Pair<>("a", "b"), new Pair<>("a", "b"));

			Assert.assertNotEquals(new Pair<>("a", "b"), new Pair<>("c", "b"));
			Assert.assertNotEquals(new Pair<>("a", "b"), new Pair<>("a", "c"));
			Assert.assertNotEquals(new Pair<>("a", "b"), new Pair<>("a", "a"));
			Assert.assertNotEquals(new Pair<>("a", "b"), new Pair<>("b", "b"));
		}
	}

}
