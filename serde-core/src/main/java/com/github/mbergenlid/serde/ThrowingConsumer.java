package com.github.mbergenlid.serde;

public interface ThrowingConsumer<T, E extends Exception> {
   void accept(T value) throws E;
}
