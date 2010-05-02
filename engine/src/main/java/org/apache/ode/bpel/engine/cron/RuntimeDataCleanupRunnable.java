package org.apache.ode.bpel.engine.cron;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.dao.bpel.BpelDAOConnection;
import org.apache.ode.dao.bpel.FilteredInstanceDeletable;
import org.apache.ode.bpel.engine.Contexts;
import org.apache.ode.bpel.engine.BpelServerImpl.ContextsAware;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.bpel.iapi.ProcessConf.CleanupInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.bpel.iapi.Scheduler.MapSerializableRunnable;

public class RuntimeDataCleanupRunnable implements MapSerializableRunnable, ContextsAware {
    private final Log _log = LogFactory.getLog(RuntimeDataCleanupRunnable.class);

    private static final long serialVersionUID = 1L;

    private transient Contexts _contexts;

    private int _transactionSize;
    private CleanupInfo _cleanupInfo;
    private QName _pid;
    private Set<QName> _pidsToExclude;
    
    public RuntimeDataCleanupRunnable() {
    }
    
    @SuppressWarnings("unchecked")
    public void restoreFromDetails(JobDetails details) {
        _cleanupInfo = (CleanupInfo)details.getDetailsExt().get("cleanupInfo");
        _transactionSize = (Integer)details.getDetailsExt().get("transactionSize");
        _pid = (QName) details.getDetailsExt().get("pid");
        _pidsToExclude = (Set<QName>)details.getDetailsExt().get("pidsToExclude");
    }

    public void storeToDetails(JobDetails details) {
        // we don't serialize
    }

    public void setContexts(Contexts contexts) {
        _contexts = contexts;
    }
    
    public void run() {
        _log.info("CRON CLEAN.run().");
        try {
            for( String filter : _cleanupInfo.getFilters() ) {
                _log.info("CRON CLEAN.run(" + filter + ")");

                if( _pid != null ) {
                    filter += " pid=" + _pid;
                } else if( _pidsToExclude != null ) {
                    StringBuffer pids = new StringBuffer();
                    for( QName pid : _pidsToExclude ) {
                        if( pids.length() > 0 ) {
                            pids.append("|");
                        }
                        pids.append(pid);
                    }
                    filter += " pid<>" + pids.toString();
                }

                if( filter.trim().length() > 0 ) {
                    long numberOfDeletedInstances = 0;
                    do {
                        numberOfDeletedInstances = cleanInstances(filter, _cleanupInfo.getCategories(), _transactionSize);
                    } while( numberOfDeletedInstances == _transactionSize );
                }
            }
        } catch( RuntimeException re ) {
            throw re;
        } catch( Exception e ) {
            throw new RuntimeException("", e);
        }
    }
    
    int cleanInstances(String filter, final Set<CLEANUP_CATEGORY> categories, int limit) {
        _log.debug("CleanInstances using filter: " + filter + ", limit: " + limit);

        final InstanceFilter instanceFilter = new InstanceFilter(filter, "", limit);
        try {
            if (_contexts.scheduler != null) {
                return _contexts.execTransaction(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        BpelDAOConnection con = _contexts.dao.getConnection();
                        if( con instanceof FilteredInstanceDeletable ) {
                            return ((FilteredInstanceDeletable)con).deleteInstances(instanceFilter, categories);
                        }
                        return 0;
                    }
                });
            } else {
                return 0;
                }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Exception while listing instances: ", e);
        }
    }
}
