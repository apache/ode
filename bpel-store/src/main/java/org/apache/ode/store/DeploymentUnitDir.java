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
package org.apache.ode.store;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.BpelC;
import org.apache.ode.bpel.compiler.BpelCompiler;
import org.apache.ode.bpel.compiler.DefaultResourceFinder;
import org.apache.ode.bpel.compiler.WSDLLocatorImpl;
import org.apache.ode.bpel.compiler.wsdl.Definition4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactory4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactoryBPEL20;
import org.apache.ode.bpel.dd.DeployDocument;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TDeployment.Process;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.utils.InternPool;
import org.apache.ode.utils.InternPool.InternableBlock;
import org.apache.ode.utils.fs.FileUtils;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Node;

/**
 * Container providing various functions on the deployment directory.
 *
 * @author mriou
 * @author Maciej Szefler <mszefler at gmail dot com>
 */
class DeploymentUnitDir {


    private static Log __log = LogFactory.getLog(DeploymentUnitDir.class);

    private String _name;
    private File _duDirectory;
    private File _descriptorFile;
    private Properties _properties = new Properties();

    private HashMap<QName, CBPInfo> _processes = new HashMap<QName, CBPInfo>();
    private HashMap<QName, TDeployment.Process> _processInfo = new HashMap<QName, TDeployment.Process>();

    private volatile DeployDocument _dd;
    private volatile DocumentRegistry _docRegistry;

    private long _version = -1;

