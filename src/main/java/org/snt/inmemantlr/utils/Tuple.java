package org.snt.inmemantlr.utils;

public class Tuple<K,T> {
	
	private K first;
	private T second;
	
	public Tuple(K first, T second) {
		this.first = first;
		this.second = second;
	}
	
	public K getFirst() {
		return first;
	}
	public void setFirst(K first) {
		this.first = first;
	}
	public T getSecond() {
		return second;
	}
	public void setSecond(T second) {
		this.second = second;
	}

}
