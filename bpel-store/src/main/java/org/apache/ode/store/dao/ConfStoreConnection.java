package org.apache.ode.store.dao;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dusty
 * Date: Oct 31, 2006
 * Time: 11:30:54 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ConfStoreConnection {
    ProcessConfDAO getProcessConf(QName pid);

    List<ProcessConfDAO> getActiveProcesses();

    ProcessConfDAO createProcess(QName pid, QName type);

    <T> T exec(Callable<T> callable) throws Exception;

    public interface Callable<T> {
        public T run() throws Exception;
    }
}
