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

package org.apache.ode.bpel.deploy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bom.wsdl.Definition4BPEL;
import org.apache.ode.bom.wsdl.WSDLFactory4BPEL;
import org.apache.ode.bom.wsdl.WSDLFactoryBPEL20;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.compiler.BpelC;
import org.apache.ode.bpel.compiler.DefaultWsdlFinder;
import org.apache.ode.bpel.compiler.DefaultXsltFinder;
import org.apache.ode.bpel.dd.DeployDocument;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.DeploymentUnit;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Container providing various functions on the deployment directory.
 * 
 * @author mriou
 * 
 * TODO Add a way to cause lazy methods to re-process stuff on disk.
 */
public class DeploymentUnitImpl implements DeploymentUnit {

    private static Log __log = LogFactory.getLog(DeploymentUnitImpl.class);

    private long _lastModified;

    private String _name;

    private File _duDirectory;

    private DocumentRegistry _docRegistry;

    private HashMap<QName, OProcess> _processes;

    private DeployDocument _dd;

    private File _descriptorFile;

    private HashMap<QName, TDeployment.Process> _processInfo;

    private static final FileFilter _wsdlFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".wsdl");
        }
    };

    private static final FileFilter _cbpFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".cbp");
        }
    };

    private static final FileFilter _bpelFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".bpel");
        }
    };

    public DeploymentUnitImpl(File dir) {
        if (!dir.exists())
            throw new IllegalArgumentException("Directory " + dir + " does not exist!");

        _duDirectory = dir;
        _name = dir.getName();
        _descriptorFile = new File(_duDirectory, "deploy.xml");

        if (!_descriptorFile.exists())
            throw new IllegalArgumentException("Directory " + dir + " does not contain a deploy.xml file!");

        _lastModified = _descriptorFile.lastModified();

        refresh();
    }

    
    /**
     * Checking for each BPEL file if we have a corresponding compiled process.
     * If we don't, starts compilation. The force parameter just forces
     * compilation, whether a cbp file exists or not.
     */
    private void compileProcesses(boolean force) {
        File[] bpels = _duDirectory.listFiles(_bpelFilter);
        for (File bpel : bpels) {
            File compiled = new File(bpel.getParentFile(), bpel.getName().substring(0,bpel.getName().length()-".bpel".length()) + ".cbp");
            if (compiled.exists() && !force) {
                continue;
            }
            compile(bpel);
        }
    }

    private void compile(File bpelFile) {
        BpelC bpelc = BpelC.newBpelCompiler();
        bpelc.setOutputDirectory(_duDirectory);
        bpelc.setWsdlFinder(new DefaultWsdlFinder(_duDirectory));
        bpelc.setXsltFinder(new DefaultXsltFinder(_duDirectory));
        try {
            bpelc.compile(bpelFile);
        } catch (IOException e) {
            __log.error("Couldn't compile process file!", e);
        }
    }

    private void loadProcessDefinitions(boolean force) {
        try {
            compileProcesses(force);
        } catch (CompilationException e) {
            // No retry on compilation error, we just forget about it
            _lastModified = new File(_duDirectory, "deploy.xml").lastModified();
            throw new BpelEngineException("Compilation failure!");
        }
        if (_processes == null || force) {
            _processes = new HashMap<QName, OProcess>();
            File[] cbps = _duDirectory.listFiles(_cbpFilter);
            for (File file : cbps) {
                OProcess oprocess = loadProcess(file);
                _processes.put(new QName(oprocess.targetNamespace, oprocess.getName()), oprocess);
            }
        }
    }

    /**
     * Load the parsed and compiled BPEL process definition.
     */
    private OProcess loadProcess(File f) {
        InputStream is = null;
        try {
            is = new FileInputStream(f);
            Serializer ofh = new Serializer(is);
            return ofh.readOProcess();
        } catch (Exception e) {
            throw new BpelEngineException("Couldn't read compiled BPEL process " + f.getAbsolutePath(), e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Exception e) {
            }
        }
    }

    public boolean removed() {
        return !_duDirectory.exists();
    }

    public boolean matches(File f) {
        return f.getAbsolutePath().equals(new File(_duDirectory, "deploy.xml").getAbsolutePath());
    }

    public boolean checkForUpdate() {
        File deployXml = new File(_duDirectory, "deploy.xml");
        if (!deployXml.exists())
            return false;
        return deployXml.lastModified() != _lastModified;
    }

    public int hashCode() {
        return (int) (_name.hashCode() + _lastModified);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DeploymentUnitImpl)) return false;
        return ((DeploymentUnitImpl)obj).getDeployDir().getAbsolutePath().equals(getDeployDir().getAbsolutePath());
    }

    public File getDeployDir() {
        return _duDirectory;
    }

    public void setLastModified(long lastModified) {
        _lastModified = lastModified;
    }

    public long getLastModified() {
        return _lastModified;
    }

    public DeployDocument getDeploymentDescriptor() {
        if (_dd == null) {
            File ddLocation = new File(_duDirectory, "deploy.xml");
            try {
                _dd = DeployDocument.Factory.parse(ddLocation);
            } catch (Exception e) {
                throw new BpelEngineException("Couldnt read deployment descriptor at location "
                        + ddLocation.getAbsolutePath(), e);
            }

        }
        return _dd;
    }

    public HashMap<QName, OProcess> getProcesses() {
        loadProcessDefinitions(false);
        return _processes;
    }

    public DocumentRegistry getDocRegistry() {
        if (_docRegistry == null) {
            _docRegistry = new DocumentRegistry(new DocumentEntityResolver(_duDirectory));

            WSDLFactory4BPEL wsdlFactory = (WSDLFactory4BPEL) WSDLFactoryBPEL20.newInstance();
            WSDLReader r = wsdlFactory.newWSDLReader();

            File[] wsdls = _duDirectory.listFiles(_wsdlFilter);
            for (File file : wsdls) {
                try {
                    _docRegistry.addDefinition((Definition4BPEL) r.readWSDL(file.toURI().toString()));
                } catch (WSDLException e) {
                    throw new BpelEngineException("Couldn't read WSDL document " + file.getAbsolutePath(), e);
                }
            }
        }
        return _docRegistry;
    }

    public Definition getDefinitionForNamespace(String namespaceURI) {
        return getDocRegistry().getDefinition(namespaceURI);
    }

    public Collection<Definition> getDefinitions() {
        Definition4BPEL defs[] = getDocRegistry().getDefinitions();
        ArrayList<Definition> ret = new ArrayList<Definition>(defs.length);
        for (Definition4BPEL def : defs)
            ret.add(def);
        return ret;
    }

    public Set<QName> getProcessNames() {        
        if (_processes == null) loadProcessDefinitions(false);
        return _processes.keySet();
    }

    public String toString() {
        return "{DeploymentUnit " + _name + "}";
    }

    public TDeployment.Process getProcessDeployInfo(QName pid) {
        if (_processInfo == null) {
            _processInfo = new HashMap<QName, TDeployment.Process>();

            for (TDeployment.Process p : getDeploymentDescriptor().getDeploy().getProcessList()) {
                _processInfo.put(p.getName(), p);
            }
        }

        return _processInfo.get(pid);
    }

    public void refresh() {
        loadProcessDefinitions(true);
    }
}
