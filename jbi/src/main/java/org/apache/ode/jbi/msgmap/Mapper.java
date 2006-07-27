package org.apache.ode.jbi.msgmap;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.Operation;

import org.apache.ode.bpel.iapi.Message;



/**
 * Interface implemented by message format converters. 
 * TODO: Perhaps we should move this into the engine and make it pluggable?
 */
public interface Mapper {
  
  
  /**
   * Determine if this mapper recognizes the format of the NMS message.
   * 
   * @param nmsMsg
   * @return
   */
  Recognized isRecognized(NormalizedMessage nmsMsg, Operation op);

  /**
   * Convert a ODE message to NMS format. This call must only be called
   * if {@link #isRecognized(NormalizedMessage, Operation)} returned,
   * <code>true</code>.
   * @param nmsMsg
   * @param odeMsg
   * @param msgdef
   * @throws MessagingException
   * @throws MessageTranslationException
   */
  void toNMS(NormalizedMessage nmsMsg, Message odeMsg, javax.wsdl.Message msgdef)
      throws MessagingException, MessageTranslationException;

  /**
   * Convert an NMS message to ODE format. This call must only be called
   * if {@link #isRecognized(NormalizedMessage, Operation)} returned,
   * <code>true</code>.
   * @param odeMsg
   * @param nmsMsg
   * @param msgdef
   * @throws MessageTranslationException
   */
  void toODE(Message odeMsg, NormalizedMessage nmsMsg, javax.wsdl.Message msgdef)
      throws MessageTranslationException;

  

  enum Recognized {
    TRUE,
    FALSE,
    UNSURE
  }
}
