package com.github.mbergenlid.serde.json.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.booleans;
import static org.quicktheories.generators.SourceDSL.doubles;
import static org.quicktheories.generators.SourceDSL.floats;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.lists;
import static org.quicktheories.generators.SourceDSL.longs;
import static org.quicktheories.generators.SourceDSL.strings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mbergenlid.serde.Serialize;
import com.github.mbergenlid.serde.Serializer;
import com.github.mbergenlid.serde.impl.Serde;
import com.github.mbergenlid.serde.impl.SerdeList;
import com.github.mbergenlid.serde.json.SerdeJson;
import org.junit.Test;
import org.quicktheories.core.Gen;

import java.util.function.Consumer;

public class SerializerTest {

   @Test
   public void serializeBool() {
      qt().forAll(booleans().all())
         .checkAssert(comparator(Serde::serializeBool));
   }

   private <T> Consumer<T> comparator(Serde.SerializeFunction<T> serializeFn) {
      return value -> assertThat(SerdeJson.toBytes(value, serializeFn)).isEqualTo(jacksonValue(value));
   }

   private <T> void comparator(Serialize serialize, T value) {
      assertThat(SerdeJson.toBytes(serialize)).isEqualTo(jacksonValue(value));
   }

   @Test
   public void serializeByte() {
      qt().forAll(integers().between(Byte.MIN_VALUE, Byte.MAX_VALUE).map(Integer::byteValue))
         .checkAssert(comparator(Serde::serializeByte));
   }

   @Test
   public void serializeShort() {
      qt().forAll(integers().between(Short.MIN_VALUE, Short.MAX_VALUE).map(Integer::shortValue))
         .checkAssert(comparator(Serde::serializeShort));
   }

   @Test
   public void serializeInt() {
      qt().forAll(integers().all().map(Integer::intValue))
         .checkAssert(comparator(Serde::serializeInt));
   }

   @Test
   public void serializeLong() {
      qt().forAll(longs().all().map(Long::longValue))
         .checkAssert(comparator(Serde::serializeLong));
   }

   //TODO look at more codepoints.
   @Test
   public void serializeString() {
      qt().forAll(strings().betweenCodePoints(0, 0xD7FF).ofLengthBetween(0, 20))
//         .check(value -> {
//            if(!Arrays.equals(toString(s -> s.serializeString(value)), jacksonValue(value))) {
//               System.out.println("=========");
//               printCodePoints(value);
//               printBytes(toString(s -> s.serializeString(value)));
//               printBytes(jacksonValue(value));
//            }
//            return Arrays.equals(toString(s -> s.serializeString(value)), jacksonValue(value));
//         });
         .checkAssert(comparator(Serde::serializeString));
   }

   @Test
   public void serializeFloat() {
      qt().forAll(floats().any()).checkAssert(comparator(Serde::serializeFloat));
   }

   @Test
   public void serializeDouble() {
      qt().forAll(doubles().any()).checkAssert(comparator(Serde::serializeDouble));
   }

   @Test
   public void serializeSeq() {
      qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 20))
         .checkAssert(list -> comparator(SerdeList.wrap(list, Serde::serializeInt), list));
   }

   @Test
   public void serializeRecord() {
      qt().forAll(myRecords())
         .checkAssert(value -> comparator(value, value));
   }

   private Gen<MyRecord> myRecords() {
      return strings().betweenCodePoints(0, 0xD7FF).ofLengthBetween(0, 20).zip(integers().all(), MyRecord::new);
   }

   @Test
   public void serializeOtherRecord() {
      qt().forAll(doubles().any().zip(myRecords(), MyOtherRecord::new))
         .checkAssert(value -> comparator(value, value));
   }

   public static class MyRecord implements Serialize {
      @JsonProperty("name")
      private final String name;
      @JsonProperty("someNumber")
      private final int someNumber;

      private MyRecord(String name, int someNumber) {
         this.name = name;
         this.someNumber = someNumber;
      }

      @Override
      public <E extends Exception> void serialize(Serializer<E> serializer) throws E {
         serializer.serializeRecord(MyRecord.class.getName(), rec -> rec
            .serializeField("name", Serde.wrap(name, Serde::serializeString))
            .serializeField("someNumber", Serde.wrap(someNumber, Serde::serializeInt))
         );
      }

      @Override
      public String toString() {
         return "MyRecord{" +
            "name='" + name + '\'' +
            ", someNumber=" + someNumber +
            '}';
      }
   }

   public static class MyOtherRecord implements Serialize {
      @JsonProperty("dField")
      private final double dField;
      @JsonProperty("myRecord")
      private final MyRecord myRecord;

      public MyOtherRecord(double dField, MyRecord myRecord) {
         this.dField = dField;
         this.myRecord = myRecord;
      }

      @Override
      public <E extends Exception> void serialize(Serializer<E> serializer) throws E {
         serializer.serializeRecord(MyOtherRecord.class.getName(), rec -> rec
            .serializeField("dField", Serde.wrap(dField, Serde::serializeDouble))
            .serializeField("myRecord", myRecord)
         );
      }

      @Override
      public String toString() {
         return "MyOtherRecord{" +
            "dField=" + dField +
            ", myRecord=" + myRecord +
            '}';
      }
   }

   private static void printBytes(byte[] value) {
      System.out.print("{");
      for (byte b : value) {
         System.out.print(String.format("%x, ", b));
      }
      System.out.println("};");
   }

   private static void printCodePoints(String value) {
      System.out.print("{");
      value.codePoints().forEach(cp -> System.out.print(String.format("%x, ", cp)));
      System.out.println("};");
   }

   private byte[] jacksonValue(Object value) {
      try {
         return new ObjectMapper().writeValueAsBytes(value);
      } catch (JsonProcessingException e) {
         throw new RuntimeException(e);
      }
   }
}
