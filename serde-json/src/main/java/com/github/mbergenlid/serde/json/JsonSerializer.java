package com.github.mbergenlid.serde.json;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.mbergenlid.serde.Serialize;
import com.github.mbergenlid.serde.Serializer;
import com.github.mbergenlid.serde.ThrowingConsumer;

import java.io.IOException;
import java.io.OutputStream;

public class JsonSerializer implements Serializer<IOException> {

   private final OutputStream out;

   public JsonSerializer(OutputStream outputStream) {
      this.out = outputStream;
   }

   public void serializeBool(boolean value) throws IOException {
      out.write(value ? "true".getBytes() : "false".getBytes());
   }

   public void serializeByte(byte value) throws IOException {
      serializeLong(value);
   }

   @Override
   public void serializeShort(short value) throws IOException {
      serializeInt(value);
   }

   @Override
   public void serializeInt(int value) throws IOException {
      serializeLong(value);
   }

   @Override
   public void serializeLong(long value) throws IOException {
      out.write(Long.toString(value).getBytes());
   }

   @Override
   public void serializeFloat(float value) throws IOException {
      if(Float.isInfinite(value)) {
         out.write('"');
         out.write(Float.toString(value).getBytes(UTF_8));
         out.write('"');
      } else {
         out.write(Float.toString(value).getBytes(UTF_8));
      }
   }

   @Override
   public void serializeDouble(double value) throws IOException {
      if(Double.isInfinite(value)) {
         out.write('"');
         out.write(Double.toString(value).getBytes(UTF_8));
         out.write('"');
      } else {
         out.write(Double.toString(value).getBytes(UTF_8));
      }
   }

   @Override
   public void serializeString(String value) throws IOException {
      out.write('"');

      for(byte b : value.getBytes(UTF_8)) {
         try {
            if (b >= 0 && b < 32) {
               switch (b) {
                  case 0x0D: out.write("\\r".getBytes()); break;
                  case 0x08: out.write("\\b".getBytes()); break;
                  case 0x0C: out.write("\\f".getBytes()); break;
                  case 0x0A: out.write("\\n".getBytes()); break;
                  case 0x09: out.write("\\t".getBytes()); break;
                  default:
                     out.write("\\u".getBytes());
                     out.write(String.format("%04X", b).getBytes());
               }
            } else {
               if (b == '"') {
                  out.write('\\');
               } else if (b == '\\') {
                  out.write('\\');
               }
               out.write(b);
            }
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
      out.write('"');
   }

   @Override
   public void serializeSeq(ThrowingConsumer<SerializeSeq<IOException>, IOException> seq) throws IOException {
      out.write('[');
      seq.accept(new Seq());
      out.write(']');
   }

   @Override
   public void serializeRecord(
      String name, ThrowingConsumer<SerializeRecord<IOException>, IOException> record
   ) throws IOException {
      out.write('{');
      record.accept(new Record());
      out.write('}');
   }

   private class Seq implements SerializeSeq<IOException> {
      private boolean empty = true;

      @Override
      public void serializeElement(Serialize value) throws IOException {
         if(empty) {
            empty = false;
         } else {
            out.write(',');
         }
         value.serialize(JsonSerializer.this);
      }

   }

   private class Record implements SerializeRecord<IOException> {
      private boolean empty = true;

      @Override
      public SerializeRecord<IOException> serializeField(String name, Serialize value) throws IOException {
         if(empty) {
            empty = false;
         } else {
            out.write(',');
         }
         JsonSerializer.this.serializeString(name);
         out.write(':');
         value.serialize(JsonSerializer.this);
         return this;
      }
   }
}
