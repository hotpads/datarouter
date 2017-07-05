package io.datarouter.util.iterable.scanner.sorted;

public abstract class BaseHoldingScanner<T> extends BaseScanner<T>{

	protected T current;

	@Override
	public T getCurrent(){
		return current;
	}
}
