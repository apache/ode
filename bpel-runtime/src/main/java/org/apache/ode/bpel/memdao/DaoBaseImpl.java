/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.memdao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;


/**
 * Base-class for in-memory data-access objects.
 */
class DaoBaseImpl {
    private static final Log __logger = LogFactory.getLog(DaoBaseImpl.class);

    Date _createTime = new Date();

    public Date getCreateTime() {
        return _createTime;
    }

//    protected void finalize() throws Throwable {
//        super.finalize();
//        __logger.debug("Finalizing " + this);
//    }
}
