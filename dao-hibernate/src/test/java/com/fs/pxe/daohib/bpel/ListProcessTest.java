/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel;

import com.fs.pxe.bpel.common.ProcessFilter;
import com.fs.pxe.bpel.dao.BpelDAOConnection;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.sfwk.DAOConnectionFactoryImpl;
import junit.framework.TestCase;
import org.objectweb.jotm.Jotm;

import java.io.FileInputStream;
import java.util.*;

/**
 * Testing BpelDAOConnectionImpl.listProcesses. We're just producing a lot
 * of different filter combinations and test if they execute ok. To really
 * test that the result is the one expected would take a huge test database
 * (with at least a process and an instance for every possible combination).
 */
public class ListProcessTest extends TestCase {

  private Map<String, List> filterElmts;
  private ArrayList<String> order;

  private BpelDAOConnection daoConn;
  private Jotm jotm;

  protected void setUp() throws Exception {
    Properties hibProps = new Properties();
    hibProps.load(new FileInputStream("../dao-hibernate/src/hibernate/derby.properties"));

    jotm = new Jotm(true, false);

    SessionManager sessMgr = new SessionManager(hibProps, jotm.getTransactionManager());
    new DAOConnectionFactoryImpl(sessMgr);

    jotm.getTransactionManager().begin();

    BpelDAOConnectionFactoryImpl factoryImpl = new BpelDAOConnectionFactoryImpl(sessMgr);
    daoConn = factoryImpl.createConnection();

    buildFilterElements();
  }

  protected void tearDown() throws Exception {
    jotm.getTransactionManager().commit();
  }

  public void testListProcess() throws Exception {
    for (int index = 0; index < 5; index++) {
      StringBuffer filter = new StringBuffer();
      for (Map.Entry<String, List> entry : filterElmts.entrySet()) {
        filter.append(entry.getKey());
        filter.append(entry.getValue().get((index < entry.getValue().size()) ? index : index % entry.getValue().size()));
        ProcessFilter pfilter = new ProcessFilter(filter.toString(),
                order.get((index < order.size()) ? index : index % order.size()));
        daoConn.processQuery(pfilter);
      }
    }
  }

  private void buildFilterElements() {
    filterElmts = new HashMap<String, List>();
    ArrayList<String> nameList = new ArrayList<String>();
    nameList.add("=Hello* ");
    nameList.add("=HelloWorld ");
    filterElmts.put("name", nameList);

    ArrayList<String> namespaceList = new ArrayList<String>();
    namespaceList.add("=http://pxe* ");
    namespaceList.add("=http://pxe ");
    filterElmts.put("namespace", namespaceList);

    // Note: process status is not implemented yet, generated queries won't
    // reflect this attribute.
    ArrayList<String> statusList = new ArrayList<String>();
    statusList.add("=activated ");
    statusList.add("=retired ");
    filterElmts.put("status", statusList);

    ArrayList<String> deployedList = new ArrayList<String>();
    deployedList.add(">=2005-11-29T15:11 ");
    deployedList.add("<=2005-11-29T15:11 ");
    deployedList.add("<2005-11-29T15:11 deployed>=2005-11-29T15:11 ");
    deployedList.add(">2005-11-29T15:11 deployed<=2005-11-29T15:11 ");
    deployedList.add("=2005-11-29T15:11 ");
    filterElmts.put("deployed", deployedList);

    order = new ArrayList<String>();
    order.add("name ");
    order.add("namespace ");
    order.add("version ");
    order.add("status ");
    order.add("deployed ");

  }

}
