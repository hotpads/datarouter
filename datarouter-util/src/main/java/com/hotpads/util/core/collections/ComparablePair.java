package com.hotpads.util.core.collections;

import com.hotpads.datarouter.util.core.DrComparableTool;

@SuppressWarnings("serial")
public class ComparablePair<
	L extends Comparable<? super L>,
	R extends Comparable<? super R>>
extends Pair<L,R>
implements Comparable<ComparablePair<L,R>>{

	public ComparablePair(L left, R right){
		super(left, right);
	}

	public static <L extends Comparable<? super L>,R extends Comparable<? super R>> ComparablePair<L,R> create(L left,
			R right){
		return new ComparablePair<>(left, right);
	}

	@Override
	public int compareTo(ComparablePair<L,R> other){
		int leftDiff = DrComparableTool.nullFirstCompareTo(this.left, other.left);
		if(leftDiff != 0){
			return leftDiff;
		}
		return DrComparableTool.nullFirstCompareTo(this.right, other.right);
	}

}
