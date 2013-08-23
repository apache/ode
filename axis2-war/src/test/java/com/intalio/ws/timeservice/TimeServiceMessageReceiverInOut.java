
/**
 * TimeServiceMessageReceiverInOut.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.1  Built on : Oct 19, 2009 (10:59:00 EDT)
 */
        package com.intalio.ws.timeservice;

        /**
        *  TimeServiceMessageReceiverInOut message receiver
        */

        public class TimeServiceMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        TimeServiceSkeleton skel = (TimeServiceSkeleton)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)){

        

            if("getCityTime".equals(methodName)){
                
                com.intalio.ws.timeservice.GetCityTimeResponse getCityTimeResponse1 = null;
	                        com.intalio.ws.timeservice.GetCityTime wrappedParam =
                                                             (com.intalio.ws.timeservice.GetCityTime)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    com.intalio.ws.timeservice.GetCityTime.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               getCityTimeResponse1 =
                                                   
                                                   
                                                         skel.getCityTime(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), getCityTimeResponse1, false);
                                    } else 

            if("getUTCTime".equals(methodName)){
                
                com.intalio.ws.timeservice.GetUTCTimeResponse getUTCTimeResponse3 = null;
	                        com.intalio.ws.timeservice.GetUTCTime wrappedParam =
                                                             (com.intalio.ws.timeservice.GetUTCTime)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    com.intalio.ws.timeservice.GetUTCTime.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               getUTCTimeResponse3 =
                                                   
                                                   
                                                         skel.getUTCTime(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), getUTCTimeResponse3, false);
                                    
            } else {
              throw new java.lang.RuntimeException("method not found");
            }
        

        newMsgContext.setEnvelope(envelope);
        }
        }
        catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        
        //
            private  org.apache.axiom.om.OMElement  toOM(com.intalio.ws.timeservice.GetCityTime param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(com.intalio.ws.timeservice.GetCityTime.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.intalio.ws.timeservice.GetCityTimeResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(com.intalio.ws.timeservice.GetCityTimeResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.intalio.ws.timeservice.GetUTCTime param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(com.intalio.ws.timeservice.GetUTCTime.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.intalio.ws.timeservice.GetUTCTimeResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(com.intalio.ws.timeservice.GetUTCTimeResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.intalio.ws.timeservice.GetCityTimeResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.intalio.ws.timeservice.GetCityTimeResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private com.intalio.ws.timeservice.GetCityTimeResponse wrapgetCityTime(){
                                com.intalio.ws.timeservice.GetCityTimeResponse wrappedElement = new com.intalio.ws.timeservice.GetCityTimeResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.intalio.ws.timeservice.GetUTCTimeResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.intalio.ws.timeservice.GetUTCTimeResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private com.intalio.ws.timeservice.GetUTCTimeResponse wrapgetUTCTime(){
                                com.intalio.ws.timeservice.GetUTCTimeResponse wrappedElement = new com.intalio.ws.timeservice.GetUTCTimeResponse();
                                return wrappedElement;
                         }
                    


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

        try {
        
                if (com.intalio.ws.timeservice.GetCityTime.class.equals(type)){
                
                           return com.intalio.ws.timeservice.GetCityTime.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.intalio.ws.timeservice.GetCityTimeResponse.class.equals(type)){
                
                           return com.intalio.ws.timeservice.GetCityTimeResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.intalio.ws.timeservice.GetUTCTime.class.equals(type)){
                
                           return com.intalio.ws.timeservice.GetUTCTime.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.intalio.ws.timeservice.GetUTCTimeResponse.class.equals(type)){
                
                           return com.intalio.ws.timeservice.GetUTCTimeResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }



    

        /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
        return returnMap;
        }

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
    