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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompileListener;
import org.apache.ode.bpel.compiler.api.SourceLocation;
import org.apache.ode.bpel.compiler.bom.BpelObjectFactory;
import org.apache.ode.bpel.compiler.bom.Process;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.utils.StreamUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * <p>
 * Wrapper for {@link org.apache.ode.bpel.compiler.BpelCompiler} implementations,
 * providing basic utility methods and auto-detection of BPEL version.
 * </p>
s */
public class BpelC {
    private static final Log __log = LogFactory.getLog(BpelC.class);
    private static final CommonCompilationMessages __cmsgs =
            MessageBundle.getMessages(CommonCompilationMessages.class);

    public static final String PROCESS_CUSTOM_PROPERTIES = "customProps";

    private CompileListener _compileListener;
    public OutputStream _outputStream = null;

    private File _bpelFile;
    private File _suDir;
    private ResourceFinder _wsdlFinder;
    private URI _bpel11wsdl;
    private Map<String,Object> _compileProperties;
    private boolean _dryRun = false;

    public static BpelC newBpelCompiler() {
        return new BpelC();
    }

    private BpelC() {
    }

    protected void finalize() throws Throwable
    {
        this.invalidate();
        super.finalize();
    }

    private void invalidate() {
        this.setResourceFinder(null);
        this.setCompileListener(null);
        this.setOutputStream(null);
        XslTransformHandler.getInstance().setErrorListener( new net.sf.saxon.StandardErrorListener() );
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
     * Configures the compiler to run a dry compilation, doesn't generate the produced
     * compiled process.
     * @param dryRun
     */
    public void setDryRun(boolean dryRun) {
        _dryRun = dryRun;
    }

    /**
     * <p>
     * Tell the compiler how to locate WSDL imports for a BPEL process.  Setting this
     * to <code>null</code> will cause the default behavior.
     * </p>
     * @param finder the {@link ResourceFinder} implementation to use.
     */
    public void setResourceFinder(ResourceFinder finder) {
        _wsdlFinder = finder;
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
     * Compilation properties eventually retrieved by the compiler 
     * @param compileProperties
     */
    public void setCompileProperties(Map<String, Object> compileProperties) {
        _compileProperties = compileProperties;
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

    public void setBaseDirectory(File baseDir) {
        if (baseDir == null) throw new IllegalArgumentException("Argument 'baseDir' is null");
        if (!baseDir.exists()) throw new IllegalArgumentException("Directory "+baseDir+" does not exist");
        _suDir = baseDir;
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
    public void compile(final Process process, String outputPath, long version) throws CompilationException, IOException {
        if (process == null)
            throw new NullPointerException("Attempt to compile NULL process.");

        logCompilationMessage(__cmsgs.infCompilingProcess());

        BpelCompiler compiler;
        ResourceFinder wf;

        if (_wsdlFinder != null) {
            wf = _wsdlFinder;
        } else {
            File suDir = _suDir != null ? _suDir : _bpelFile.getParentFile(); 
            wf = new DefaultResourceFinder(_bpelFile.getAbsoluteFile().getParentFile(), suDir.getAbsoluteFile());
        }

        CompileListener clistener = new CompileListener() {
            public void onCompilationMessage(CompilationMessage compilationMessage) {
                SourceLocation location = compilationMessage.source;
                if (location == null) {
                    compilationMessage.source = process;
                }
                logCompilationMessage(compilationMessage);
            }
        };

        try {
            switch (process.getBpelVersion()) {
                case BPEL20:
                    compiler = new BpelCompiler20();
                    compiler.setResourceFinder(wf);
                    if (_bpel11wsdl != null) {
                        CompilationMessage cmsg = __cmsgs.warnWsdlUriIgnoredFor20Process();
                        logCompilationMessage(cmsg);
                    }
                    break;
                case BPEL20_DRAFT:
                    compiler = new BpelCompiler20Draft();
                    compiler.setResourceFinder(wf);
                    if (_bpel11wsdl != null) {
                        CompilationMessage cmsg = __cmsgs.warnWsdlUriIgnoredFor20Process();
                        logCompilationMessage(cmsg);
                    }
                    break;
                case BPEL11:
                    compiler = new BpelCompiler11();
                    compiler.setResourceFinder(wf);
                    if (_bpel11wsdl != null) {
                        compiler.addWsdlImport(new URI(_bpelFile.getName()), _bpel11wsdl,null);
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
            compiler.setCompileListener(clistener);
            if (_compileProperties != null) {
                if (_compileProperties.get(PROCESS_CUSTOM_PROPERTIES) != null)
                    compiler.setCustomProperties((Map<QName, Node>) _compileProperties.get(PROCESS_CUSTOM_PROPERTIES));
            }
        } catch (CompilationException ce) {
            this.invalidate();
            throw ce;
        } catch (Exception ex) {
            CompilationMessage cmsg = __cmsgs.errBpelParseErr();
            logCompilationMessage(cmsg);
            this.invalidate();
            throw new CompilationException(cmsg,ex);
        }

        OProcess oprocess;
        try {
            oprocess = compiler.compile(process,wf,version);
        }
        catch (CompilationException cex) {
            this.invalidate();
            throw cex;
        }

        if (!_dryRun) {
            if (outputPath != null) {
                this.setOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)));
                if (__log.isDebugEnabled()) {
                    __log.debug("Writing compilation results to " + outputPath);
                }
            } else if (_outputStream != null) {
                if (__log.isDebugEnabled()) {
                    __log.debug("Writing compilation results to " + _outputStream.getClass().getName());
                }
            } else {
                throw new IllegalStateException("must setOutputStream() or setOutputDirectory()!");
            }

            try {
                Serializer fileHeader = new Serializer(System.currentTimeMillis());
                fileHeader.writeOProcess(oprocess, _outputStream);
            } finally {
                // close & mark myself invalid
                this.invalidate();
            }
        }
    }

    /**
     * <p>
     * Compile a BPEL process from a file.  This method uses a {@link BpelObjectFactory}
     * to parse the XML and then calls {@link #compile(Process,String)}.
     * </p>
     * @param bpelFile the file of the BPEL process to be compiled.
     * @throws IOException if one occurs while reading the BPEL process or writing the
     * output.
     * @throws CompilationException if one occurs while compiling the process.
     */
    public void compile(File bpelFile, long version) throws CompilationException, IOException {
        if (__log.isDebugEnabled()) {
            __log.debug("compile(URL)");
        }

        if (bpelFile == null) {
            this.invalidate();
            throw new IllegalArgumentException("Null bpelFile");
        }

        _bpelFile = bpelFile;
        Process process;
        try {
            InputSource isrc = new InputSource(new ByteArrayInputStream(StreamUtils.read(bpelFile.toURL())));
            isrc.setSystemId(bpelFile.getAbsolutePath());

            process = BpelObjectFactory.getInstance().parse(isrc,_bpelFile.toURI());
        } catch (Exception e) {
            CompilationMessage cmsg = __cmsgs.errBpelParseErr().setSource(new SourceLocationImpl(bpelFile.toURI()));
            this.invalidate();
            throw new CompilationException(cmsg, e);
        }

        assert process != null;

        // Output file = bpel file with a cbp extension
        String bpelPath = bpelFile.getAbsolutePath();
        String cbpPath = bpelPath.substring(0, bpelPath.lastIndexOf(".")) + ".cbp";

        compile(process, cbpPath, version);
        this.invalidate();
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
