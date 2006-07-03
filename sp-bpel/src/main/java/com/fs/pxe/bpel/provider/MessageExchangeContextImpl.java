package com.fs.pxe.bpel.provider;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Decoder;

import com.fs.pxe.bpel.iapi.BpelEngineException;
import com.fs.pxe.bpel.iapi.ContextException;
import com.fs.pxe.bpel.iapi.MessageExchangeContext;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;
import com.fs.pxe.bpel.iapi.MessageExchange.FailureType;
import com.fs.pxe.sfwk.spi.MessageExchange;
import com.fs.pxe.sfwk.spi.MessageExchangeRef;
import com.fs.utils.ObjectPrinter;
import com.fs.utils.SerializableUtils;

class MessageExchangeContextImpl implements MessageExchangeContext {
  private static final Log __log = LogFactory.getLog(MessageExchangeContextImpl.class);

  private BpelServiceProvider _sp;
  BASE64Decoder decoder = new BASE64Decoder();
  
  MessageExchangeContextImpl(BpelServiceProvider provider) {
    _sp = provider;
  }

  public void invokePartner(PartnerRoleMessageExchange mex) throws ContextException {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("invokePartner", new Object[] {
          "mex", mex}));

    // We have to figure out which process we are 
    QName pid = mex.getCaller();
    BpelService service = _sp.findBpelServiceByProcessId(pid);
    if (service == null)
      mex.replyWithFailure(FailureType.OTHER,"Unknown caller.", null);
    else
      service.invokePartner(mex);
  }

  public void onAsyncReply(MyRoleMessageExchange myRoleMex) throws BpelEngineException {
    
    MessageExchangeRef sfwkRef;
    try {
      sfwkRef = (MessageExchangeRef) SerializableUtils.toObject(
          decoder.decodeBuffer(
          myRoleMex.getProperty("mexref")), getClass().getClassLoader());
    } catch (IOException e) {
      throw new BpelEngineException(e);
    }
    MessageExchange sfwkMex = sfwkRef.resolve();
    _sp.handleBpelResponse(sfwkMex, myRoleMex);
    
  }

}
