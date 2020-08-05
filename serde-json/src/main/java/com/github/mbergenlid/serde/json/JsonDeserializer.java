package com.github.mbergenlid.serde.json;

import com.github.mbergenlid.serde.Deserializer;
import com.github.mbergenlid.serde.Visitor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JsonDeserializer implements Deserializer<RuntimeException> {

   private final JsonTokenizer tokenizer;

   public JsonDeserializer(ByteArrayInputStream stream) {
      this.tokenizer = new JsonTokenizer(new BufferedReader(new InputStreamReader(stream)));
   }

   @Override
   public <T, V extends Visitor<T>> T deserializeBool(V visitor) {
      final JsonTokenizer.Token token = tokenizer.next();
      switch (token.type()) {
         case TRUE:
            return visitor.visitBool(true);
         case FALSE:
            return visitor.visitBool(false);
         default:
            throw new RuntimeException();
      }
   }

   @Override
   public <T, V extends Visitor<T>> T deserializeString(V visitor) {
      final JsonTokenizer.Token token = tokenizer.next();
      if(token.type() == JsonTokenizer.TokenType.STRING) {
         return visitor.visitString(token.stringValue());
      } else {
         throw new RuntimeException();
      }
   }
}
