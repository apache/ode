/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa;

import com.fs.sax.evt.Characters;
import com.fs.sax.evt.SaxEvent;
import com.fs.sax.evt.StartElement;
import com.fs.utils.DOMUtils;

import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DOMGenerator {
	
  private Node _root; // this may be character node
  private Document _doc;
  private Stack<Element> _nodeStack = new Stack<Element>();
  
	public DOMGenerator(Document doc) {
		_doc = doc;
	}
  
  public DOMGenerator(){
  	_doc = DOMUtils.newDocument();
  }
  
  public Node getRoot(){
  	return _root;
  }
  
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    switch(se.getType()){
      case SaxEvent.START_ELEMENT:
        StartElement ste = (StartElement)se;
        Element e = _doc.createElementNS(ste.getName().getNamespaceURI(), ste.getName().getLocalPart());
        for(Iterator<QName> iter = ste.getAttributes().getQNames(); iter.hasNext(); ){
          QName attr = iter.next();
          e.setAttributeNS(attr.getNamespaceURI(), attr.getLocalPart(), ste.getAttributes().getValue(attr));
        }
        if(_nodeStack.isEmpty()){
          _doc.appendChild(e);
          // set root, even if already set as text node
          _root = e;
        }else{
          _nodeStack.peek().appendChild(e);
        }
        _nodeStack.add(e);
        break;
        
      case SaxEvent.END_ELEMENT:
        _nodeStack.pop();
        break;
        
      case SaxEvent.CHARACTERS:
        Node text = _doc.createTextNode(((Characters)se).getContent());
        if(!_nodeStack.isEmpty())
          _nodeStack.peek().appendChild(text);
              
        if(_root == null)
          _root = text;
        break;
      default:
        // do nothing
    }
  }
}
