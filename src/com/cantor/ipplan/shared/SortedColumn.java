package com.cantor.ipplan.shared;

public interface SortedColumn<T, C> {
  public abstract C getSortedValue(T object);
}
