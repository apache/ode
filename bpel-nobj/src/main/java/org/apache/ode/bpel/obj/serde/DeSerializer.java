package org.apache.ode.bpel.obj.serde;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OProcessWrapper;
import org.apache.ode.bpel.obj.migrate.ObjectTraverser;
import org.apache.ode.bpel.obj.migrate.OmUpgradeVisitor;
import org.apache.ode.bpel.obj.migrate.UpgradeChecker;
import org.apache.ode.bpel.obj.serde.OmSerdeFactory.SerializeFormat;

/**
 * Main Serializer and Deserializer of OModel classes. This class handles different
 * serialization format. (De)Serialize header information. It will upgrade the deserialized
 * OProcess to newest version. And write it back if the write back file is specified.
 * @see OmSerdeFactory
 */
public class DeSerializer {
    private static final Log __log = LogFactory.getLog(DeSerializer.class);

    private OProcessWrapper wrapper = new OProcessWrapper();
	private InputStream is;
	private File writeBackFile;

	public DeSerializer(InputStream is) {
		this.is = new BufferedInputStream(is);
		deserializeHeader();
	}

	public DeSerializer() {
	}

	public void serialize(OutputStream out, OProcess process) {
		serialize(out, process, OmSerdeFactory.FORMAT_SERIALIZED_DEFAULT);
	}

	public void serialize(OutputStream out, OProcess process,
			SerializeFormat format) {
		wrapper.setCompileTime(System.currentTimeMillis());
		wrapper.setProcess(process);
		wrapper.setFormat(format);
		serialize(out);
	}
	private void serialize(OutputStream out){
		try {
	        DataOutputStream dos = new DataOutputStream(out);
	        dos.write(wrapper.getMagic());
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(wrapper.getFormat());
			oos.writeLong(wrapper.getCompileTime());
			oos.writeObject(wrapper.getGuid());
			oos.writeObject(wrapper.getType());
			oos.writeObject(wrapper.getOtherHeaders());
		} catch (IOException e1) {
			SerializaionRtException e = new SerializaionRtException(
					"Error when serialize Headers.");
			e.initCause(e1);
			throw e;
		}
		OmSerdeFactory factory = new OmSerdeFactory();
		factory.setFormat(wrapper.getFormat());
		OmSerializer serializer = factory.createOmSerializer(out,
				wrapper.getProcess());
		serializer.serialize();
	}

	private void deserializeHeader() {
		try {
	        DataInputStream oin = new DataInputStream(is);
	        oin.mark(OProcessWrapper.CURRENT_MAGIC_NUMBER.length + 2);
	        byte[] magic = new byte[OProcessWrapper.CURRENT_MAGIC_NUMBER.length];
	        oin.read(magic, 0, magic.length);
	        if (Arrays.equals(Serializer.MAGIC_NUMBER_OFH_20040908, magic) ||
	        		Arrays.equals(Serializer.MAGIC_NUMBER_OFH_20061101, magic)){
	        	oin.reset();
	        	Serializer serializer = new Serializer(is);
	        	wrapper.setMagic(magic);
	        	wrapper.setGuid(serializer.guid);
	        	wrapper.setCompileTime(serializer.compileTime);
	        	wrapper.setType(serializer.type);
	        	wrapper.setFormat(SerializeFormat.FORMAT_SERIALIZED_LEGACY);
	        }else{
		        ObjectInputStream ois = new ObjectInputStream(is);
				wrapper = new OProcessWrapper();
				wrapper.setMagic(magic);
				wrapper.setFormat((SerializeFormat) ois.readObject());
				wrapper.setCompileTime(ois.readLong());
				wrapper.setGuid((String) ois.readObject());
				wrapper.setType((QName) ois.readObject());
				wrapper.setOtherHeaders((Map<String, Object>) (ois.readObject()));
				wrapper.checkValid();
	        }
		} catch (Exception e1) {
			SerializaionRtException e = new SerializaionRtException(
					"Error when reading Headers during deseriazation");
			e.initCause(e1);
			throw e;
		}
	}

	public OProcess deserialize() {
		OmSerdeFactory factory = new OmSerdeFactory();
		factory.setFormat(wrapper.getFormat());
		OmDeserializer de = factory.createOmDeserializer(is);
		OProcess process = de.deserialize();
		wrapper.setProcess(process);
		//upgrade
		UpgradeChecker checker = new UpgradeChecker();
		ObjectTraverser traverser = new ObjectTraverser();
		traverser.accept(checker);
		traverser.traverseObject(process);
		if (!checker.isNewest()) {
			OmUpgradeVisitor upgrader = new OmUpgradeVisitor();
			traverser = new ObjectTraverser();
			traverser.accept(upgrader);
			traverser.traverseObject(process);
			if (writeBackFile != null){
				writeBack();
			}
		}
		return process;
	}

	private void writeBack() {
		byte[] magic = wrapper.getMagic();
        if (Arrays.equals(Serializer.MAGIC_NUMBER_OFH_20040908, magic) ||
        		Arrays.equals(Serializer.MAGIC_NUMBER_OFH_20061101, magic)){
        	//upgrade to new omodel magic and format
        	wrapper.setMagic(OProcessWrapper.CURRENT_MAGIC_NUMBER);
        	wrapper.setFormat(OmSerdeFactory.FORMAT_SERIALIZED_DEFAULT);
        }
        OutputStream wbStream;
		try {
			if (writeBackFile.exists()){
				writeBackFile.renameTo(new File(writeBackFile.getAbsolutePath()+ ".bak"));
			}
			wbStream = new FileOutputStream(writeBackFile);
			serialize(wbStream);
		} catch (FileNotFoundException e) {
			__log.info("Error when write back upgraded process. file not found");
		}
	}

	public OProcessWrapper getWrapper() {
		return wrapper;
	}

	public void setWrapper(OProcessWrapper wrapper) {
		this.wrapper = wrapper;
	}

	public void setWriteBackFile(File writeBack) {
		this.writeBackFile = writeBack;
	}
}
