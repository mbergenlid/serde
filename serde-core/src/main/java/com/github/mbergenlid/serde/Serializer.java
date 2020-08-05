package com.github.mbergenlid.serde;

public interface Serializer<E extends Exception> {

   void serializeBool(boolean value) throws E;

   void serializeByte(byte value) throws E;

   void serializeShort(short value) throws E;

   void serializeInt(int value) throws E;

   void serializeLong(long value) throws E;

   void serializeFloat(float value) throws E;

   void serializeDouble(double value) throws E;

   void serializeString(String value) throws E;

   void serializeSeq(ThrowingConsumer<SerializeSeq<E>, E> seq) throws E;

   interface SerializeSeq<E extends Exception> {
      void serializeElement(Serialize value) throws E;
   }

   void serializeRecord(String name, ThrowingConsumer<SerializeRecord<E>, E> record) throws E;

   interface SerializeRecord<E extends Exception> {
      SerializeRecord<E> serializeField(String name, Serialize value) throws E;
   }

   //   void serializeMap(Function<SerializeMap<E>, SerializeMap<E>> map) throws E;
//
//   interface SerializeMap<E extends Exception> {
//      void serializeField() throws E;
//   }
}
