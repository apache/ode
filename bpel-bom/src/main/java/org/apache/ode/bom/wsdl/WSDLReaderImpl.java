package org.apache.ode.bom.wsdl;

import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;

/**
 * Little hack to solve the disfunctional WSDL4J extension mechanism. Without this,
 * WSDL4J will attempt to do Class.forName to get the WSDLFactory, which will break
 * if WSDL4J is loaded from a parent class-loader (as it often is, e.g. in ServiceMix).
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
class WSDLReaderImpl extends com.ibm.wsdl.xml.WSDLReaderImpl {

    private WSDLFactory _localFactory;
    
    WSDLReaderImpl(WSDLFactory factory) {
        _localFactory = factory;
    }
    
    @Override
    protected WSDLFactory getWSDLFactory() throws WSDLException {
        return _localFactory;
    }

    
}
