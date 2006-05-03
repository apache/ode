/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.sax.fsa;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

public abstract class AbstractGraphProvider implements GraphProvider{
  
  private Map<String,StateFactory> _nodes = new HashMap<String,StateFactory>();
  private Map<QNameStringPair,String> _qNameEdges = new HashMap<QNameStringPair,String>();
  private Map<String,UriStringPair> _otherUriEdges = new HashMap<String,UriStringPair>();

  public StateFactory getStateFactory(String name) {
    return _nodes.get(name);
  }
  
  /**
   * Add a state factory to the graph with the specified name.  This plays the role
   * of a labeled vertex.
   * @param name the name (label) for the vertex
   * @param sf the {@link StateFactory} that can produce handlers for the named state.
   */
  public void addStateFactory(String name, StateFactory sf) {
    _nodes.put(name,sf);
  }
  
  /**
   * Add an edge to the graph that connects one state to another based on an element
   * with the supplied <code>QName</code>.  Note that no effort is made to ensure
   * that the source and target vertices are present in the graph at the time that
   * the edge is added.
   * @param src the source vertex for the edge, by name
   * @param target the target vertex for the edge, by name
   * @param child the {@link QName} of the element that would trigger this transition.
   */
  public void addQNameEdge(String src, String target, QName child) {
    _qNameEdges.put(new QNameStringPair(src,child),target);
  }
  
  /**
   * <p>
   * Add an edge to the graph that connects one state to another based on a
   * &quot;<code>##other</code>&quot;-style match, i.e., any element outside of the
   * named namespace.
   * </p>
   * <p>
   * <em>N.B.</em>  Only one of these is stored per vertex, and the uri is only used
   * as a sanity check when determining non-matches.
   * </p>
   * @param src the source vertex for the edge, by name
   * @param target the target vertex for the edge, by name
   * @param uri the namespace URI not to match
   */
  public void addOtherEdge(String src, String target, String uri)  {
    _otherUriEdges.put(src, new UriStringPair(target,uri));
  }
  
  public String getOtherEdge(String src, String uri){
    UriStringPair notUri =  _otherUriEdges.get(src);
    if (notUri == null) {
      return null;
    } else if (! notUri.uri.equals(uri)) {
      return notUri.str;
    } else {
      return null;
    }
  }
  
  public String getQNameEdge(String src, QName child){
    return _qNameEdges.get(new QNameStringPair(src, child));
  }
  
  private class QNameStringPair {
    String src;
    QName qname;
    
    QNameStringPair(String s, QName q) {
      src = s;
      qname = q;
    }
    
    public boolean equals(Object o) {
      if (o == null) {
        return false;
      } else if (o instanceof QNameStringPair) {
        QNameStringPair qsp = (QNameStringPair) o;        
        return qsp.src.equals(src) && qsp.qname.equals(qname);
      } else {
        return false;
      }
    }
    
    public int hashCode() {
      return src.hashCode() | qname.hashCode(); 
    }
  }
  
  private class UriStringPair {
    String str;
    String uri;
    
    UriStringPair(String s, String u) {
      str = s;
      uri = u;
    }
    
    public boolean equals(Object o) {
      if (o == null) {
        return false;
      } else if (o instanceof UriStringPair) {
        UriStringPair usp = (UriStringPair) o;        
        return usp.str.equals(str) && usp.uri.equals(uri);
      } else {
        return false;
      }
    }
    
    public int hashCode() {
      return str.hashCode() | uri.hashCode();
    }
  }

}
