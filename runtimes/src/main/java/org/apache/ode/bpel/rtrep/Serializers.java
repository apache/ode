package org.apache.ode.bpel.rtrep;

import org.apache.ode.bpel.rapi.Serializer;

import java.io.InputStream;
import java.io.IOException;

/**
 * Factory to instantiate OModel serializers/deserializers for a specific version of the model. It's
 * the entry point to reading and writing the ODE internal object model.
 */
public class Serializers {

    public static Serializer getLatest() {
        return new org.apache.ode.bpel.rtrep.v2.Serializer(System.currentTimeMillis());
    }
    public static Serializer getLatest(InputStream stream) throws IOException {
        return new org.apache.ode.bpel.rtrep.v2.Serializer(stream);
    }


    public static Serializer getVersion(int version) {
        // TODO switch on the version when we'll have more than one
        return getLatest();
    }
    public static Serializer getVersion(InputStream stream, int version) throws IOException {
        // TODO switch on the version when we'll have more than one
        return getLatest(stream);
    }

}
