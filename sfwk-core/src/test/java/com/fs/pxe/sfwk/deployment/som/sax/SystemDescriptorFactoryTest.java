/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import com.fs.pxe.sfwk.deployment.som.*;
import com.fs.pxe.sfwk.deployment.som.impl.*;
import com.fs.pxe.sfwk.rr.ResourceRepository;
import com.fs.pxe.sfwk.rr.ResourceRepositoryBuilder;
import com.fs.utils.fs.TempFileManager;
import com.fs.utils.sax.FailOnErrorErrorHandler;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SystemDescriptorFactoryTest extends TestCase{
  
  private ResourceRepositoryBuilder _helloRr;
  private ResourceRepositoryBuilder _hello2Rr;
  private ResourceRepositoryBuilder _hello3Rr;
  private ResourceRepository _rr;

  public SystemDescriptorFactoryTest(String s) {
    super(s);
  }

  public void setUp() throws Exception {
    _helloRr = new ResourceRepositoryBuilder(TempFileManager.getTemporaryDirectory("helloRr"));
    _hello2Rr = new ResourceRepositoryBuilder(TempFileManager.getTemporaryDirectory("hello2Rr"));
    _hello3Rr = new ResourceRepositoryBuilder(TempFileManager.getTemporaryDirectory("hello3Rr"));
    _helloRr.addURI(new URI("uri:hello.wsdl"), getResource("HelloWorld.wsdl"));
    _hello2Rr.addURI(new URI("uri:hello2.wsdl"), getResource("HelloWorld2.wsdl"));
    _hello3Rr.addURI(new URI("uri:hello2.wsdl"), getResource("HelloWorld.wsdl"));
  }

  public void tearDown() {
    if (_rr != null) {
      try {
        _rr.close();
      }
      catch (IOException ioex) {
        // should never happen
        fail(ioex.getMessage());
      }
    }

    // get rid of any temp files/directories;
    // should work without complaining on Windows. 
    TempFileManager.cleanup();
  }

  private URL getResource(String n) {
    return SystemDescriptorFactoryTest.class.getResource(n);
  }

  private SystemDescriptorImpl generateDescriptor(int s, int p)  throws Exception {
    SystemDescriptorImpl sd = new SystemDescriptorImpl();
    sd.setName("system-" + s + "-" + p);
    sd.setWsdlUri(new URI("file:/foo/bar"));
    PortImpl[][] ipp = new PortImpl[s][p];
    PortImpl[][] epp = new PortImpl[s][p];
    for (int i=0; i < s; ++i) {
      ServiceImpl si = sd.newService();
      si.setName("service" + i);
      si.setProviderUri(new URI("uri://provider_" + i));
      for (int k=0; k < (i+1); ++k) {
        PropertyImpl pr = new PropertyImpl();
        pr.setName("property-" + i + "-" +k);
        pr.setValue("value-" + i + "-" +k);
        si.addProperty(pr);
      }
      sd.addService(si);
      for (int j=0; j<p; ++j) {
        ipp[i][j] = si.newPort();
        ipp[i][j].setName("imported-" + i + "-" + j);
        ipp[i][j].setType(new QName("uri:port","type_" + j));
        ipp[i][j].setChannelRef("channel-" + i + "-" + j);
        for (int k=0; k < (p-j); ++k) {
          PropertyImpl pr = new PropertyImpl();
          pr.setName("property-" + i + "-" + j + "-" + k);
          pr.setValue("value-" + i + "-" + j + "-" + k);
          ipp[i][j].addProperty(pr);
        }
        si.addImportedPort(ipp[i][j]);
        epp[i][j] = si.newPort();
        epp[i][j].setName("exported-" + i + "-" + j);
        epp[i][j].setType(new QName("uri:port","type_" + j));
        epp[i][j].setChannelRef("channel-" + ((i+1)%s) + "-" + j);
        for (int k=0; k < j+1; ++k) {
          PropertyImpl pr = new PropertyImpl();
          pr.setName("property-" + i + "-" + j + "-" + k);
          pr.setValue("value-" + i + "-" + j + "-" + k);
          epp[i][j].addProperty(pr);
        }
        si.addExportedPort(epp[i][j]);
      }
    }
    for (int i=0; i < s; ++i) {
      for (int j=0; j < p; ++j) {
        ChannelImpl ci = sd.newChannel();
        sd.addChannel(ci);
        ci.setName("channel-" + i + "-" + j);
      }
    }
    return sd;
  }

  private void validateGeneratedDescriptor(SystemDescriptor sd, int s, int p)  throws Exception {
    assertTrue(sd.getName().equals("system-" + s + "-" + p));
    assertTrue(sd.getWsdlUri().toString().equals(new URI("file:/foo/bar").toString()));
    Service[] ss = sd.getServices();
    assertTrue(ss.length == s);
    for (int i=0; i < s; ++i) {
      assertTrue(ss[i].getName().equals("service" + i));
      assertTrue(ss[i].getProviderUri().toString().equals(new URI("uri://provider_" + i).toString()));
      Property[] sp = ss[i].getProperties();
      assertTrue(sp.length == (i+1));
      for (int k=0; k < (i+1); ++k) {
        assertTrue(sp[k].getName().equals("property-" + i + "-" +k));
        assertTrue(sp[k].getValue().equals("value-" + i + "-" +k));

      }      
      Port[] ipp = ss[i].getImportedPorts();
      Port[] epp = ss[i].getExportedPorts();
      assertTrue(ipp.length == p);
      assertTrue(epp.length == p);
      for (int j=0; j<p; ++j) {
        assertTrue(ipp[j].getName().equals("imported-" + i + "-" + j));
        assertTrue(ipp[j].getType().equals(new QName("uri:port","type_" + j)));
        assertTrue(ipp[j].getChannelRef().equals("channel-" + i + "-" + j));
        Property[] pp = ipp[j].getProperties();
        assertTrue(pp.length == (p-j));
        for (int k=0; k < (p-j); ++k) {
          assertTrue(pp[k].getName().equals("property-" + i + "-" + j + "-" + k));
          assertTrue(pp[k].getValue().equals("value-" + i + "-" + j + "-" + k));
        }
        assertTrue(epp[j].getName().equals("exported-" + i + "-" + j));
        assertTrue(epp[j].getType().equals(new QName("uri:port","type_" + j)));
        assertTrue(epp[j].getChannelRef().equals("channel-" + ((i+1)%s) + "-" + j));
        pp = epp[j].getProperties();
        for(int k=0; k < j+1; ++k) {
          assertTrue(pp[k].getName().equals("property-" + i + "-" + j + "-" + k));
          assertTrue(pp[k].getValue().equals("value-" + i + "-" + j + "-" + k));          
        }
      }
    }
    Channel[] cc = sd.getChannels();
    assertTrue(cc.length == s*p);
    int cnt = 0;
    for (int i=0; i < s; ++i) {
      for (int j=0; j < p; ++j) {        
        assertTrue(cc[cnt++].getName().equals("channel-" + i + "-" + j));
      }
    }
  }
  
  
  private URL serializeDescriptor(SystemDescriptorImpl sd) throws Exception {
    File f = TempFileManager.getTemporaryFile("TSDF-test");
    XMLSerializer xs = new XMLSerializer();
    FileOutputStream fos = new FileOutputStream(f);
    xs.setOutputByteStream(fos);
    sd.toSaxEvents(xs);
    fos.close();
    return f.toURL();
  }
  
  private void validateHelloWorld(SystemDescriptorImpl sd) throws Exception {
    Service[] ss = sd.getServices();
    assertTrue("expected exactly two services instead of " + ss.length,ss.length==2);
    
    assertTrue(ss[0].getName().equals("HelloService"));
    assertTrue(ss[0].getExportedPorts().length == 0);
    assertTrue(ss[0].getProperties().length == 0);
    assertTrue(ss[0].getImportedPorts().length == 1);
    assertTrue(ss[0].getImportedPorts()[0].getName().equals("HelloPort"));        
    assertTrue(ss[0].getImportedPorts()[0].getType().toString() + " is incorrect; " +
        "it should be " + new QName("http://pxe/bpel/unit-test.wsdl","HelloPortType").toString(), 
        ss[0].getImportedPorts()[0].getType().equals(
        new QName("http://pxe/bpel/unit-test.wsdl","HelloPortType")));
    assertTrue(ss[0].getImportedPorts()[0].getProperties().length == 0);
    
    assertTrue(ss[1].getName().equals("helloWorld.BpelService"));
    assertTrue(ss[1].getExportedPorts().length == 1);
    assertTrue(ss[1].getProperties().length == 0);
    assertTrue(ss[1].getImportedPorts().length == 0);
    assertTrue(ss[1].getExportedPorts()[0].getName().equals("helloPartnerLink.me"));
    assertTrue(ss[1].getExportedPorts()[0].getType().toString() + " is incorrect.",
        ss[1].getExportedPorts()[0].getType().equals(
        new QName("http://pxe/bpel/unit-test.wsdl","HelloPortType")));
    assertTrue(ss[1].getExportedPorts()[0].getProperties().length == 0);
    
    assertTrue(ss[1].getExportedPorts()[0].getChannelRef().equals( 
      ss[0].getImportedPorts()[0].getChannelRef()));
    
    Channel[] cc = sd.getChannels();
    assertTrue("expected exactly one channel instead of " + cc.length, cc.length==1);    
  }

  private void expectedFailure(String res, int line) throws IOException {
    try {
      SystemDescriptorFactory.parseDescriptor(
          getResource(res),new FailOnErrorErrorHandler(),
          null,true);
    } catch (SAXParseException spe) {
      if (spe.getLineNumber() != line) {
        System.err.println(res + " : " + spe.getLineNumber());
        spe.printStackTrace(System.err);
        System.err.println();
      }
      assertTrue(res + " failed in an unexpected location: " + spe.getLineNumber(),
          spe.getLineNumber() == line);
      return;
    } catch (SAXException se) {
      fail("The resource " + res + " should have caused a more graceful failure than: " +
          se.getMessage());
    }
    fail("The resource " + res + " should have caused an error.");
  }
  
  public void testParsingGeneratedDescriptor() throws Exception {

    SystemDescriptor md = SystemDescriptorFactory.parseDescriptor(
        serializeDescriptor(generateDescriptor(20,20)),
        new FailOnErrorErrorHandler(),
        null,true);
    validateGeneratedDescriptor(md,20,20);
  }
  
  public void testBadDescriptorInvalidXml1() throws Exception {
    SystemDescriptorFactory.parseDescriptor(
        getResource("helloworld-good-wrong-schemaLocation.xml"),
        new FailOnErrorErrorHandler(),
        null,true);
  }

  public void testBadDescriptorInvalidXml2() throws Exception {
    expectedFailure("helloworld-bad-wrong-namespace2.xml",7);
  }
  
  public void testBadDescriptorInvalidXml3() throws Exception {
    expectedFailure("helloworld-bad-wrong-namespace3.xml",6);
  }
  
  public void testBadDescriptorInvalidXml4() throws Exception {
    expectedFailure("helloworld-bad-non-unique-service-names.xml",24);
  }  

  public void testBadDescriptorInvalidXml5() throws Exception {
    expectedFailure("helloworld-bad-non-unique-channel-names.xml",10);
  }  
  
  public void testPortTypeNotDeclaredControl() throws Exception {
    //  This test **should** succeed.
    _rr = _hello2Rr.toResourceRepository();
    SystemDescriptorFactory.parseDescriptor(getResource("helloworld-undecl-porttype.xml"),
        new FailOnErrorErrorHandler(), _rr ,true);
  }

  public void testPortTypeNotDeclared() throws Exception {
    String res = "helloworld-undecl-porttype.xml";
    _rr = _hello3Rr.toResourceRepository();
    try {
      SystemDescriptorFactory.parseDescriptor(getResource(res), new FailOnErrorErrorHandler(), _rr, true);
    } catch (SAXParseException spe) {
      if (spe.getLineNumber() != 19) {
        System.err.println(res + " : " + spe.getLineNumber());
        spe.printStackTrace(System.err);
        System.err.println();
      }
      assertTrue("Failure (" + spe.getMessage() + ") occurred in an unexpected location: " +
          spe.getLineNumber(),
          spe.getLineNumber() == 19);
      return;
    } catch (SAXException se) {
      fail(res + " should have failed more gracefully.");
    }
    fail(res + " should have caused a failure.");
  }
  
  public void testMismatchedPortType() throws Exception {
    expectedFailure("helloworld-mismatched-porttype.xml",20);
  }
  
  public void testGoodDescriptorNoWsdlRepository() throws Exception {
    SystemDescriptor sd = SystemDescriptorFactory.parseDescriptor(getResource("helloworld-good.xml"),
        new FailOnErrorErrorHandler(),null,true);
    validateHelloWorld((SystemDescriptorImpl)sd);  
  }

  public void testGoodDescriptorMultiplePrefixesNoWsdlRepository() throws Exception {
    SystemDescriptorFactory.parseDescriptor(getResource("multiple-prefixes-test.xml"),
        new FailOnErrorErrorHandler(),null,true);
  }
  
  public void testDescriptorRecycling() throws Exception {
    SystemDescriptor sd = SystemDescriptorFactory.parseDescriptor(getResource("helloworld-good.xml"),
        new FailOnErrorErrorHandler(),null,true);
    validateHelloWorld((SystemDescriptorImpl)sd);  
    
    SystemDescriptorContentHandler sdch = new SystemDescriptorContentHandler(new FailOnErrorErrorHandler());
    
    ((SystemDescriptorImpl)sd).toSaxEvents(sdch);
    SystemDescriptor sd2 = sdch.getDescriptor();
    validateHelloWorld((SystemDescriptorImpl)sd2); 
    
  }
  
  public void testGoodDescriptorWsdlRepository() throws Exception {
    SystemDescriptorFactory.parseDescriptor(getResource("helloworld-good.xml"),
        new FailOnErrorErrorHandler(),null,true);
  }

  public void testResourceRepositoryClosing() throws Exception {
    // create a ResourceRepository for reading
    _rr = _hello3Rr.toResourceRepository();

    // see setUp()
    InputStream is = _rr.resourceAsStream(new URI("uri:hello2.wsdl"));
    assertNotNull(is);

    // until now nothing exciting has happened (hopefully); however, tearDown()
    // will now attempt to close the _rr, which should release the
    // just opened InputStream.
  }

  public void testResourceRepositoryIllegalState() throws Exception {
    // create a ResourceRepository for reading
    _rr = _hello3Rr.toResourceRepository();

    // ..and close it again
    _rr.close();

    try {
      // see setUp()
      _rr.resourceAsStream(new URI("uri:hello2.wsdl"));
      // should not have succeeded!
      fail();
    }
    catch (IllegalStateException isex) {
      // that's what we wanted.
    }
  }

}
