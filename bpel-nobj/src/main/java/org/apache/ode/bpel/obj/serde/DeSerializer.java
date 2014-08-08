package org.apache.ode.bpel.obj.serde;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OProcessWrapper;
import org.apache.ode.bpel.obj.migrate.ObjectTraverser;
import org.apache.ode.bpel.obj.migrate.OmUpgradeVisitor;
import org.apache.ode.bpel.obj.serde.OmSerdeFactory.SerializeFormat;

public class DeSerializer {
	private OProcessWrapper wrapper = new OProcessWrapper();
	private InputStream is;
	public DeSerializer(InputStream is){
		this.is = is;
		deserializeHeader();
	}
	
	public DeSerializer() {
	}

	public void serialize(OutputStream out, OProcess process){
		serialize(out, process, OmSerdeFactory.FORMAT_SERIALIZED_DEFAULT);
	}
	public void serialize(OutputStream out, OProcess process, SerializeFormat format){
		wrapper.setCompileTime(System.currentTimeMillis());
		wrapper.setProcess(process);
		wrapper.setFormat(format);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(wrapper.getMagic());
			oos.writeObject(wrapper.getFormat());
			oos.writeLong(wrapper.getCompileTime());
			oos.writeObject(wrapper.getGuid());
			oos.writeObject(wrapper.getType());
			oos.writeObject(wrapper.getOtherHeaders());
		} catch (IOException e1) {
			SerializaionRtException e =  new SerializaionRtException("Error when serialize Headers.");
			e.initCause(e1);
			throw e;
		}
		OmSerdeFactory factory = new OmSerdeFactory();
		factory.setFormat(format);
		OmSerializer serializer = factory.createOmSerializer(out, wrapper.getProcess());
		serializer.serialize();
	}
	
	private void deserializeHeader(){
		try {
			ObjectInputStream ois = new ObjectInputStream(is);
			wrapper = new OProcessWrapper();
			wrapper.setMagic((byte[])ois.readObject());
			wrapper.setFormat((SerializeFormat)ois.readObject());
			wrapper.setCompileTime(ois.readLong());
			wrapper.setGuid((String)ois.readObject());
			wrapper.setType((QName)ois.readObject());
			wrapper.setOtherHeaders((Map<String, Object>) (ois.readObject()));
			wrapper.checkValid();
		} catch (Exception e1) {
			SerializaionRtException e = new SerializaionRtException("Error when reading Headers during deseriazation");
			e.initCause(e1);
			throw e;
		}
	}
	public OProcess deserialize(){
		OmSerdeFactory factory = new OmSerdeFactory();
		factory.setFormat(wrapper.getFormat());
    	OmDeserializer de = factory.createOmDeserializer(is);
    	OProcess process = de.deserialize();
		//upgrade
		OmUpgradeVisitor upgrader = new OmUpgradeVisitor();
		ObjectTraverser traverser = new ObjectTraverser();
		traverser.accept(upgrader);
		traverser.traverseObject(process);
		return process;
	}
	
	public OProcessWrapper getWrapper() {
		return wrapper;
	}
	public void setWrapper(OProcessWrapper wrapper) {
		this.wrapper = wrapper;
	}

}
