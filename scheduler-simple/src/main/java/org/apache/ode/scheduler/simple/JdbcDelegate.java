package org.apache.ode.scheduler.simple;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final String UPGRADE_JOB_DEFAULT = "update ODE_JOB set nodeid = ? where nodeid is null and scheduled = 0 "
            + "and mod(ts,?) = ? and ts < ?";

    private static final String UPGRADE_JOB_SQLSERVER = "update ODE_JOB set nodeid = ? where nodeid is null and scheduled = 0 "
        + "and (ts % ?) = ? and ts < ?";

    private static final String SAVE_JOB = "insert into ODE_JOB "
            + " (jobid, nodeid, ts, scheduled, transacted, details) values(?, ?, ?, ?, ?, ?)";

    private static final String GET_NODEIDS = "select distinct nodeid from ODE_JOB";

    private static final String SCHEDULE_IMMEDIATE = "select jobid, ts, transacted, scheduled, details from ODE_JOB "
            + "where nodeid = ? and scheduled = 0 and ts < ? order by ts";

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
            con = getConnection();
            ps = con.prepareStatement(SAVE_JOB);
            ps.setString(1, job.jobId);
            ps.setString(2, nodeId);
            ps.setLong(3, job.schedDate);
            ps.setInt(4, asInteger(loaded));
            ps.setInt(5, asInteger(job.transacted));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                StreamUtils.write(bos, (Serializable) job.detail);
            } catch (Exception ex) {
                __log.error("Error serializing job detail: " + job.detail);
                throw new DatabaseException(ex);
            }
            ps.setBytes(6, bos.toByteArray());
            return ps.executeUpdate() == 1;
        } catch (SQLException se) {
            throw new DatabaseException(se);
        } finally {
            close(ps);
            close(con);
        }
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
                Map<String, Object> details;
                try {
                    ObjectInputStream is = new ObjectInputStream(rs.getBinaryStream(5));
                    details = (Map<String, Object>) is.readObject();
                    is.close();
                } catch (Exception e) {
                    throw new DatabaseException("Error deserializing job details", e);
                }
                Job job = new Job(rs.getLong(2), rs.getString(1), asBoolean(rs.getInt(3)), details);
                ret.add(job);
            }
            rs.close();
            ps.close();
            
            // mark jobs as scheduled, UPDATE_SCHEDULED_SLOTS at a time
            int j = 0;
            int updateCount = 0;
            ps = con.prepareStatement(UPDATE_SCHEDULED);
            for (int updates = 1; updates <= (ret.size() / UPDATE_SCHEDULED_SLOTS) + 1; updates++) {
                for (int i = 1; i <= UPDATE_SCHEDULED_SLOTS; i++) {
                    ps.setString(i, j < ret.size() ? ret.get(j).jobId : "");
                    j++;
                }
                ps.execute();
                updateCount += ps.getUpdateCount();
            }
            if (updateCount != ret.size()) {
                throw new DatabaseException(
                        "Updating scheduled jobs failed to update all jobs; expected=" + ret.size()
                                + " actual=" + updateCount);
            }
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
        return _ds.getConnection();
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
                __log.debug("Using database " + dbProductName + " major version " + dbMajorVer);
                if (dbProductName.indexOf("DB2") >= 0) {
                    d = Dialect.DB2;
                } else if (dbProductName.indexOf("Derby") >= 0) {
                    d = Dialect.DERBY;
                } else if (dbProductName.indexOf("Firebird") >= 0) {
                    d = Dialect.FIREBIRD;
                } else if (dbProductName.indexOf("HSQL") >= 0) {
                    d = Dialect.HSQL;
                } else if (dbProductName.indexOf("Microsoft SQL") >= 0) {
                    d = Dialect.SQLSERVER;
                } else if (dbProductName.indexOf("MySQL") >= 0) {
                    d = Dialect.MYSQL;
                } else if (dbProductName.indexOf("Sybase") >= 0) {
                    d = Dialect.SYBASE;
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

    enum Dialect {
        DB2, DERBY, FIREBIRD, HSQL, MYSQL, ORACLE, SQLSERVER, SYBASE, UNKNOWN 
    }
    
}
