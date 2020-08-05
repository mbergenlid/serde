package com.github.mbergenlid.serde.json;

import com.github.mbergenlid.serde.Serialize;
import com.github.mbergenlid.serde.impl.Serde;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SerdeJson {

   public static byte[] toBytes(Serialize value) {
      return toBytes(value, Serialize::serialize);
   }

   public static <T> byte[] toBytes(T value, Serde.SerializeFunction<T> serializeFn) {
      final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      final JsonSerializer jsonSerializer = new JsonSerializer(outputStream);
      try {
         serializeFn.serialize(value, jsonSerializer);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      return outputStream.toByteArray();
   }
}
