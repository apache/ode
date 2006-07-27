package org.apache.ode.bpel.epr;

import org.apache.ode.bpel.iapi.EndpointReference;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * Adds methods on {@link EndpointReference} to set and manipulate endpoint references.
 */
public interface MutableEndpoint extends EndpointReference {

  static final String ADDRESS = "address";
  static final String SESSION = "session";
  static final String SERVICE_QNAME = "service";
  static final String PORT_NAME = "port";
  static final String BINDING_QNAME = "binding";

  /**
   * Expresses the fact that the endpoint can be either tranformed to a
   * Map representation or initialized from a Map. Used for endpoint
   * conversion, to transform one endpoint type into another (using Map
   * as an intermediary format).
   */
  Map toMap();

  /**
   * Expresses the fact that the endpoint can be either tranformed to a
   * Map representation or initialized from a Map. Used for endpoint
   * conversion, to transform one endpoint type into another (using Map
   * as an intermediary format).
   */
  void fromMap(Map eprMap);

  /**
   * Checks if the type of the provided node is the right one for this
   * ServiceEndpoint implementation. The endpoint should be unwrapped
   * (without service-ref) before calling this method.
   * @param node
   * @return true if the node content matches the service endpoint implementation, false otherwise
   */
  boolean accept(Node node);

  /**
   * Set service endpoint value from an XML node.
   * @param node
   */
  void set(Node node);

  /**
   * @return endpoint target URL
   */
  String getUrl();

}
