package com.github.mbergenlid.serde.impl;

import com.github.mbergenlid.serde.Serialize;
import com.github.mbergenlid.serde.Serializer;

import java.util.List;

public class SerdeList  {

   public static <T> Serialize wrap(List<T> list, Serde.SerializeFunction<T> elementSerializer) {
      return Serde.wrap(list, new Serde.SerializeFunction<>() {
         @Override
         public <E extends Exception> void serialize(List<T> value, Serializer<E> serializer) throws E {
            serializer.serializeSeq(seq -> {
               for (T v : value) {
                  seq.serializeElement(Serde.wrap(v, elementSerializer));
               }
            });
         }
      });
   }
}
