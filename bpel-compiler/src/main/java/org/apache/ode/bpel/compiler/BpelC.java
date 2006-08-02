/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.compiler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bom.api.BpelObject;
import org.apache.ode.bom.api.Process;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.capi.CompilationMessage;
import org.apache.ode.bpel.capi.CompileListener;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.bpel.parser.BpelParseException;
import org.apache.ode.bpel.parser.BpelProcessBuilder;
import org.apache.ode.bpel.parser.BpelProcessBuilderFactory;
import org.apache.ode.bpel.parser.BpelProcessBuilderFactoryException;
import org.apache.ode.bpel.xsl.XslCompilationErrorListener;
import org.apache.ode.bpel.xsl.XslTransformHandler;
import org.apache.ode.sax.fsa.ParseError;
import org.apache.ode.utils.StreamUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.xml.sax.InputSource;

import javax.xml.transform.TransformerFactory;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * <p>
 * Wrapper for {@link org.apache.ode.bpel.compiler.BpelCompiler} implementations,
 * providing basic utility methods and auto-detection of BPEL version.
 * </p>
 */
public class BpelC {
  private static final Log __log = LogFactory.getLog(BpelC.class);
  private static final CommonCompilationMessages __cmsgs =
          MessageBundle.getMessages(CommonCompilationMessages.class);

  private BpelProcessBuilderFactory _bpelProcessBuilderFactory;
  private CompileListener _compileListener;
  public OutputStream _outputStream = null;
  private File _outputDir = null;

  private URL _bpelUrl;
  private WsdlFinder _wsdlFinder;
  private XsltFinder _xsltFinder;
  private URI _bpel11wsdl;

  public static BpelC newBpelCompiler() {
    return new BpelC();
  }

  private BpelC() {
    try {
      _bpelProcessBuilderFactory = BpelProcessBuilderFactory.newProcessBuilderFactory();
    } catch (BpelProcessBuilderFactoryException e) {
      throw new RuntimeException(e);
    }
    _bpelProcessBuilderFactory.setStrict(true);
  }

  protected void finalize() throws Throwable
  {
    this.invalidate();
    super.finalize();
  }

  private void invalidate() {
    this.setWsdlFinder(null);
    this.setCompileListener(null);
    this.setOutputStream(null);
    this.setOutputDirectory(null);
  }

  /**
   * <p>
   * Set a non-default target {@link CompileListener} implementation.
   * </p>
   * @param cl the listener.
   */
  public void setCompileListener(CompileListener cl) {
    _compileListener = cl;
  }

  /**
   * <p>
   * Tell the compiler how to locate WSDL imports for a BPEL process.  Setting this
   * to <code>null</code> will cause the default behavior.
   * </p>
   * @param finder the {@link WsdlFinder} implementation to use.
   */
  public void setWsdlFinder(WsdlFinder finder) {
    _wsdlFinder = finder;
  }

  /**
   * <p>
   * Tell the compiler how to locate XSLT sheets used in assignment withn a BPEL process.
   * Setting this to <code>null</code> will cause the default behavior.
   * </p>
   * @param finder the {@link XsltFinder} implementation to use.
   */
  public void setXsltFinder(XsltFinder finder) {
    _xsltFinder = finder;
  }

  /**
   * Register a "global" WSDL import for compilation. This is used to specify WSDL
   * imports for BPEL 1.1 processes that do not support the <code>&lt;import&gt;</code>
   * BPEL construct.
   * @param wsdl the WSDL URI (resolvable against the resource repository)
   */
  public void setProcessWSDL(URI wsdl) {
    if (__log.isDebugEnabled()) {
      __log.debug("Adding WSDL import: \"" + wsdl.toASCIIString() + "\".");
    }
    _bpel11wsdl = wsdl;
  }

  /**
   * Set the output stream to which the compiled representation will be generated. 
   * @param os compiled representation output stream
   */
  public void setOutputStream(OutputStream os) {
    if (_outputStream != null) {
      try {
        _outputStream.close();
      }
      catch (IOException ioex) {
        // ignore
      }
    }

    _outputStream = os;

    if (__log.isDebugEnabled()) {
      __log.debug("Sett output to stream " + os);
    }
  }

  /**
   * <p>
   * Set the target directory for output.  This overrides {@link #setOutputStream(OutputStream)}.
   * </p>
   * @param outputDir the filesystem directory to write the compiled process to.
   * @see #setOutputStream(OutputStream)
   */
  public void setOutputDirectory(File outputDir) {
    // override any outputStream setting
    this.setOutputStream(null);

    // check if this is suitable for output
    if (outputDir != null) {
      if (outputDir.exists() && outputDir.isDirectory() && outputDir.canWrite()) {
        _outputDir = outputDir;
        if (__log.isDebugEnabled()) {
          __log.debug("Set output directory to " + outputDir.toURI());
        }
      } else {
        throw new IllegalArgumentException("outputDirectory not writeable: " + outputDir.toURI());
      }
    }
  }

