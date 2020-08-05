package com.github.mbergenlid.serde.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class JsonTokenizer implements Iterator<JsonTokenizer.Token> {

   public static final Token START_ARRAY = new Token(TokenType.START_ARRAY);
   public static final Token END_ARRAY = new Token(TokenType.END_ARRAY);
   public static final Token START_OBJECT = new Token(TokenType.START_ARRAY);
   public static final Token END_OBJECT = new Token(TokenType.END_OBJECT);
   public static final Token COMMA = new Token(TokenType.COMMA);
   public static final Token COLON = new Token(TokenType.COLON);
   public static final Token TRUE = new Token(TokenType.TRUE);
   public static final Token FALSE = new Token(TokenType.FALSE);
   private final UndoableReader reader;
   private Token nextToken;

   public JsonTokenizer(Reader reader) {
      this.reader = new UndoableReader(reader);
   }

   @Override
   public boolean hasNext() {
      if(nextToken != null) {
         return true;
      } else {
         nextToken = readNextToken();
         return nextToken != null;
      }
   }

   private Token readNextToken() {
      try {
         int c;
         //noinspection StatementWithEmptyBody
         while((c = reader.read()) == ' ') { }
         switch (c) {
            case '[': return START_ARRAY;
            case ']': return END_ARRAY;
            case '{': return START_OBJECT;
            case '}': return END_OBJECT;
            case ',': return COMMA;
            case ':': return COLON;
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
               reader.undo();
               return readNumber();
            case '"':
               return readString();
            case 't':
               if(readString("rue")) {
                  return TRUE;
               } else  {
                  throw new TokenException("Unknown token while reading true");
               }
            case 'f':
               if(readString("alse")) {
                  return FALSE;
               } else  {
                  throw new TokenException("Unknown token while reading false");
               }
            case -1: return null;
         }
         throw new TokenException();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private boolean readString(String s) throws IOException {
      for(char c : s.toCharArray()) {
         int characterRead = reader.read();
         if(characterRead != c) {
            return false;
         }
      }
      return true;
   }

   private Token readString() throws IOException {
      int c = reader.read();
      final StringBuilder builder = new StringBuilder();
      while(c != '"' && c != -1) {
         if(c == '\\') {
            int nextChar = reader.read();
            switch (nextChar) {
               case '"':
                  builder.append('"'); break;
               case 'u':
                  int codePoint = 0;
                  for(int i = 0; i < 4; i++) {
                     int c1 = reader.read();
                     if(c1 >= '0' && c1 <= '9') {
                        codePoint = codePoint*16 + (c1 - '0');
                     } else if(c1 >= 'A' && c1 <= 'F') {
                        codePoint = codePoint*16 + (c1 - 'A' + 10);
                     } else {
                        throw new TokenException();
                     }
                  }
                  builder.appendCodePoint(codePoint);
                  break;
               case 'r':
                  builder.append('\r');
                  break;
               case 'f':
                  builder.append('\f');
                  break;
               case 'n':
                  builder.append('\n');
                  break;
               case 't':
                  builder.append('\t');
                  break;
               case 'b':
                  builder.append('\b');
                  break;
               case '\\':
                  builder.append('\\');
                  break;
               default:
                  throw new TokenException("Unknown escape \\" + (char)nextChar);
            }
         } else {
            builder.append((char)c);
         }
         c = reader.read();
      }
      if(c == -1) {
         throw new TokenException();
      }
      return Token.string(builder.toString());
   }

   private Token readNumber() throws IOException {
      return readInteger();
   }

   private Token readInteger() throws IOException {
      int original = reader.read();
      int value = original == '-' ? 0 : (original - '0');
      int v = reader.read();
      while(v >= '0' && v <= '9') {
         value = value*10 + (v - '0');
         v = reader.read();
      }
      int scale = 0;
      if(v == '.') {
         v = reader.read();
         while(v >= '0' && v <= '9') {
            scale += 1;
            value = value*10 + (v - '0');
            v = reader.read();
         }
      }
      reader.undo();
      if(original == '-') {
         value *= -1;
      }
      return Token.number(value, scale);
   }

   public Token next() {
      if(!hasNext()) {
         throw new NoSuchElementException();
      }
      final var nextToken = this.nextToken;
      this.nextToken = null;
      return nextToken;
   }

   public static class Token {
      private final TokenType name;

      private Token(TokenType name) {
         this.name = name;
      }

      public static Token integer(int value) {
         return new NumberToken(value, 0);
      }

      public static Token number(int unscaled, int scale) {
         return new NumberToken(unscaled, scale);
      }

      public static Token string(String value) {
         return new StringToken(value);
      }

      public String toString() {
         return name.toString();
      }

      @Override
      public boolean equals(Object obj) {
         return obj.getClass().equals(Token.class) && ((Token)obj).name == this.name;
      }

      public TokenType type() {
         return name;
      }

      public String stringValue() {
         assert name == TokenType.STRING;
         return ((StringToken)this).value;
      }

      private static class StringToken extends Token {

         private final String value;

         private StringToken(String value) {
            super(TokenType.STRING);
            this.value = value;
         }

         @Override
         public boolean equals(Object obj) {
            return obj.getClass().equals(StringToken.class) &&
               this.value.equals(((StringToken)obj).value);
         }

         @Override
         public String toString() {
            return "STRING(\"" + value + "\")";
         }
      }
   }

   private static class NumberToken extends Token {

      private final int value;
      private final int scale;

      private NumberToken(int value, int scale) {
         super(TokenType.INTEGER);
         this.value = value;
         this.scale = scale;
      }

      @Override
      public boolean equals(Object obj) {
         return obj.getClass().equals(NumberToken.class) &&
            this.value == ((NumberToken)obj).value && this.scale == ((NumberToken)obj).scale;
      }
   }

   public enum TokenType {
      START_ARRAY,
      END_ARRAY,
      START_OBJECT,
      END_OBJECT,
      COMMA,
      COLON,
      INTEGER,
      STRING,
      TRUE,
      FALSE
   }

   private class TokenException extends RuntimeException {

      public TokenException() {
         super();
      }

      public TokenException(String message) {
         super(message);
      }
   }

   private static class UndoableReader {
      private final Reader reader;
      private int head;
      private boolean useHead = false;

      private UndoableReader(Reader reader) {
         this.reader = reader;
      }

      public int read() throws IOException {
         if(useHead) {
            useHead = false;
            return head;
         } else {
            return head = this.reader.read();
         }
      }

      public int read(char[] buf) throws IOException {
         return reader.read(buf);
      }

      public void undo() {
         assert !useHead;
         useHead = true;
      }
   }
}