    private static final FileFilter _wsdlFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".wsdl") && path.isFile();
        }
    };

    private static final FileFilter _cbpFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".cbp") && path.isFile();
        }
    };

    private static final FileFilter _bpelFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".bpel") && path.isFile();
        }
    };

    private static final FileFilter _endpointFilter = new FileFilter() {
        public boolean accept(File path) {
            // endpoint-configuration.properties is deprecated, keep it for backward compatibility
            return (path.getName().endsWith(".endpoint") || path.getName().equals("endpoint-configuration.properties")) && path.isFile();
        }
    };

    DeploymentUnitDir(File dir) {
        if (!dir.exists())
            throw new IllegalArgumentException("Directory " + dir + " does not exist!");

        _duDirectory = dir;
        _name = dir.getName();
        _descriptorFile = new File(_duDirectory, "deploy.xml");

        if (!_descriptorFile.exists())
            throw new IllegalArgumentException("Directory " + dir + " does not contain a deploy.xml file!");

        try {
            ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
            _properties = (java.util.Properties) ctx.getBean("properties");
            if (__log.isDebugEnabled()) {
                __log.debug("Loaded spring properties from file beans.xml:" + _properties + " for " +  _name);
            }
        } catch (Exception e) {
            __log.info("Can't initialize beans.xml application context " + e + " for " + _name);
        }

    }


    String getName() {
        return _duDirectory.getName();
    }

    void setName(String name) {
    	// supports extensibility
    	_name = name;
    }

    CBPInfo getCBPInfo(QName typeName) {
        return _processes.get(typeName);
    }


    /**
     * Checking for each BPEL file if we have a corresponding compiled process. If we don't,
     * starts compilation.
     */
    void compile() {
        List<File> bpels = FileUtils.directoryEntriesInPath(_duDirectory, DeploymentUnitDir._bpelFilter);
        if (bpels.size() == 0)
            throw new IllegalArgumentException("Directory " + _duDirectory.getName() + " does not contain any process!");
        for (File bpel : bpels) {
        	String b = bpel.getAbsolutePath();
        	File cbp = new File(b.substring(0,b.lastIndexOf(".bpel")) + ".cbp"); 
        	if (!cbp.exists() || cbp.lastModified() < bpel.lastModified()) {
        		__log.debug("compiling " + bpel);
        		compile(bpel);
        	} else {
        		__log.debug("skipping compilation of " + bpel + " cbp found: " + cbp);
        	}
        }
    }

    void scan() {
        HashMap<QName, CBPInfo> processes = new HashMap<QName, CBPInfo>();
        List<File> cbps = FileUtils.directoryEntriesInPath(_duDirectory, DeploymentUnitDir._cbpFilter);
        for (File file : cbps) {
            CBPInfo cbpinfo = loadCBPInfo(file);
            processes.put(cbpinfo.processName, cbpinfo);
        }
        _processes = processes;

        HashMap<QName, Process> processInfo = new HashMap<QName, TDeployment.Process>();
        for (TDeployment.Process p : getDeploymentDescriptor().getDeploy().getProcessList()) {
            processInfo.put(p.getName(), p);
        }
        _processInfo = processInfo;

    }

    boolean isRemoved() {
        return !_duDirectory.exists();
    }

    private void compile(final File bpelFile) {
        final BpelC bpelc = BpelC.newBpelCompiler();

        // BPEL 1.1 does not suport the <import> element, so "global" WSDL needs to be configured explicitly.
        File bpel11wsdl = findBpel11Wsdl(bpelFile);
        if (bpel11wsdl != null)
            bpelc.setProcessWSDL(bpel11wsdl.toURI());

        bpelc.setCompileProperties(prepareCompileProperties(bpelFile));
        bpelc.setBaseDirectory(_duDirectory);
        // Create process such that immutable objects are intern'ed.
        InternPool.runBlock(new InternableBlock() {
        	public void run() {
                try {
                    bpelc.compile(bpelFile, getVersion());
                } catch (IOException e) {
                    __log.error("Compile error in " + bpelFile, e);
                }
        	}
        });
    }

    /**
     * Load the parsed and compiled BPEL process definition.
     */
    private CBPInfo loadCBPInfo(File f) {
        InputStream is = null;
        try {
            is = new FileInputStream(f);
            Serializer ofh = new Serializer(is);
            CBPInfo info = new CBPInfo(ofh.type, ofh.guid, f);
            return info;
        } catch (Exception e) {
            throw new ContextException("Couldn't read compiled BPEL process " + f.getAbsolutePath(), e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Exception e) {
                ;
            }
        }
    }


    public int hashCode() {
        return _duDirectory.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DeploymentUnitDir)) return false;
        return ((DeploymentUnitDir) obj).getDeployDir().getAbsolutePath().equals(getDeployDir().getAbsolutePath());
    }

    public File getDeployDir() {
        return _duDirectory;
    }

    /**
     *
     * The list of endpoint configuration files contained in the deployment directory and its subdirectories.
     * Files are ordered lexicographically but for each directory, files come before its sudirectories.
     * <p>The list is built on each call to handle changes.
     * @see org.apache.ode.utils.fs.FileUtils#directoryEntriesInPath(java.io.File)
     */
    public List<File> getEndpointConfigFiles() {
        return FileUtils.directoryEntriesInPath(getDeployDir(),_endpointFilter);
    }

    public DeployDocument getDeploymentDescriptor() {
        if (_dd == null) {
            File ddLocation = new File(_duDirectory, "deploy.xml");
            try {
                XmlOptions options = new XmlOptions();
                HashMap otherNs = new HashMap();

                otherNs.put("http://ode.fivesight.com/schemas/2006/06/27/dd",
                        "http://www.apache.org/ode/schemas/dd/2007/03");
                options.setLoadSubstituteNamespaces(otherNs);
                _dd = DeployDocument.Factory.parse(ddLocation, options);
            } catch (Exception e) {
                throw new ContextException("Couldn't read deployment descriptor at location "
                        + ddLocation.getAbsolutePath(), e);
            }

        }
        return _dd;
    }

    public DocumentRegistry getDocRegistry() {
        if (_docRegistry == null) {
            _docRegistry = new DocumentRegistry();

            WSDLFactory4BPEL wsdlFactory = (WSDLFactory4BPEL) WSDLFactoryBPEL20.newInstance();
            WSDLReader r = wsdlFactory.newWSDLReader();
            DefaultResourceFinder rf = new DefaultResourceFinder(_duDirectory, _duDirectory);
            URI basedir = _duDirectory.toURI();
            List<File> wsdls = FileUtils.directoryEntriesInPath(_duDirectory, DeploymentUnitDir._wsdlFilter);
            for (File file : wsdls) {
                URI uri = basedir.relativize(file.toURI());
                try {
                    _docRegistry.addDefinition((Definition4BPEL) r.readWSDL(new WSDLLocatorImpl(rf, uri)));
                } catch (WSDLException e) {
                    throw new ContextException("Couldn't read WSDL document at " + uri, e);
                }
            }
        }
        return _docRegistry;
    }

    public Definition getDefinitionForService(QName name) {
        return getDocRegistry().getDefinition(name);
    }

    public Definition getDefinitionForPortType(QName name) {
        return getDocRegistry().getDefinitionForPortType(name);
    }

    public Collection<Definition> getDefinitions() {
        Definition4BPEL defs[] = getDocRegistry().getDefinitions();
        ArrayList<Definition> ret = new ArrayList<Definition>(defs.length);
        for (Definition4BPEL def : defs)
            ret.add(def);
        return ret;
    }

    public Set<QName> getProcessNames() {
        return _processInfo.keySet();
    }

    public String toString() {
        return "{DeploymentUnit " + _name + "}";
    }

    public TDeployment.Process getProcessDeployInfo(QName type) {
        if (_processInfo == null) {
        }

        return _processInfo.get(type);
    }

    public List<File> allFiles() {
        return allFiles(_duDirectory);
    }

    private List<File> allFiles(File dir) {
        ArrayList<File> result = new ArrayList<File>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                result.addAll(allFiles(file));
            }
            if (file.isHidden()) continue;
            if (file.isFile()) {
                result.add(file);
            }
        }
        return result;
    }

    public final class CBPInfo {
        final QName processName;
        final String guid;
        final File cbp;

        CBPInfo(QName processName, String guid, File cbp) {
            this.processName = processName;
            this.guid = guid;
            this.cbp = cbp;
        }
    }

    private Map<String, Object> prepareCompileProperties(File bpelFile) {
        List<Process> plist = getDeploymentDescriptor().getDeploy().getProcessList();
        for (Process process : plist) {
            if (process.getFileName() == null || "".equals(process.getFileName()))
                continue;

            if (bpelFile.getName().equals(process.getFileName())) {
                Map<QName, Node> props = ProcessStoreImpl.calcInitialProperties(_properties, process);
                Map<String, Object> result = new HashMap<String, Object>();
                result.put(BpelC.PROCESS_CUSTOM_PROPERTIES, props);
                return result;
            }
        }
        return null;
    }


    /**
     * Figure out the name of the WSDL file for a BPEL 1.1 process.
     *
     * @param bpelFile BPEL process file name
     * @return file name of the WSDL, or null if none specified.
     */
    private File findBpel11Wsdl(File bpelFile) {
        List<Process> plist = getDeploymentDescriptor().getDeploy().getProcessList();
        for (Process process : plist) {
            if (process.getFileName() == null || "".equals(process.getFileName()))
                continue;
            if (!bpelFile.getName().equals(process.getFileName()))
                continue;
            if (process.getBpel11WsdlFileName() == null || "".equals(process.getBpel11WsdlFileName()))
                return null;

            return new File(bpelFile.getParentFile(), process.getBpel11WsdlFileName());
        }
        return null;
    }

    public long getVersion() {
        return _version;
    }
    
    /**
     * @return Static DU version number generated from DU name. -1 when package doesn't use versioning.
     */
    public long getStaticVersion() {
        return BpelCompiler.getVersion(getName());
    }

    public void setVersion(long version) {
        _version = version;
    }

    public Properties getProperties() {
        return _properties;
    }
}
