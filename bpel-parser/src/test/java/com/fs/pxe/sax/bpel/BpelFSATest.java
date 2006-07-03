/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.bpel;

import com.fs.pxe.bom.api.*;
import com.fs.pxe.bom.api.Process;
import com.fs.pxe.bpel.parser.BpelParseException;
import com.fs.pxe.bpel.parser.BpelProcessBuilder;
import com.fs.pxe.bpel.parser.BpelProcessBuilderFactory;
import com.fs.pxe.bpel.parser.BpelProcessBuilderFactoryException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BpelFSATest extends TestCase {
  
  private static final Log __log = LogFactory.getLog(BpelFSATest.class);

  private BpelProcessBuilder _pb; 

  public BpelFSATest(String s) {
    super(s);
  }
  
  public void setUp() {
  	try {
  		_pb = BpelProcessBuilderFactory.newProcessBuilderFactory().newBpelProcessBuilder();
  	}
  	catch (BpelProcessBuilderFactoryException ex) {
  		fail("Could not create BpelProcessBuilderFactory: " + ex);
  	}
  }
  
  private Process goodSmoke(String name) throws SAXException, IOException, BpelParseException {
    InputStream is = BpelFSATest.class.getResourceAsStream(name);
    if (is == null) {
      throw new RuntimeException("Unable to locate testing resource " + name);
    }
    InputSource isr = new InputSource(is);
    isr.setSystemId(name);
    return _pb.parse(isr,"");
  }
  
  private void badSmoke(String name) throws Exception {
    try {
      goodSmoke(name);
    } catch (SAXException se) {
        __log.debug(se.getMessage());
        return;
    } catch (BpelParseException pe) {
        __log.debug(pe.getMessage());
        return;
    }
    fail("bad BPEL " + name + " did not cause a failure.");
  }
  
  public void testGoodSmokeHelloWorld() throws Exception {
    Process p = goodSmoke("HelloWorld.bpel");
    Activity seq = getActivity(p.getRootActivity(), "seq");
    Activity assign = getActivity(p.getRootActivity(), "assign1");
    ReceiveActivity start = (ReceiveActivity)getActivity(p.getRootActivity(), "start");
    ReplyActivity end = (ReplyActivity)getActivity(p.getRootActivity(), "end");
    assertNotNull(seq);
    assertNotNull(assign);
    assertNotNull(start);
    assertNotNull(end);
    
    Set vars = p.getVariables();
    assertEquals(2, vars.size());
    for(Iterator iter = vars.iterator(); iter.hasNext(); ){
    	Variable v = (Variable)iter.next();
      if(v.getName().equals("myVar")){
      	assertEquals("HelloMessage", v.getTypeName().getLocalPart());
      }else if(v.getName().equals("tmpVar")){
      	assertEquals("string", v.getTypeName().getLocalPart());
      }else{
      	assertTrue(false);
      }
    }
    
    Set partners = p.getPartnerLinks();
    assertEquals(1, partners.size());
    PartnerLink plink = (PartnerLink)partners.iterator().next();
    assertEquals("helloPartnerLink", plink.getName());
    assertEquals("me", plink.getMyRole());
    assertNull(plink.getPartnerRole());
    assertEquals("HelloPartnerLinkType", plink.getPartnerLinkType().getLocalPart());
    
    List copies = ((AssignActivity)assign).getCopies();
    assertEquals(2, copies.size());
    
    {
      // first copy check
      Copy c1 = (Copy)copies.get(0);
      VariableVal from1 = (VariableVal)c1.getFrom();
      assertNotNull("from1", from1);
      assertEquals("myVar", from1.getVariable());
      assertEquals("TestPart", from1.getPart());
      VariableVal to1 = (VariableVal)c1.getTo();
      assertNotNull("to1", to1);
      assertEquals("tmpVar", to1.getVariable());
      assertNull(to1.getPart());
    }
    
    {
      // 2nd copy check
      Copy c = (Copy)copies.get(1);
      ExpressionVal from = (ExpressionVal)c.getFrom();
      assertNotNull("from2", from);
      assertNotNull("expr", from.getExpression());
      VariableVal to = (VariableVal)c.getTo();
      assertNotNull("to2", to);
      assertEquals("myVar", to.getVariable());
      assertEquals("TestPart", to.getPart());
    }
    
    assertEquals("helloPartnerLink", start.getPartnerLink());
    assertEquals("HelloPortType", start.getPortType().getLocalPart());
    assertEquals("hello", start.getOperation());
    assertEquals("myVar", start.getVariable());
    assertTrue(start.getCreateInstance());
    
    assertEquals("helloPartnerLink", end.getPartnerLink());
    assertEquals("HelloPortType", end.getPortType().getLocalPart());
    assertEquals("hello", end.getOperation());
    assertEquals("myVar", end.getVariable());
    
  }

  public void testGoodSmokeRetailAggregator() throws Exception {
    Process p = goodSmoke("RetailAggregator.bpel");
    Set cdecls = p.getCorrelationSetDecls();
    assertEquals(1, cdecls.size());
    CorrelationSet cs = p.getCorrelationSetDecl("customerId");
    QName[] props = cs.getProperties();
    assertEquals(1, props.length);
    assertEquals("customerId", props[0].getLocalPart());
    {
      // receive 'setupSession'
      ReceiveActivity receive = (ReceiveActivity)getActivity(p.getRootActivity(), "setupSession");
      List l = receive.getCorrelations(Correlation.CORRPATTERN_IN);
      assertEquals(1, l.size());
      Correlation c = (Correlation)l.get(0);
      assertEquals("customerId", c.getCorrelationSet());
      assertEquals(Correlation.INITIATE_YES, c.getInitiate());
    }
    {
    	// assign 'assign2'
      AssignActivity assign = (AssignActivity)getActivity(p.getRootActivity(), "assign2");
      List copies = assign.getCopies();
      assertEquals(1, copies.size());
      Copy c = (Copy)copies.get(0);
      VariableVal v = (VariableVal)c.getFrom();
      assertEquals("setupInfo", v.getVariable());
      assertEquals("CustomerIdPart", v.getPart());
      assertNotNull(v.getLocation());
      assertNotNull(v.getLocation().getXPathString());
    }
    {
    	// while
      WhileActivity wa = (WhileActivity)getActivity(p.getRootActivity(), "while");
      assertNotNull(wa.getCondition());
      assertNotNull(wa.getCondition().getXPathString());
      assertNotNull(wa.getActivity());
    }
    {
      ReceiveActivity rcv = (ReceiveActivity)getActivity(p.getRootActivity(), "aggregatorRequest");
      List cors = rcv.getCorrelations(Correlation.CORRPATTERN_IN);
      assertEquals(1, cors.size());
      Correlation c = (Correlation)cors.get(0);
      assertEquals("customerId", c.getCorrelationSet());
      assertEquals(Correlation.INITIATE_NO, c.getInitiate());
    }
    {
      InvokeActivity invoke = (InvokeActivity)getActivity(p.getRootActivity(), "invokeRetailOrder");
      assertEquals("retailerPartnerLink", invoke.getPartnerLink());
      assertEquals("retailOrder", invoke.getInputVar());
      assertEquals("orderResponse", invoke.getOutputVar());
      assertEquals("submitOrder", invoke.getOperation());
      assertEquals("RetailerPortType", invoke.getPortType().getLocalPart()); 
    }
  }
  
  public void testBadSmokeHelloWorld() throws Exception {
    badSmoke("bad_wrongNS.bpel");
  }
  
  public void testGoodHelloWorldExtensibility() throws Exception {
    goodSmoke("HelloWorld_Extensibility.bpel");
  }
  public void testGoodHelloWorldWithRDFLabels() throws Exception {
      goodSmoke("HelloWorld_WithRDFLabels.bpel");
    }
  public void testGoodAsync11Process() throws Exception {
  	_testGoodAsync11Process(goodSmoke("AsyncProcess.bpel"));
  }

  public void _testGoodAsyncProcess(Process p) throws Exception {
      
      {
        // pick check
        PickActivity pick = (PickActivity)getActivity(p.getRootActivity(), "pick");
        assertFalse(pick.getCreateInstance());
        Set events = pick.getOnMessages();
        assertEquals(1, events.size());
        OnMessage evt = (OnMessage)events.iterator().next();
        assertEquals("Callback", evt.getOperation());
        assertEquals("Response", evt.getVariable());
        assertEquals("AsyncResponder", evt.getPartnerLink());
        assertEquals("CallbackPT", evt.getPortType().getLocalPart());
        List cors = evt.getCorrelations(Correlation.CORRPATTERN_IN);
        assertEquals(1, cors.size());
        Correlation c = (Correlation)cors.get(0);
        assertEquals("OrderCorrelator", c.getCorrelationSet());
        assertEquals(Correlation.INITIATE_NO, c.getInitiate());
        assertTrue(pick.getOnAlarms().isEmpty());
      }
      {
        // switch check
        SwitchActivity sact = (SwitchActivity)getActivity(p.getRootActivity(), "switch");
        List cases = sact.getCases();
        assertEquals(2, cases.size());
        SwitchActivity.Case c1 = (SwitchActivity.Case)cases.get(0);
        assertNotNull(c1.getCondition());
        SwitchActivity.Case c2  = (SwitchActivity.Case)cases.get(1);
        assertNull(c2.getCondition());
      }
      {
        // BookOrder Invoke
        InvokeActivity invoke = (InvokeActivity)getActivity(p.getRootActivity(), "BookOrderInvoke");
        List cors = invoke.getCorrelations(Correlation.CORRPATTERN_OUT);
        assertEquals(1, cors.size());
        Correlation c = (Correlation)cors.get(0);
        assertEquals("OrderCorrelator", c.getCorrelationSet());
        assertEquals(Correlation.INITIATE_YES, c.getInitiate());
      }
    }
  
  public void _testGoodAsync20Process(Process p) throws Exception {
      _testGoodAsyncProcess(p);
    {
      // assign check
      AssignActivity assign = (AssignActivity)getActivity(p.getRootActivity(), "assign");
      List copies = assign.getCopies();
      assertEquals(3, copies.size());
      Copy copy1 = (Copy)copies.get(0);
      From from1 = copy1.getFrom();
      assertTrue(from1 instanceof LiteralVal);
      assertNotNull(((LiteralVal)from1).getLiteral());
      Copy copy2 = (Copy)copies.get(1);
      From from2 = copy2.getFrom();
      assertTrue(from2 instanceof ExpressionVal);
      //assertNotNull(((ExpressionVal)from2).getExpression().getNode());
      To to2 = copy2.getTo();
      
      assertTrue("to2 is " + to2.getClass(), to2 instanceof ExpressionVal);
      assertNotNull(((ExpressionVal)to2).getExpression());
      
    }
  }
  
  public void _testGoodAsync11Process(Process p) throws Exception {
      _testGoodAsyncProcess(p);
      {
        // assign check
        AssignActivity assign = (AssignActivity)getActivity(p.getRootActivity(), "assign");
        List copies = assign.getCopies();
        assertEquals(3, copies.size());
        Copy copy1 = (Copy)copies.get(0);
        From from1 = copy1.getFrom();
        assertTrue(from1 instanceof LiteralVal);
        assertNotNull(((LiteralVal)from1).getLiteral());
        Copy copy2 = (Copy)copies.get(1);
        From from2 = copy2.getFrom();
        assertTrue(from2 instanceof ExpressionVal);
        //assertNotNull(((ExpressionVal)from2).getExpression().getNode());
        To to2 = copy2.getTo();
        
        assertTrue("to2 is " + to2.getClass(), to2 instanceof VariableVal);
        assertNotNull(((VariableVal)to2).getLocation());
      }
    }

  public void testGoodFault() throws Exception {
  	Process p = goodSmoke("Fault1.bpel");
    {
    	ScopeActivity scope = (ScopeActivity)getActivity(p.getRootActivity(), "scope1");
      FaultHandler f = scope.getFaultHandler();
      assertNotNull(f);
      Catch[] catches = f.getCatches();
      assertEquals(1, catches.length);
      assertEquals(catches[0].getFaultName().getLocalPart(), "uninitializedVariable");
    }
  }
  
  public void testGoodCompensation() throws Exception {
    Process p = goodSmoke("comp1.bpel");
    {
      // compensation scope
      ScopeActivity scope = (ScopeActivity)getActivity(p.getRootActivity(), "s2");
      CompensationHandler ch = scope.getCompensationHandler();
      assertNotNull(ch);
      assertNotNull(ch.getActivity());
    }
    {
    	// compensate scope
      ScopeActivity scope = (ScopeActivity)getActivity(p.getRootActivity(), "s1");
      FaultHandler fh = scope.getFaultHandler();
      assertNotNull(fh);
      Catch[] catches = fh.getCatches();
      assertEquals(1, catches.length);
      CompensateActivity comp = (CompensateActivity)catches[0].getActivity();
      assertNotNull(comp);
      assertEquals(comp.getScopeToCompensate(),"s2");
    }
  }
  
  public void testGoodFlow3() throws Exception {
    Process p = goodSmoke("flow3.bpel");
    {
      FlowActivity flow = (FlowActivity)getActivity(p.getRootActivity(), "f1");
      Set links = flow.getLinks();
      assertEquals(1, links.size());
      Link link = (Link)links.iterator().next();
      assertEquals("link-a", link.getLinkName());
    }
    
    {
      Activity a = getActivity(p.getRootActivity(), "a");
      Set sources = a.getLinkSources();
      assertEquals(1, sources.size());
      LinkSource src = (LinkSource)sources.iterator().next();
      assertEquals("link-a", src.getLinkName());
      assertNotNull(src.getTransitionCondition());
      assertNotNull(src.getTransitionCondition().getXPathString());
    }
    {
      Activity a = getActivity(p.getRootActivity(), "b");
      Set targets = a.getLinkTargets();
      assertEquals(1, targets.size());
      LinkTarget target = (LinkTarget)targets.iterator().next();
      assertEquals("link-a", target.getLinkName());
    }
    {
      Activity a = getActivity(p.getRootActivity(), "c");
      Expression join = a.getJoinCondition();
      assertNotNull(join);
      assertNotNull(join.getXPathString());
    }
    
  }
  
  public void testGoodAsync20Process() throws Exception {
    _testGoodAsync20Process(goodSmoke("AsyncProcess_20.bpel"));
  }

  private Activity getActivity(Activity current, String name){
    if(current == null)
      return null;
    
    Activity matched = null;
  	if(current.getName() != null && current.getName().equals(name))
      matched = current;
    if(matched == null && current instanceof CompositeActivity){
    	for(Iterator iter = ((CompositeActivity)current).getChildren().iterator(); iter.hasNext(); ){
        Activity child = (Activity)iter.next();
        matched = getActivity(child, name);
        if(matched != null)
          break;
      }
    }
    if(matched == null && current instanceof ScopeActivity){
    	matched = getActivity(((ScopeActivity)current).getChildActivity(), name);
    }
    if(matched == null && current instanceof PickActivity){
    	for(Iterator iter = ((PickActivity)current).getOnMessages().iterator(); iter.hasNext(); ){
    		Activity child = ((OnMessage)iter.next()).getActivity();
        matched = getActivity(child, name);
        if(matched != null)
          break;
      }
      if(matched == null && ((PickActivity)current).getOnAlarms() != null){
        Set onas = ((PickActivity)current).getOnAlarms();
        // there is only one, so...
        OnAlarm oneh = (OnAlarm) onas.iterator().next();
        Activity child = oneh.getActivity();
        matched = getActivity(child, name);
      }
    }
    if(matched == null && current instanceof SwitchActivity){
    	for(Iterator iter = ((SwitchActivity)current).getCases().iterator(); iter.hasNext(); ){
    		Activity child = ((SwitchActivity.Case)iter.next()).getActivity();
        matched = getActivity(child, name);
        if(matched != null)
          break;
      }
    }
    if(matched == null && current instanceof WhileActivity){
    	Activity child = ((WhileActivity)current).getActivity();
      matched = getActivity(child, name);
    }
    if(matched == null && current instanceof Scope){
    	Scope slc = (Scope)current;
      if(slc.getCompensationHandler() != null){
      	matched = getActivity(slc.getCompensationHandler().getActivity(), name);
      }
      if(matched == null && slc.getFaultHandler() != null){
      	Catch[] catches = slc.getFaultHandler().getCatches();
        for(int i = 0; i < catches.length; ++i){
        	matched = getActivity(catches[i].getActivity(), name);
          if(matched != null)
            break;
        }
      }
    }
    return matched;
  }
  
  
  
}
