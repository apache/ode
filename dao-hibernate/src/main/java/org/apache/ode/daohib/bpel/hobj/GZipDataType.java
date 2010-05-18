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

package org.apache.ode.daohib.bpel.hobj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.usertype.UserType;

/**
 * Custom Hibernate datatype that compresses (GZip) byte arrays
 * to increase performance and save disk space.
 */
public class GZipDataType implements UserType {
    private static final Log log = LogFactory.getLog(GZipDataType.class);

    public static final int[] SQL_TYPES = new int[] { Types.BLOB };

    public static final Class RETURNED_CLASS = new byte[0].getClass();

    /** For backward compatibility with non-zipped data, prefix the gzip stream with a magic sequence */
    public static final byte[] GZIP_PREFIX = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x03, 0x02, 0x01, 0x00 };

    // Compression statistics
    private static long _totalBytesBefore = 0;
    private static long _totalBytesAfter = 0;
    private static volatile long _lastLogTime = 0;
    private static final Object STATS_LOCK = new Object();

    private static volatile boolean _compressionEnabled = System.getProperty("org.apache.ode.daohib.bpel.hobj.GZipDataType.enabled", "true").equalsIgnoreCase("true");

    /** Reconstruct an object from the cacheable representation */
    public Object assemble(Serializable cached, Object owner) {
        // serializable representation is same
        return cached;
    }

    /** Transform the object into its cacheable representation */
    public Serializable disassemble(Object value) {
        // as-is
        return (Serializable) value;
    }

    /** Return a deep copy of the persistent state */
    public Object deepCopy(Object value) {
        if (value == null) return null;
        return ((byte[]) value).clone();
    }

    /**  Compare two instances of the class mapped by this type for persistence "equality". */
    public boolean equals(Object x, Object y) {
        byte[] buf1 = (byte[]) x;
        byte[] buf2 = (byte[]) y;
        if (buf1 == buf2) return true;
        if (buf1 == null && buf2 != null) return false;
        if (buf1 != null && buf2 == null) return false;
        if (buf1.length != buf2.length) return false;
        for (int i=0; i<buf1.length; i++) {
            if (buf1[i] != buf2[i]) return false;
        }
        return true;
    }

    /** Get a hashcode for the instance, consistent with persistence "equality" */
    public int hashCode(Object x) {
        if (x == null) return 0;
        byte[] buf = (byte[]) x;
        int hash = 0;
        for (int i=0; i<buf.length; i++) {
            hash += buf[i];
        }
        return hash;
    }

    /** Are objects of this type mutable? */
    public boolean isMutable() {
        return false;
    }

    /** Retrieve an instance of the mapped class from a JDBC resultset. */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws SQLException {
        if (names.length != 1) throw new IllegalStateException("Expected a single column name instead of "+names.length);
        byte[] buf = rs.getBytes(names[0]);
        if (buf == null) {
            return null;
        }
        if (buf.length >= GZIP_PREFIX.length) {
            boolean gzip = true;
            for (int i=0; i<GZIP_PREFIX.length; i++) {
                if (buf[i] != GZIP_PREFIX[i]) {
                    gzip = false;
                    break;
                }
            }
            if (gzip) {
                buf = gunzip(new ByteArrayInputStream(buf, GZIP_PREFIX.length, buf.length-GZIP_PREFIX.length));
            }
        }
        return buf;
    }

    /** Write an instance of the mapped class to a prepared statement. */
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws SQLException {
        byte[] buf = (byte[]) value;
        if (buf != null) {
            synchronized (STATS_LOCK) {
                if (_totalBytesBefore > Integer.MAX_VALUE) {
                    // prevent overflow - renormalize to percent value
                    _totalBytesAfter = _totalBytesAfter*100/_totalBytesBefore;
                    _totalBytesBefore = 100;
                }
                _totalBytesBefore += buf.length;
            }
            // only try to zip if we have more than 100 bytes
            if (buf != null && buf.length > 100 && _compressionEnabled) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(buf.length);
                for (int i=0; i<GZIP_PREFIX.length; i++) {
                    baos.write(GZIP_PREFIX[i]);
                }
                gzip((byte[]) value, baos);
                byte[] zipped = baos.toByteArray();
                // only use zipped representation if we gain 2% or more
                if (zipped.length*100/buf.length < 99) {
                    buf = zipped;
                }
            }
            synchronized (STATS_LOCK) {
                _totalBytesAfter += buf.length;
            }
            if (log.isDebugEnabled()) {
                long now = System.currentTimeMillis();
                if (_lastLogTime+5000 < now) {
                    log.debug("Average compression ratio: "+ (_totalBytesAfter*100/_totalBytesBefore)+"%");
                    _lastLogTime = now;
                }
            }
        }
        st.setBytes(index, buf);
    }

    /** During merge, replace the existing (target) value in the entity we are
     *  merging to with a new (original) value from the detached entity we are merging.
     */
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    /** The class returned by nullSafeGet(). */
    public Class returnedClass() {
        return RETURNED_CLASS;
    }

    /** Return the SQL type codes for the columns mapped by this type. */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * Compress (using gzip algorithm) a byte array into an output stream.
     */
    public static void gzip(byte[] content, OutputStream out) {
        try {
            GZIPOutputStream zip = new GZIPOutputStream(out);
            zip.write(content, 0, content.length);
            zip.finish();
            zip.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Decompress (using gzip algorithm) a byte array.
     */
    public static byte[] gunzip(InputStream input) {
        try {
            GZIPInputStream unzip = new GZIPInputStream(input);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(32*1024);
            byte[] buf = new byte[4096];
            int len;
            while ((len = unzip.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            unzip.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
