package com.github.mbergenlid.serde;

public interface Deserializer<E extends Exception> {

   <T, V extends Visitor<T>> T deserializeBool(V visitor) throws E;

   <T, V extends Visitor<T>> T deserializeString(V visitor) throws E;
}
