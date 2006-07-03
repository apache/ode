/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

import com.fs.pxe.bpel.pmapi.*;
import com.fs.pxe.bpel.provider.BpelManagementFacade;
import com.fs.pxe.ra.PxeConnection;
import com.fs.pxe.ra.PxeConnectionFactory;
import com.fs.pxe.ra.PxeManagedConnectionFactory;
import com.fs.utils.rmi.RMIConstants;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Demonstration of the Management API used to query and filter existing
 * processes and process instances. Simply acquires a connection to the PXE
 * engine and retrieves a ProcessManagement and InstanceManagement
 * implementation. 
 */
public class BpelManagementExample {

  public static void main(String[] argv) throws Exception {

    PxeManagedConnectionFactory pmcf = new PxeManagedConnectionFactory();
    pmcf.setURL(RMIConstants.getConnectionURL());
    PxeConnectionFactory cf = (PxeConnectionFactory)pmcf.createConnectionFactory();
    PxeConnection conn = (PxeConnection)cf.getConnection();
    BpelManagementFacade bmf =
      (BpelManagementFacade)conn.createServiceProviderSession(
          "uri:bpelProvider", BpelManagementFacade.class);

    ProcessManagement pm = bmf;
    InstanceManagement im = bmf;

    System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
             "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    DocumentBuilderFactory factory = new DocumentBuilderFactoryImpl();
    Document doc = factory.newDocumentBuilder().newDocument();
    Element elmt = doc.createElement("testnode");
    doc.appendChild(elmt);
    elmt.setTextContent("218");

    System.out.println("SETTING PROPERTY");
    pm.setProcessProperty(
            "HelloWorld.helloWorld.BpelService", new QName("http://pxe", "testprop"), "118");
    ProcessInfoDocument pid = pm.setProcessProperty(
            "HelloWorld.helloWorld.BpelService", new QName("http://pxe", "testnode"), doc);
    System.out.println(pid);

    System.out.println("PROCESSES:");
    ProcessInfoListDocument processInfoList = pm.listProcesses(
        "name=Hello* namespace=http://pxe* status=activated "
        // Add to filter on deployment date.
        // + "deployed>=2005-11-29T15:11 deployed < 2005-11-29T15:13"
        ,"name +namespace -version");
    System.out.println(processInfoList);

    System.out.println("INSTANCES:");
    InstanceInfoListDocument instanceList = im.listInstances(
        "name=Hello* namespace=http://pxe* status=completed|active "
        // Add to filter on started and last active date.
        // + "started>=2005-11-29T15:15:19 started<2005-11-29T15:15:20 "
        // + "last-active>=2005-11-29T15:15:19 last-active<2005-11-29T15:15:20"
        ,"+pid name -version -started", 10);
    System.out.println(instanceList);
  }
}