  /**
   * <p>
   * Compile a BPEL process from a BOM {@link Process} object.
   * </p>
   * 
   * @param process
   *          the BOM <code>Process</code> to compile.
   * 
   * @throws IOException
   *           if one occurs while processing (e.g., getting imports) or writing
   *           output.
   * @throws CompilationException
   *           if one occurs while compiling.
   */
  public void compile(Process process) throws CompilationException, IOException {
    logCompilationMessage(__cmsgs.infCompilingProcess());

    BpelCompiler compiler;
    WsdlFinder wf;
    XsltFinder xf;

    try {
      if (_wsdlFinder != null) {
        wf = _wsdlFinder;
      } else {
        wf = new DefaultWsdlFinder(_bpelUrl.toURI());
      }
    } catch (URISyntaxException e) {
      CompilationMessage cmsg = __cmsgs.errInvalidImport(_bpelUrl.toExternalForm());
      logCompilationMessage(cmsg);
      this.invalidate();
      throw new CompilationException(cmsg);
    }

    try {
      if (_xsltFinder != null) {
        xf = _xsltFinder;
      } else {
        xf = new DefaultXsltFinder(_bpelUrl.toURI());
      }
    } catch (URISyntaxException e) {
      CompilationMessage cmsg = __cmsgs.errInvalidImport(_bpelUrl.toExternalForm());
      logCompilationMessage(cmsg);
      this.invalidate();
      throw new CompilationException(cmsg);
    }

    CompileListener clistener = new CompileListener() {
      public void onCompilationMessage(CompilationMessage compilationMessage) {
        Object location = compilationMessage.source;
        if (location == null) {
          compilationMessage.source = _bpelUrl.toExternalForm() + ":";
        }
        if (location instanceof BpelObject) {
          compilationMessage.source = _bpelUrl.toExternalForm() + ":" + ((BpelObject)location).getLineNo();
        }
        logCompilationMessage(compilationMessage);
      }
    };

    switch (process.getBpelVersion()) {
      case Process.BPEL_V200:
        compiler = new BpelCompiler20();
        compiler.setCompileListener(clistener);
        compiler.setWsdlFinder(wf);
        compiler.setXsltFinder(xf);
        initializeXslEngine(compiler);
        if (_bpel11wsdl != null) {
          CompilationMessage cmsg = __cmsgs.warnWsdlUriIgnoredFor20Process();
          logCompilationMessage(cmsg);
        }
        break;
      case Process.BPEL_V110:
        compiler = new BpelCompiler11();
        compiler.setCompileListener(clistener);
        compiler.setWsdlFinder(wf);
        if (_bpel11wsdl != null) {
          compiler.addWsdlImport(_bpel11wsdl);
        } else {
          CompilationMessage cmsg = __cmsgs.errBpel11RequiresWsdl();
          logCompilationMessage(cmsg);
          this.invalidate();
          throw new CompilationException(cmsg);
        }
        break;
      default:
        CompilationMessage cmsg = __cmsgs.errUnrecognizedBpelVersion();
        logCompilationMessage(cmsg);
        this.invalidate();
        throw new CompilationException(cmsg);
    }

    OProcess oprocess;
    try {
      oprocess = compiler.compile(process);
    }
    catch (CompilationException cex) {
      this.invalidate();
      throw cex;
    }

    if (_outputStream != null) {
      if (__log.isDebugEnabled()) {
        __log.debug("Writing compilation results to " + _outputStream.getClass().getName());
      }
    } else if (_outputDir != null) {
      File outFile = new File(_outputDir, oprocess.getName() + ".cbp");
      this.setOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
      if (__log.isDebugEnabled()) {
        __log.debug("Writing compilation results to " + outFile.toURI().toString());
      }
    } else {
      throw new IllegalStateException("must setOutputStream() or setOutputDirectory()!");
    }

    try {
      Serializer fileHeader = new Serializer(System.currentTimeMillis(), 1);
      fileHeader.write(_outputStream);
      fileHeader.writeOProcess(oprocess, _outputStream);

//      if (_bpelUrl.toString().startsWith("file:") && _outputDir != null) {
//        String filePath = _bpelUrl.getFile();
//        filePath = filePath.substring(0, filePath.lastIndexOf(".")) + ".dd";
//
//        DDHandler ddHandler;
//        try {
//          ddHandler = new DDHandler(new File(filePath));
//          if (!ddHandler.exists()) __log.info("No deployment descriptor found, one will be generated if needed.");
//          boolean modified = ddHandler.validateAndEnhance(oprocess, compiler.getWsdlDefinitions());
//          if (ddHandler.exists() || modified)
//            ddHandler.write(new File(_outputDir, oprocess.getName() + ".dd"));
//        } catch (DDValidationException e) {
//          CompilationMessage cm = __cmsgs.errInvalidDeploymentDescriptor(e.getMessage());
//          logCompilationMessage(cm);
//          throw new CompilationException(cm);
//        } catch (DDException e) {
//          logCompilationMessage(__cmsgs.errInvalidDeploymentDescriptor(e.getMessage()));
//        }
//      } else {
//        __log.warn("Post-compilation using deployment descriptor deactivated (compilation from url or stream).");
//      }
    } finally {
      // close & mark myself invalid
      this.invalidate();
    }
  }

