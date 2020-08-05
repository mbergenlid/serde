package com.github.mbergenlid.serde.impl;

import com.github.mbergenlid.serde.Serialize;
import com.github.mbergenlid.serde.Serializer;

public class Serde<T> implements Serialize {

   private final T value;
   private final SerializeFunction<T> serializeFunction;

   private Serde(T value, SerializeFunction<T> serializeFunction) {
      this.value = value;
      this.serializeFunction = serializeFunction;
   }

   public static <T> Serialize wrap(T value, SerializeFunction<T> serializeFunction) {
      return new Serde<>(value, serializeFunction);
   }

   @Override
   public <E extends Exception> void serialize(Serializer<E> serializer) throws E {
      serializeFunction.serialize(value, serializer);
   }

   public static <E extends Exception> void serializeInt(int integer, Serializer<E> serializer) throws E {
      serializer.serializeInt(integer);
   }

   public static <E extends Exception> void serializeByte(byte value, Serializer<E> serializer) throws E {
      serializer.serializeByte(value);
   }

   public static <E extends Exception> void serializeShort(short value, Serializer<E> serializer) throws E {
      serializer.serializeShort(value);
   }

   public static <E extends Exception> void serializeLong(long value, Serializer<E> serializer) throws E {
      serializer.serializeLong(value);
   }

   public static <E extends Exception> void serializeString(String value, Serializer<E> serializer) throws E {
      serializer.serializeString(value);
   }

   public static <E extends Exception> void serializeFloat(float value, Serializer<E> serializer) throws E {
      serializer.serializeFloat(value);
   }

   public static <E extends Exception> void serializeDouble(double value, Serializer<E> serializer) throws E {
      serializer.serializeDouble(value);
   }

   public static <E extends Exception> void serializeBool(boolean value, Serializer<E> serializer) throws E {
      serializer.serializeBool(value);
   }

   public interface SerializeFunction<T> {
      <E extends Exception> void serialize(T value, Serializer<E> serializer) throws E;
   }
}
