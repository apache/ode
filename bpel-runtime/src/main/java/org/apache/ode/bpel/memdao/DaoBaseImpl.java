/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.memdao;

import org.apache.ode.utils.uuid.UUIDGen;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;


/**
 * Base-class for in-memory data-access objects.
 */
class DaoBaseImpl {
    private static final Log __logger = LogFactory.getLog(DaoBaseImpl.class);
    private static final UUIDGen __uuidGen = new UUIDGen();

    Date _createTime = new Date();
    String _uuid = __uuidGen.nextUUID();

    public Date getCreateTime() {
        return _createTime;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        __logger.debug("Finalizing " + this);
    }
}
