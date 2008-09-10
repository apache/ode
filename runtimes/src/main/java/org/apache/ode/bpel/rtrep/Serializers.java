package org.apache.ode.bpel.rtrep;

import org.apache.ode.bpel.rapi.Serializer;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
        try {
            Class serializerClass = Class.forName("org.apache.ode.bpel.rtrep.v" + version + ".Serializer");
            Constructor cstrct = serializerClass.getConstructor(Long.TYPE);
            return (Serializer) cstrct.newInstance(System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException("Couldn't build an OModel serializer for version " + version);
        }
    }
    public static Serializer getVersion(InputStream stream, int version) throws IOException {
        try {
            Class serializerClass = Class.forName("org.apache.ode.bpel.rtrep.v" + version + ".Serializer");
            Constructor cstrct = serializerClass.getConstructor(InputStream.class);
            return (Serializer) cstrct.newInstance(stream);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't build an OModel serializer for version " + version);
        }
    }

}
