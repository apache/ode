package org.apache.ode.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.BpelC;
import org.apache.ode.bpel.compiler.DefaultWsdlFinder;
import org.apache.ode.bpel.compiler.DefaultXsltFinder;
import org.apache.ode.bpel.compiler.wsdl.Definition4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactory4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactoryBPEL20;
import org.apache.ode.bpel.dd.DeployDocument;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TDeployment.Process;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.o.Serializer;
import org.w3c.dom.Node;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;

/**
 * Container providing various functions on the deployment directory.
 *
 * @author mriou
 * @author Maciej Szefler <mszefler at gmail dot com>
 */
class DeploymentUnitDir  {

    private static Log __log = LogFactory.getLog(DeploymentUnitDir.class);

    private String _name;
    private File _duDirectory;
    private File _descriptorFile;

    private HashMap<QName, CBPInfo> _processes = new HashMap<QName,CBPInfo>();
    private HashMap<QName, TDeployment.Process> _processInfo = new HashMap<QName,TDeployment.Process>();

    private volatile DeployDocument _dd;
    private volatile DocumentRegistry _docRegistry;

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

    DeploymentUnitDir(File dir) {
        if (!dir.exists())
            throw new IllegalArgumentException("Directory " + dir + " does not exist!");

        _duDirectory = dir;
        _name = dir.getName();
        _descriptorFile = new File(_duDirectory, "deploy.xml");

        if (!_descriptorFile.exists())
            throw new IllegalArgumentException("Directory " + dir + " does not contain a deploy.xml file!");
    }


    String getName() {
        return _duDirectory.getName();
    }

    CBPInfo getCBPInfo(QName typeName) {
        return _processes.get(typeName);
    }


    /**
     * Checking for each BPEL file if we have a corresponding compiled process. If we don't, 
     * starts compilation. 
     */
    void compile() {
        File[] bpels = _duDirectory.listFiles(DeploymentUnitDir._bpelFilter);
        for (File bpel : bpels) {
            File compiled = new File(bpel.getParentFile(), bpel.getName().substring(0,bpel.getName().length()-".bpel".length()) + ".cbp");
            if (compiled.exists() && compiled.lastModified() >= bpel.lastModified()) {
                continue;
            }
            compile(bpel);
        }
    }

    void scan() {
        HashMap<QName, CBPInfo> processes = new HashMap<QName, CBPInfo>();
        File[] cbps = _duDirectory.listFiles(DeploymentUnitDir._cbpFilter);
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

    private void compile(File bpelFile) {
        BpelC bpelc = BpelC.newBpelCompiler();
        bpelc.setOutputDirectory(_duDirectory);
        bpelc.setWsdlFinder(new DefaultWsdlFinder(_duDirectory));
        bpelc.setXsltFinder(new DefaultXsltFinder(_duDirectory));
        bpelc.setCompileProperties(prepareCompileProperties(bpelFile));
        try {
            bpelc.compile(bpelFile);
        } catch (IOException e) {
            __log.error("Compile error in " + bpelFile, e);
        }
    }

    /**
     * Load the parsed and compiled BPEL process definition.
     */
    private CBPInfo loadCBPInfo(File f) {
        InputStream is = null;
        try {
            is = new FileInputStream(f);
            Serializer ofh = new Serializer(is);
            CBPInfo info = new CBPInfo(ofh.type,ofh.guid,f);
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
        return ((DeploymentUnitDir)obj).getDeployDir().getAbsolutePath().equals(getDeployDir().getAbsolutePath());
    }

    public File getDeployDir() {
        return _duDirectory;
    }

    public DeployDocument getDeploymentDescriptor() {
        if (_dd == null) {
            File ddLocation = new File(_duDirectory, "deploy.xml");
            try {
                _dd = DeployDocument.Factory.parse(ddLocation);
            } catch (Exception e) {
                throw new ContextException("Couldn't read deployment descriptor at location "
                        + ddLocation.getAbsolutePath(), e);
            }

        }
        return _dd;
    }

    public DocumentRegistry getDocRegistry() {
        if (_docRegistry == null) {
            _docRegistry = new DocumentRegistry(new DocumentEntityResolver(_duDirectory));

            WSDLFactory4BPEL wsdlFactory = (WSDLFactory4BPEL) WSDLFactoryBPEL20.newInstance();
            WSDLReader r = wsdlFactory.newWSDLReader();

            ArrayList<File> wsdls = listFilesRecursively(_duDirectory, DeploymentUnitDir._wsdlFilter);
            for (File file : wsdls) {
                try {
                    _docRegistry.addDefinition((Definition4BPEL) r.readWSDL(file.toURI().toString()));
                } catch (WSDLException e) {
                    throw new ContextException("Couldn't read WSDL document " + file.getAbsolutePath(), e);
                }
            }
        }
        return _docRegistry;
    }

    public Definition getDefinitionForService(QName name) {
        return getDocRegistry().getDefinition(name);
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

    public TDeployment.Process getProcessDeployInfo(QName pid) {
        if (_processInfo == null) {
        }

        return _processInfo.get(pid);
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

    private ArrayList<File> listFilesRecursively(File root, FileFilter filter) {
        ArrayList<File> result = new ArrayList<File>();
        // Filtering the files we're interested in in the current directory
        File[] select = root.listFiles(filter);
        for (File file : select) {
            result.add(file);
        }
        // Then we can check the directories
        File[] all = root.listFiles();
        for (File file : all) {
            if (file.isDirectory())
                result.addAll(listFilesRecursively(file, filter));
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
            if (bpelFile.getName().equals(process.getFileName())) {
                Map<QName, Node> props = ProcessStoreImpl.calcInitialProperties(process);
                Map<String, Object> result = new HashMap<String, Object>();
                result.put(BpelC.PROCESS_CUSTOM_PROPERTIES, props);
                return result;
            }
        }
        return null;
    }

}
