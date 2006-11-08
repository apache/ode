package org.apache.ode.store.deploy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.BpelC;
import org.apache.ode.bpel.compiler.DefaultWsdlFinder;
import org.apache.ode.bpel.compiler.DefaultXsltFinder;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.wsdl.Definition4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactory4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactoryBPEL20;
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
import java.io.*;
import java.util.*;

/**
 * Container providing various functions on the deployment directory.
 *
 * @author mriou
 *
 * TODO Add a way to cause lazy methods to re-process stuff on disk.
 */
public class DeploymentUnitImpl implements DeploymentUnit {

    private static Log __log = LogFactory.getLog(DeploymentUnitImpl.class);

    private String _name;
    private File _duDirectory;
    private DocumentRegistry _docRegistry;
    private HashMap<QName, OProcess> _processes;
    private DeployDocument _dd;
    private File _descriptorFile;
    private HashMap<QName, TDeployment.Process> _processInfo;
    private boolean _refreshed;

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

        refresh();
    }


    /**
     * Checking for each BPEL file if we have a corresponding compiled process.
     * If we don't, starts compilation. The force parameter just forces
     * compilation, whether a cbp file exists or not.
     */
    private void compileProcesses(boolean force) {
        ArrayList<File> bpels = listFilesRecursively(_duDirectory, DeploymentUnitImpl._bpelFilter);
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
            DeploymentUnitImpl.__log.error("Couldn't compile process file!", e);
        }
    }

    private void loadProcessDefinitions(boolean force) {
        try {
            compileProcesses(force);
        } catch (CompilationException e) {
            // No retry on compilation error, we just forget about it
            throw new BpelEngineException("Compilation failure!", e);
        }
        if (_processes == null || force) {
            _processes = new HashMap<QName, OProcess>();
            ArrayList<File> cbps = listFilesRecursively(_duDirectory, DeploymentUnitImpl._cbpFilter);
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

    public int hashCode() {
        return _name.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DeploymentUnitImpl)) return false;
        return ((DeploymentUnitImpl)obj).getDeployDir().getAbsolutePath().equals(getDeployDir().getAbsolutePath());
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

            ArrayList<File> wsdls = listFilesRecursively(_duDirectory, DeploymentUnitImpl._wsdlFilter);
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
        if (!_refreshed) {
            loadProcessDefinitions(true);
            _refreshed = true;
        }
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
}
