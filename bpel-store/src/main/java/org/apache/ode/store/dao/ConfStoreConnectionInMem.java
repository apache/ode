package org.apache.ode.store.dao;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author mriou <mriou at apache dot org>
 */
public class ConfStoreConnectionInMem implements ConfStoreConnection {

    private List<ProcessConfDAO> _daos = new ArrayList<ProcessConfDAO>();

    public ProcessConfDAO createProcess(QName pid, QName type) {
        ProcessConfDAOInMem dao = new ProcessConfDAOInMem();
        dao.setActive(true);
        dao.setDeployDate(new Date());
        dao.setProcessId(pid);
        dao.setTypeName(type.getLocalPart());
        dao.setTypeNamespace(type.getNamespaceURI());
        dao.setVersion(0);
        _daos.add(dao);
        return dao;
    }

    public <T> T exec(Callable<T> callable) throws Exception {
        return callable.run();
    }

    public List<ProcessConfDAO> getActiveProcesses() {
        // In-memory deactivation is highly unlikely to be useful
        return _daos;
    }

    public ProcessConfDAO getProcessConf(QName pid) {
        for (ProcessConfDAO dao : _daos) {
            if (dao.getProcessId().equals(pid)) return dao;
        }
        return null;
    }
}
