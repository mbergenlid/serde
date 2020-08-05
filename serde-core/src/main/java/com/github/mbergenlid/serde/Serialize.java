package com.github.mbergenlid.serde;

import java.io.IOException;

public interface Serialize {

   <E extends Exception> void serialize(Serializer<E> serializer) throws E;
}
