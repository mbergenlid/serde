package com.github.mbergenlid.serde.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.strings;

import com.github.mbergenlid.serde.impl.Serde;
import org.junit.Test;
import org.quicktheories.generators.SourceDSL;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonTokenizerTest {

   @Test
   public void testVariousTokens() {
      final var tokens = fromString("[]{},:1234truefalse");
      assertThat(tokens).isEqualTo(List.of(
         JsonTokenizer.START_ARRAY,
         JsonTokenizer.END_ARRAY,
         JsonTokenizer.START_OBJECT,
         JsonTokenizer.END_OBJECT,
         JsonTokenizer.COMMA,
         JsonTokenizer.COLON,
         JsonTokenizer.Token.integer(1234),
         JsonTokenizer.TRUE,
         JsonTokenizer.FALSE
      ));
   }

   @Test
   public void testNumbers() {
      final var tokens = fromString("-1234 1234 1.1234");
      assertThat(tokens).isEqualTo(
         List.of(
            JsonTokenizer.Token.integer(-1234),
            JsonTokenizer.Token.integer(1234),
            JsonTokenizer.Token.number(11234, 4)
         )
      );
   }

   @Test
   public void testNumbers2() {
      final var tokens = fromString("1234,");
      assertThat(tokens).isEqualTo(
         List.of(
            JsonTokenizer.Token.integer(1234),
            JsonTokenizer.COMMA
         )
      );
   }

   @Test
   public void testStrings() {
      qt().forAll(strings().ascii().ofLengthBetween(0,20)).checkAssert(s -> {
         final String jsonInput = new String(SerdeJson.toBytes(s, Serde::serializeString), StandardCharsets.UTF_8);
         final var tokens = fromString(jsonInput);
         assertThat(tokens).as("JSON Input = %s", jsonInput).isEqualTo(
            List.of(
               JsonTokenizer.Token.string(s)
            )
         );
      });
   }

   private List<JsonTokenizer.Token> fromString(String s) {
      final JsonTokenizer jsonTokenizer = new JsonTokenizer(
         new InputStreamReader(
            new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))
         )
      );
      final List<JsonTokenizer.Token> tokens = new ArrayList<>();
      for (JsonTokenizer.Token token : (Iterable<JsonTokenizer.Token>) () -> jsonTokenizer) {
         tokens.add(token);
      }
      return tokens;
   }
}
