package org.apache.ode.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.wsdl.Definition4BPEL;
import org.apache.ode.utils.xsd.SchemaModel;
import org.apache.xerces.xni.parser.XMLEntityResolver;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A parsed collection of WSDL definitions, including BPEL-specific extensions.
 */
public class DocumentRegistry {
    private static final Log __log = LogFactory.getLog(DocumentRegistry.class);
    private static final Messages __msgs = Messages.getMessages(Messages.class);

    private final ArrayList<Definition4BPEL> _definitions = new ArrayList<Definition4BPEL>();
    private final Map<URI, byte[]> _schemas = new HashMap<URI,byte[]>();

    private SchemaModel _model;
    private XMLEntityResolver _resolver;

    public DocumentRegistry(XMLEntityResolver resolver) {
        // bogus schema to force schema creation
        _schemas.put(URI.create("http://www.apache.org/ode/bogus/namespace"),
                ("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                        + " targetNamespace=\"http://www.apache.org/ode/bogus/namespace\">"
                        + "<xsd:simpleType name=\"__bogusType__\">"
                        + "<xsd:restriction base=\"xsd:normalizedString\"/>"
                        + "</xsd:simpleType>" + "</xsd:schema>").getBytes());
        _resolver = resolver;
    }


    /**
     * Obtains an WSDL definition based on its target namespace.
     *
     * @param serviceName
     *
     * @return WSDL definition or <code>null</code> if unavailable.
     */
    public Definition4BPEL getDefinition(QName serviceName) {
        for (Definition4BPEL definition4BPEL : _definitions) {
            if (definition4BPEL.getTargetNamespace().equals(serviceName.getNamespaceURI())) {
                if (definition4BPEL.getService(serviceName) != null)
                    return definition4BPEL;
            }
        }
        return null;
    }

    public Definition4BPEL[] getDefinitions(){
        return _definitions.toArray(new Definition4BPEL[_definitions.size()]);
    }

    /**
     * Adds a WSDL definition for use in resolving MessageType, PortType,
     * Operation and BPEL properties and property aliases
     * @param def WSDL definition
     */
    @SuppressWarnings("unchecked")
    public void addDefinition(Definition4BPEL def) throws CompilationException {
        if (def == null)
            throw new NullPointerException("def=null");

        if (DocumentRegistry.__log.isDebugEnabled()) {
            DocumentRegistry.__log.debug("addDefinition(" + def.getTargetNamespace() + " from " + def.getDocumentBaseURI() + ")");
        }

        _definitions.add(def);

        // For now the schemas are never used at runtime. Check the compiler if this needs to be
        // put back in.
//        captureSchemas(def);
    }

}
