package org.apache.ode.bpel.rtrep;

import org.apache.ode.bpel.rapi.Serializer;

public class Serializers {

    public static Serializer getLatest() {
        return new org.apache.ode.bpel.rtrep.v2.Serializer(System.currentTimeMillis());
    }

    public static Serializer getVersion(int version) {
        // TODO switch on the version when we'll have more than one
        return getLatest();
    }
}
