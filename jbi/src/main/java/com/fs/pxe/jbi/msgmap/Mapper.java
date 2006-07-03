package com.fs.pxe.jbi.msgmap;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.Operation;

import com.fs.pxe.bpel.iapi.Message;



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
   * Convert a PXE message to NMS format. This call must only be called
   * if {@link #isRecognized(NormalizedMessage, Operation)} returned,
   * <code>true</code>.
   * @param nmsMsg
   * @param pxeMsg
   * @param msgdef
   * @throws MessagingException
   * @throws MessageTranslationException
   */
  void toNMS(NormalizedMessage nmsMsg, Message pxeMsg, javax.wsdl.Message msgdef)
      throws MessagingException, MessageTranslationException;

  /**
   * Convert an NMS message to PXE format. This call must only be called
   * if {@link #isRecognized(NormalizedMessage, Operation)} returned,
   * <code>true</code>.
   * @param pxeMsg
   * @param nmsMsg
   * @param msgdef
   * @throws MessageTranslationException
   */
  void toPXE(Message pxeMsg, NormalizedMessage nmsMsg, javax.wsdl.Message msgdef)
      throws MessageTranslationException;

  

  enum Recognized {
    TRUE,
    FALSE,
    UNSURE
  }
}
