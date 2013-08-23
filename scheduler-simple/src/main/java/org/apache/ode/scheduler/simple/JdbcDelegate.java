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

package org.apache.ode.scheduler.simple;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.utils.DbIsolation;
import org.apache.ode.utils.StreamUtils;

/**
 * JDBC-based implementation of the {@link DatabaseDelegate} interface. Should work with most 
 * reasonably behaved databases. 
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class JdbcDelegate implements DatabaseDelegate {

    private static final Log __log = LogFactory.getLog(JdbcDelegate.class);

    private static final String DELETE_JOB = "delete from ODE_JOB where jobid = ? and nodeid = ?";

    private static final String UPDATE_REASSIGN = "update ODE_JOB set nodeid = ?, scheduled = 0 where nodeid = ?";

    private static final String UPDATE_JOB = "update ODE_JOB set ts = ?, retryCount = ? where jobid = ?";

    private static final String UPGRADE_JOB_DEFAULT = "update ODE_JOB set nodeid = ? where nodeid is null "
            + "and mod(ts,?) = ? and ts < ?";

    private static final String UPGRADE_JOB_DB2 = "update ODE_JOB set nodeid = ? where nodeid is null "
            + "and mod(ts,CAST(? AS BIGINT)) = ? and ts < ?";
    
    private static final String UPGRADE_JOB_SQLSERVER = "update ODE_JOB set nodeid = ? where nodeid is null "
            + "and (ts % ?) = ? and ts < ?";

    private static final String UPGRADE_JOB_SYBASE = "update ODE_JOB set nodeid = ? where nodeid is null "
            + "and convert(int, ts) % ? = ? and ts < ?";

    private static final String UPGRADE_JOB_SYBASE12 = "update ODE_JOB set nodeid = ? where nodeid is null "
            + "and -1 <> ? and -1 <> ? and ts < ?";
    
    private static final String SAVE_JOB = "insert into ODE_JOB "
            + " (jobid, nodeid, ts, scheduled, transacted, "
            + "instanceId,"
            + "mexId,"
            + "processId,"
            + "type,"
            + "channel,"
            + "correlatorId,"
            + "correlationKeySet,"
            + "retryCount,"
            + "inMem,"
            + "detailsExt"
            + ") values(?, ?, ?, ?, ?,"
            + "?,"
            + "?,"
            + "?,"
            + "?,"
            + "?,"
            + "?,"
            + "?,"
            + "?,"
            + "?,"
            + "?"
            + ")";

    private static final String GET_NODEIDS = "select distinct nodeid from ODE_JOB";

    private static final String SCHEDULE_IMMEDIATE = "select jobid, ts, transacted, scheduled, "
        + "instanceId,"
        + "mexId,"
        + "processId,"
        + "type,"
        + "channel,"
        + "correlatorId,"
        + "correlationKeySet,"
        + "retryCount,"
        + "inMem,"
        + "detailsExt"
        + " from ODE_JOB "
            + "where nodeid = ? and ts < ? order by ts";

//  public Long instanceId;
//  public String mexId;
//  public String processId;
//  public String type;
//  public String channel;
//  public String correlatorId;
//  public String correlationKeySet;
//  public Integer retryCount;
//  public Boolean inMem;
//  public Map<String, Object> detailsExt = new HashMap<String, Object>();    
    
    private static final String UPDATE_SCHEDULED = "update ODE_JOB set scheduled = 1 where jobid in (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final int UPDATE_SCHEDULED_SLOTS = 10;

    private DataSource _ds;

    private Dialect _dialect;
    
    public JdbcDelegate(DataSource ds) {
        _ds = ds;
        _dialect = guessDialect();
    }

    public boolean deleteJob(String jobid, String nodeId) throws DatabaseException {
        if (__log.isDebugEnabled())
            __log.debug("deleteJob " + jobid + " on node " + nodeId);

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(DELETE_JOB);
            ps.setString(1, jobid);
            ps.setString(2, nodeId);
            return ps.executeUpdate() == 1;
        } catch (SQLException se) {
            throw new DatabaseException(se);
        } finally {
            close(ps);
            close(con);
        }
    }

    public List<String> getNodeIds() throws DatabaseException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(GET_NODEIDS, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = ps.executeQuery();
            ArrayList<String> nodes = new ArrayList<String>();
            while (rs.next()) {
                String nodeId = rs.getString(1);
                if (nodeId != null)
                    nodes.add(rs.getString(1));
            }
            if (__log.isDebugEnabled())
                __log.debug("getNodeIds: " + nodes);
            return nodes;
        } catch (SQLException se) {
            throw new DatabaseException(se);
        } finally {
            close(ps);
            close(con);
        }
    }

    public boolean insertJob(Job job, String nodeId, boolean loaded) throws DatabaseException {
        if (__log.isDebugEnabled())
            __log.debug("insertJob " + job.jobId + " on node " + nodeId + " loaded=" + loaded);

        Connection con = null;
        PreparedStatement ps = null;
        try {
            int i = 1;
            con = getConnection();
            ps = con.prepareStatement(SAVE_JOB);
            ps.setString(i++, job.jobId);
            ps.setString(i++, nodeId);
            ps.setLong(i++, job.schedDate);
            ps.setInt(i++, asInteger(loaded));
            ps.setInt(i++, asInteger(job.transacted));
            
            JobDetails details = job.detail;
            ps.setObject(i++, details.instanceId, Types.BIGINT);
            ps.setObject(i++, details.mexId, Types.VARCHAR);
            ps.setObject(i++, details.processId, Types.VARCHAR);
            ps.setObject(i++, details.type, Types.VARCHAR);
            ps.setObject(i++, details.channel, Types.VARCHAR);
            ps.setObject(i++, details.correlatorId, Types.VARCHAR);
            ps.setObject(i++, details.correlationKeySet, Types.VARCHAR);
            ps.setObject(i++, details.retryCount, Types.INTEGER);
            ps.setObject(i++, details.inMem, Types.INTEGER);
            
            if (details.detailsExt == null || details.detailsExt.size() == 0) {
                ps.setObject(i++, null, Types.BLOB);
            } else {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    StreamUtils.write(bos, (Serializable) details.detailsExt);
                } catch (Exception ex) {
                    __log.error("Error serializing job detail: " + job.detail);
                    throw new DatabaseException(ex);
                }
                ps.setBytes(i++, bos.toByteArray());
            }
            
            return ps.executeUpdate() == 1;
        } catch (SQLException se) {
            throw new DatabaseException(se);
        } finally {
            close(ps);
            close(con);
        }
    }

    public boolean updateJob(Job job) throws DatabaseException {
        if (__log.isDebugEnabled())
            __log.debug("updateJob " + job.jobId + " retryCount=" + job.detail.getRetryCount());

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(UPDATE_JOB);
            ps.setLong(1, job.schedDate);
            ps.setInt(2, job.detail.getRetryCount());
            ps.setString(3, job.jobId);
            return ps.executeUpdate() == 1;
        } catch (SQLException se) {
            throw new DatabaseException(se);
        } finally {
            close(ps);
            close(con);
        }
    }

    private Long asLong(Object o) {
        if (o == null) return null;
        else if (o instanceof BigDecimal) return ((BigDecimal) o).longValue();
        else if (o instanceof Long) return (Long) o;
        else if (o instanceof Integer) return ((Integer) o).longValue();
        else throw new IllegalStateException("Can't convert to long " + o.getClass());
    }

    private Integer asInteger(Object o) {
        if (o == null) return null;
        else if (o instanceof BigDecimal) return ((BigDecimal) o).intValue();
        else if (o instanceof Integer) return (Integer) o;
        else throw new IllegalStateException("Can't convert to integer " + o.getClass());
    }

    @SuppressWarnings("unchecked")
    public List<Job> dequeueImmediate(String nodeId, long maxtime, int maxjobs) throws DatabaseException {
        ArrayList<Job> ret = new ArrayList<Job>(maxjobs);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(SCHEDULE_IMMEDIATE);
            ps.setString(1, nodeId);
            ps.setLong(2, maxtime);
            ps.setMaxRows(maxjobs);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Scheduler.JobDetails details = new Scheduler.JobDetails();
                details.instanceId = asLong(rs.getObject("instanceId"));
                details.mexId = (String) rs.getObject("mexId");
                details.processId = (String) rs.getObject("processId");
                details.type = (String) rs.getObject("type");
                details.channel = (String) rs.getObject("channel");
                details.correlatorId = (String) rs.getObject("correlatorId");
                details.correlationKeySet = (String) rs.getObject("correlationKeySet");
                details.retryCount = asInteger(rs.getObject("retryCount"));
                details.inMem = asBoolean(rs.getInt("inMem"));
                if (rs.getObject("detailsExt") != null) {
                    try {
                        ObjectInputStream is = new ObjectInputStream(rs.getBinaryStream("detailsExt"));
                        details.detailsExt = (Map<String, Object>) is.readObject();
                        is.close();
                    } catch (Exception e) {
                        throw new DatabaseException("Error deserializing job detailsExt", e);
                    }
                }
                
                {
                    //For compatibility reasons, we check whether there are entries inside
                    //jobDetailsExt blob, which correspond to extracted entries. If so, we
                    //use them.

                    Map<String, Object> detailsExt = details.getDetailsExt();
                    if (detailsExt.get("type") != null) {
                        details.type = (String) detailsExt.get("type");
                    }
                    if (detailsExt.get("iid") != null) {
                        details.instanceId = (Long) detailsExt.get("iid");
                    }
                    if (detailsExt.get("pid") != null) {
                        details.processId = (String) detailsExt.get("pid");
                    }
                    if (detailsExt.get("inmem") != null) {
                        details.inMem = (Boolean) detailsExt.get("inmem");
                    }
                    if (detailsExt.get("ckey") != null) {
                        details.correlationKeySet = (String) detailsExt.get("ckey");
                    }
                    if (detailsExt.get("channel") != null) {
                        details.channel = (String) detailsExt.get("channel");
                    }
                    if (detailsExt.get("mexid") != null) {
                        details.mexId = (String) detailsExt.get("mexid");
                    }
                    if (detailsExt.get("correlatorId") != null) {
                        details.correlatorId = (String) detailsExt.get("correlatorId");
                    }
                    if (detailsExt.get("retryCount") != null) {
                        details.retryCount = Integer.parseInt((String) detailsExt.get("retryCount"));
                    }
                }
                
                Job job = new Job(rs.getLong("ts"), rs.getString("jobid"), asBoolean(rs.getInt("transacted")), details);
                ret.add(job);
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            throw new DatabaseException(se);
        } finally {
            close(ps);
            close(con);
        }
        return ret;
    }

    public int updateReassign(String oldnode, String newnode) throws DatabaseException {
        if (__log.isDebugEnabled())
            __log.debug("updateReassign from " + oldnode + " ---> " + newnode);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(UPDATE_REASSIGN);
            ps.setString(1, newnode);
            ps.setString(2, oldnode);
            return ps.executeUpdate();
        } catch (SQLException se) {
            throw new DatabaseException(se);
        } finally {
            close(ps);
            close(con);
        }
    }

    public int updateAssignToNode(String node, int i, int numNodes, long maxtime) throws DatabaseException {
        if (__log.isDebugEnabled())
            __log.debug("updateAsssignToNode node=" + node + " " + i + "/" + numNodes + " maxtime=" + maxtime);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            if (_dialect == Dialect.SQLSERVER) {
                ps = con.prepareStatement(UPGRADE_JOB_SQLSERVER);
            } else if (_dialect == Dialect.DB2) {
                ps = con.prepareStatement(UPGRADE_JOB_DB2);
            } else if (_dialect == Dialect.SYBASE) {
                ps = con.prepareStatement(UPGRADE_JOB_SYBASE);
            } else if (_dialect == Dialect.SYBASE12) {
                ps = con.prepareStatement(UPGRADE_JOB_SYBASE12);
            } else {
                ps = con.prepareStatement(UPGRADE_JOB_DEFAULT);
            }
            ps.setString(1, node);
            ps.setInt(2, numNodes);
            ps.setInt(3, i);
            ps.setLong(4, maxtime);
            return ps.executeUpdate();
        } catch (SQLException se) {
            throw new DatabaseException(se);
        } finally {
            close(ps);
            close(con);
        }
    }

    private Connection getConnection() throws SQLException {
        Connection c = _ds.getConnection();
        DbIsolation.setIsolationLevel(c);
        return c;
    }

    private int asInteger(boolean value) {
        return (value ? 1 : 0);
    }

    private boolean asBoolean(int value) {
        return (value != 0);
    }

    private void close(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (Exception e) {
                __log.warn("Exception while closing prepared statement", e);
            }
        }
    }

    private void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                __log.warn("Exception while closing connection", e);
            }
        }
    }

    private Dialect guessDialect() {
        Dialect d = Dialect.UNKNOWN;
        Connection con = null;
        try {
            con = getConnection();
            DatabaseMetaData metaData = con.getMetaData();
            if (metaData != null) {
                String dbProductName = metaData.getDatabaseProductName();
                int dbMajorVer = metaData.getDatabaseMajorVersion();
                __log.info("Using database " + dbProductName + " major version " + dbMajorVer);
                if (dbProductName.indexOf("DB2") >= 0) {
                    d = Dialect.DB2;
                } else if (dbProductName.indexOf("Derby") >= 0) {
                    d = Dialect.DERBY;
                } else if (dbProductName.indexOf("Firebird") >= 0) {
                    d = Dialect.FIREBIRD;
                } else if (dbProductName.indexOf("HSQL") >= 0) {
                    d = Dialect.HSQL;
                } else if (dbProductName.indexOf("H2") >= 0) {
                    d = Dialect.H2;
                } else if (dbProductName.indexOf("Microsoft SQL") >= 0) {
                    d = Dialect.SQLSERVER;
                } else if (dbProductName.indexOf("MySQL") >= 0) {
                    d = Dialect.MYSQL;
                } else if (dbProductName.indexOf("Sybase") >= 0 || dbProductName.indexOf("Adaptive") >= 0) {
                    d = Dialect.SYBASE;
                    if( dbMajorVer == 12 ) {
                        d = Dialect.SYBASE12;
                    }
                }
            }
        } catch (SQLException e) {
            __log.warn("Unable to determine database dialect", e);
        } finally {
            close(con);
        }
        __log.info("Using database dialect: " + d);
        return d;
    }

    public void acquireTransactionLocks() {
        Statement s = null;
        Connection c = null;
        try {
            c = getConnection();
            s = c.createStatement();
            s.execute("update ODE_JOB set jobid = '' where 1 = 0");
        } catch (Exception e) {
            throw new RuntimeException("", e);
        } finally {
            try {
                if (s != null) s.close();
                if (c != null) c.close();
            } catch (Exception e) {
                throw new RuntimeException("", e);
            }
        }
    }
    
    enum Dialect {
        DB2, DERBY, FIREBIRD, HSQL, MYSQL, ORACLE, SQLSERVER, SYBASE, SYBASE12, H2, UNKNOWN 
    }
    
}
