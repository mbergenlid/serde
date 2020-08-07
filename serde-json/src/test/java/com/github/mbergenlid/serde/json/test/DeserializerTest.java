package com.github.mbergenlid.serde.json.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.strings;

import com.github.mbergenlid.serde.Visitor;
import com.github.mbergenlid.serde.impl.Serde;
import com.github.mbergenlid.serde.json.JsonDeserializer;
import com.github.mbergenlid.serde.json.SerdeJson;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class DeserializerTest {

   @Test
   public void deserializeBool() {
      final ByteArrayInputStream stream = new ByteArrayInputStream("true false".getBytes(StandardCharsets.UTF_8));
      final JsonDeserializer deserializer = new JsonDeserializer(stream);
      assertThat(deserializer.deserializeBool(new Visitor<Boolean>() {
         @Override
         public Boolean visitBool(boolean value) {
            return value;
         }
      })).isTrue();

      assertThat(deserializer.deserializeBool(new Visitor<Boolean>() {
         @Override
         public Boolean visitBool(boolean value) {
            return value;
         }
      })).isFalse();
   }

   @Test
   public void serdeString() {
      qt().forAll(strings().ascii().ofLengthBetween(0, 20))
         .checkAssert(value -> {
            var serdeBytes = SerdeJson.toBytes(Serde.wrap(value, Serde::serializeString));
            final JsonDeserializer deserializer = new JsonDeserializer(new ByteArrayInputStream(serdeBytes));
            final String actual = deserializer.deserializeString(new Visitor<>() {
               @Override
               public String visitString(String value) {
                  return value;
               }
            });
            assertThat(actual)
               .as(
                  "Original: %s\nSerialized: %s\nDeserialized: %s",
                  printCodePoints(value),
                  printBytes(serdeBytes),
                  printCodePoints(actual)
               )
               .isEqualTo(value);
         });
   }

   @Test
   public void serdeInteger() {
      qt().forAll(integers().all()).checkAssert(value -> {
         var serdeBytes = SerdeJson.toBytes(value, Serde::serializeInt);
         final JsonDeserializer deserializer = new JsonDeserializer(new ByteArrayInputStream(serdeBytes));
         final int actual = deserializer.deserializeInt(new Visitor<>() {
            @Override
            public Integer visitInt(int value) {
               return value;
            }
         });
         assertThat(actual).isEqualTo(value);
      });
   }

   @Test
   public void tryingToDeserializeALongIntoAnInteger() {
      final JsonDeserializer deserializer = new JsonDeserializer(new ByteArrayInputStream(SerdeJson.toBytes(
         Integer.MAX_VALUE + 1L,
         Serde::serializeLong)));
      assertThatExceptionOfType(JsonDeserializer.JsonException.class)
         .isThrownBy(() -> deserializer.deserializeInt(new Visitor<Integer>() {
            @Override
            public Integer visitInt(int value) {
               return value;
            }
         }));
   }

   public static String printBytes(byte[] value) {
      final var buf = new StringBuilder();
      buf.append("{");
      for (byte b : value) {
         buf.append(String.format("%x, ", b));
      }
      buf.append("};");
      return buf.toString();
   }

   public static String printCodePoints(String value) {
      final var buf = new StringBuilder();
      buf.append("{");
      value.codePoints().forEach(cp -> buf.append(String.format("%x, ", cp)));
      buf.append("};");
      return buf.toString();
   }

}
