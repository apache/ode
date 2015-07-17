package org.apache.ode.bpel.obj.migrate;

import java.io.InputStream;

import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.serde.OmDeserializer;
import org.apache.ode.bpel.obj.serde.SerializaionRtException;

/**
 * Adaptor for legacy Omodel serializer. Used to deserialize and migrate 
 * old format compiled bpels.
 * @see Serializer
 */
public class LegacySerializerAdapter implements OmDeserializer {
	private Serializer serializer;

	public LegacySerializerAdapter(InputStream in) {
		serializer = new Serializer();
		serializer._inputStream = in;
	}

	/**
	 * Read the old OModel and migrate it to new omodel.
	 */
	@Override
	public OProcess deserialize() throws SerializaionRtException {
		org.apache.ode.bpel.o.OProcess old;
		try {
			old = serializer.readOProcess();
		} catch (Exception e) {
			throw new SerializaionRtException(
					"Error when deserializing old OModle classes");
		}
		//migrate to new OModel
		OmOld2new mig = new OmOld2new();
		ObjectTraverser mtraverse = new ObjectTraverser();
		mtraverse.accept(mig);
		OProcess migrated = (OProcess) mtraverse.traverseObject(old);
		return migrated;
	}
}
