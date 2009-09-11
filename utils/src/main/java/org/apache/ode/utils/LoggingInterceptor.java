package org.apache.ode.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;

public class LoggingInterceptor<T> implements InvocationHandler {

	private static final Set<String> PARAMSTYPES = new HashSet<String>();
	static {
		PARAMSTYPES.add("setArray");
		PARAMSTYPES.add("setBigDecimal");
		PARAMSTYPES.add("setBoolean");
		PARAMSTYPES.add("setByte");
		PARAMSTYPES.add("setBytes");
		PARAMSTYPES.add("setDate");
		PARAMSTYPES.add("setDouble");
		PARAMSTYPES.add("setFloat");
		PARAMSTYPES.add("setInt");
		PARAMSTYPES.add("setLong");
		PARAMSTYPES.add("setObject");
		PARAMSTYPES.add("setRef");
		PARAMSTYPES.add("setShort");
		PARAMSTYPES.add("setString");
		PARAMSTYPES.add("setTime");
		PARAMSTYPES.add("setTimestamp");
		PARAMSTYPES.add("setURL");
	}
	
	private Log _log;
	private T _delegate;
    private TreeMap<String, Object> _paramsByName = new TreeMap<String, Object>();
    private TreeMap<Integer, Object> _paramsByIdx = new TreeMap<Integer, Object>();


	public LoggingInterceptor(T delegate, Log log) {
		_log = log;
		_delegate = delegate;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		try {
			if (method.getDeclaringClass() == DataSource.class 
					&& "getConnection".equals(method.getName())) {
				Connection conn = (Connection)method.invoke(_delegate, args);
				print("getConnection (tx=" + conn.getTransactionIsolation() + ")");
				return Proxy.newProxyInstance(_delegate.getClass().getClassLoader(), 
						new Class[] {Connection.class}, new LoggingInterceptor<Connection>(conn, _log));
			} else if (method.getDeclaringClass() == Connection.class 
					&& Statement.class.isAssignableFrom(method.getReturnType())) {
				Statement stmt = (Statement)method.invoke(_delegate, args);
				print(method, args);
				return Proxy.newProxyInstance(_delegate.getClass().getClassLoader(), 
						new Class[] {method.getReturnType()}, new LoggingInterceptor<Statement>(stmt, _log));
			} else {
				print(method, args);
				return method.invoke(_delegate, args);
			}
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
	}

	private void print(Method method, Object[] args) {
        if (shouldPrint()) {
        	// JDBC Connection
        	if ("prepareStatement".equals(method.getName())) {
        		print("prepareStmt: " + args[0]);
        	} else if ("prepareCall".equals(method.getName())) {
        		print("prepareCall: " + args[0]);
        	} else if ("close".equals(method.getName())) {
        		print("close()");
        	} else if ("commit".equals(method.getName())) {
        		print("commit()");
        	} else if ("rollback".equals(method.getName())) {
        		print("rollback()");
        	} else if ("setTransactionIsolation".equals(method.getName())) {
        		print("Set isolation level to " + args[0]);
        	} 
        	// JDBC Statement
        	  else if (method.getName().startsWith("execute")) {
        		print(method.getName() + ", " + getParams());
        	} else if ("clearParameters".equals(method.getName())) {
        		_paramsByIdx.clear();
        		_paramsByName.clear();
        	} else if ("setNull".equals(method.getName())) {
        		if (String.class.isAssignableFrom(args[0].getClass())) {
        			_paramsByName.put((String)args[0], null);
        		} else if (Integer.class.isAssignableFrom(args[0].getClass())) {
        			_paramsByIdx.put((Integer)args[0], null);
        		}
        	} else if (PARAMSTYPES.contains(method.getName())){
        		if (String.class.isAssignableFrom(args[0].getClass())) {
        			_paramsByName.put((String)args[0], args[1]);
        		} else if (Integer.class.isAssignableFrom(args[0].getClass())) {
        			_paramsByIdx.put((Integer)args[0], args[1]);
        		}
        	}
        }
	}
	
    private String getParams() {
        if (_paramsByIdx.size() > 0 || _paramsByName.size() > 0) {
            StringBuffer buf = new StringBuffer();
            buf.append("bound ");
            for (Map.Entry<Integer, Object> entry : _paramsByIdx.entrySet()) {
                try {
                    buf.append("(").append(entry.getKey()).append(",").append(entry.getValue()).append(") ");
                } catch (Throwable e) {
                    // We don't want to mess with the connection just for logging
                	return "[e]";
                }
            }
            for (Map.Entry<String, Object> entry : _paramsByName.entrySet()) {
                try {
                    buf.append("(").append(entry.getKey()).append(",").append(entry.getValue()).append(") ");
                } catch (Throwable e) {
                    // We don't want to mess with the connection just for logging
                    return "[e]";
                }
            }
            return buf.toString();
        }
		return "w/o params";
    }

	private boolean shouldPrint() {
        if (_log != null)
            return _log.isDebugEnabled();
        else return true;
    }

    private void print(String str) {
        if (_log != null)
            _log.debug(str);
        else System.out.println(str);
    }
    
    public static DataSource createLoggingDS(DataSource ds, Log log) {
		return (DataSource)Proxy.newProxyInstance(ds.getClass().getClassLoader(), 
				new Class[] {DataSource.class}, new LoggingInterceptor<DataSource>(ds,log));
    }
}
