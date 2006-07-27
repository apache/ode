
package org.apache.ode.bpel.common;

import org.apache.ode.utils.msg.MessageBundle;

import java.util.Collection;

/**
 * Human-readable messages for the common classes.
 */
public class Messages extends MessageBundle {

  /**
   * The filter "{0}" is not recognized; try one of {1}.
   */
  public String msgUnrecognizedFilterKey(String filterKey, Collection<String> filterKeys) {
    return this.format("The filter \"{0}\" is not recognized; try one of {1}.", filterKey,
        filterKeys);
  }

  /**
   * The restriction for filter "{0}" must follow the ISO-8601 date or date/time
   * standard (yyyyMMddhhmmss); "{1}" does not follow this form.
   */
  public String msgISODateParseErr(String filterKey, String restriction) {
    return this.format("The restriction for filter \"{0}\" must follow the"
        + " ISO-8601 date or date/time standard (yyyyMMddhhmmss);"
        + "\"{1}\" does not follow this form.", filterKey, restriction);
  }

}
