package com.github.mbergenlid.serde;

public interface Visitor<T> {

   default T visitBool(boolean value) {
      throw new UnsupportedOperationException("");
   }

   default T visitByte(byte value) {
      throw new UnsupportedOperationException("");
   }

   default T visitShort(short value) {
      throw new UnsupportedOperationException("");
   }

   default T visitInt(int value) {
      throw new UnsupportedOperationException("");
   }

   default T visitLong(long value) {
      throw new UnsupportedOperationException("");
   }

   default T visitFloat(float value) {
      throw new UnsupportedOperationException("");
   }

   default T visitDouble(double value) {
      throw new UnsupportedOperationException("");
   }

   default T visitString(String value) {
      throw new UnsupportedOperationException("");
   }


}
