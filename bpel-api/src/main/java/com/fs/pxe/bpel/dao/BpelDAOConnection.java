/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.dao;

import com.fs.pxe.bpel.common.BpelEventFilter;
import com.fs.pxe.bpel.common.InstanceFilter;
import com.fs.pxe.bpel.common.ProcessFilter;
import com.fs.pxe.bpel.evt.BpelEvent;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;


/**
 * Represents the physical resource for connecting to the bpel state store.
 */
public interface BpelDAOConnection  {
  /**
   * Return the DAO for a bpel process.
   *
   * @param processId name (identifier) of the process
   *
   * @return DAO
   */
  ProcessDAO getProcess(QName processId);


  /**
   * Retrieve a process instance from the database.
   * @param iid instance identifier
   * @return process instance
   */
  ProcessInstanceDAO getInstance(Long iid);

  /**
   * Retrieve a scope instance from the database.
   * @param siidl scope instance identifier
   * @return scope instance
   */
  ScopeDAO getScope(Long siidl);

  /**
   * Query instances in the database meeting the requested
   * criteria.
   * @param criteria
   * @return Collection<ProcessInstanceDAO>
   */
  Collection<ProcessInstanceDAO> instanceQuery(InstanceFilter criteria);

  /**
   * Query processes in the database meeting the request criteria.
   * @param criteria
   * @return Collection<ProcessDAO>
   */
  Collection<ProcessDAO> processQuery(ProcessFilter criteria);

  /**
   * Insert a BPEL event into the database.
   * @param event a BPEL event
   * @param process associated process (optional)
   * @param instance associated instance (optional) 
   */
  void insertBpelEvent(BpelEvent event, ProcessDAO process, 
      ProcessInstanceDAO instance);

  /**
   * Execute a query for the timeline for BPEL events matching the criteria.
   * @param ifilter instance filter (optional)
   * @param efilter event filter (optional)
   * @return List of event timestamps of events matching the criteria
   */
  List<Date> bpelEventTimelineQuery(InstanceFilter ifilter, BpelEventFilter efilter);
  
  /**
   * Execute a query to retrieve the BPEL events matching the criteria.
   * @param ifilter instance filter
   * @param efilter event filter
   * @return
   */
  List<BpelEvent> bpelEventQuery(InstanceFilter ifilter, BpelEventFilter efilter);

  void close();
  
  Collection<ProcessInstanceDAO> instanceQuery(String expression);

  ProcessDAO createProcess(QName pid, QName type);


  /**
   * Create a message exchange.
   * @param dir type of message exchange
   * @return
   */
  MessageExchangeDAO createMessageExchange(char dir);

  MessageExchangeDAO getMessageExchange(String mexid);

}