  /**
   * <p>
   * Compile a BPEL process from a URL.  This method uses a {@link BpelProcessBuilder}
   * to parse the XML and then calls {@link #compile(Process)}.
   * </p>
   * @param bpelUrl the URL of the BPEL process to be compiled.
   * @throws IOException if one occurs while reading the BPEL process or writing the
   * output.
   * @throws CompilationException if one occurs while compiling the process.
   */
  public void compile(URL bpelUrl) throws CompilationException, IOException {
    if (__log.isDebugEnabled()) {
      __log.debug("compile(URL)");
    }

    if (bpelUrl == null) {
      this.invalidate();
      throw new IllegalArgumentException("Null bpelUrl");
    }

    _bpelUrl = bpelUrl;
    BpelProcessBuilder bpelProcessBuilder = _bpelProcessBuilderFactory.newBpelProcessBuilder();
    Process process;
    try {
      InputSource isrc = new InputSource(new ByteArrayInputStream(StreamUtils.read(bpelUrl)));
      isrc.setSystemId(bpelUrl.toExternalForm());
      try {
        process = bpelProcessBuilder.parse(isrc, bpelUrl.toExternalForm());
      } finally {
        ParseError[] parseErrors = bpelProcessBuilder.getParseErrors();
        for (ParseError parseError : parseErrors) {
          CompilationMessage cmsg = toCompilationMessage(parseError);
          logCompilationMessage(cmsg);
        }
      }
    } catch (BpelParseException e) {
      CompilationMessage cmsg = __cmsgs.errBpelParseErr().setSource(bpelUrl.toExternalForm());
      this.invalidate();
      throw new CompilationException(cmsg, e);
    }

    compile(process);
    this.invalidate();
  }

  private void initializeXslEngine(BpelCompiler compiler) {
    System.setProperty("javax.xml.transform.TransformerFactory",
            "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

    TransformerFactory trsf = TransformerFactory.newInstance();
    XslTransformHandler.getInstance().setTransformerFactory(trsf);

    XslCompilationErrorListener xe = new XslCompilationErrorListener(compiler);
    XslTransformHandler.getInstance().setXslCompilationErrorListener(xe);
  }

  /**
   * Convert a parse error ({@link ParseError}) from the BOM API to a similar
   * {@link CompilationMessage}.
   * @param parseError BOM parse error
   * @return equivalent {@link CompilationMessage}
   */
  private CompilationMessage toCompilationMessage(ParseError parseError) {
    CompilationMessage cmsg = new CompilationMessage();
    switch (parseError.getSeverity()) {
      case ParseError.WARNING:
        cmsg.severity = CompilationMessage.WARN;
        break;
      case ParseError.ERROR:
      case ParseError.FATAL:
      default:
        cmsg.severity = CompilationMessage.ERROR;
        break;
    }

    cmsg.phase = 1;
    cmsg.code = parseError.getKey();
    cmsg.messageText = parseError.getMessage();
    cmsg.source = parseError.getLocationURI() + ":" + parseError.getLine();
    return cmsg;
  }


  /**
   * Log a compilation message, both to the log, and to the listener (if any).
   * @param cmsg
   */
  private void logCompilationMessage(CompilationMessage cmsg) {
    if (_compileListener != null) {
      _compileListener.onCompilationMessage(cmsg);
    } else {
      switch (cmsg.severity) {
        case CompilationMessage.ERROR:
          if (__log.isErrorEnabled())
            __log.error(cmsg.toErrorString());
          break;
        case CompilationMessage.INFO:
          if (__log.isInfoEnabled())
            __log.info(cmsg.toErrorString());
          break;
        case CompilationMessage.WARN:
          if (__log.isWarnEnabled())
            __log.warn(cmsg.toErrorString());
      }
    }
  }

}
