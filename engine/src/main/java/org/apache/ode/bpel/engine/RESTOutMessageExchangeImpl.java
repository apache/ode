package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.w3c.dom.Element;

public class RESTOutMessageExchangeImpl extends MessageExchangeImpl implements RESTOutMessageExchange {

    private Resource _resource;

    protected State _state = State.INVOKE_XXX;

    /** the states for a partner mex. */
    enum State {
        /** state when we're in one of the MexContext.invokeXXX methods. */
        INVOKE_XXX,
        /** hold all actions (blocks the IL) */
        HOLD,
        /** the MEX is dead, it should no longer be accessed by the IL */
        DEAD
    };

    public RESTOutMessageExchangeImpl(ODEProcess process, Long iid, String mexId, Resource resource) {
        super(process, iid, mexId, null, null, null);
        _resource = resource;
    }

    public Resource getTargetResource() {
        return _resource;
    }

    public void reply(Message response) throws BpelEngineException {
        _response = (MessageImpl) response;
        _fault = null;
        _failureType = null;
        ack(AckType.RESPONSE);
        save();
    }

    public void replyWithFailure(FailureType type, String description, Element details) throws BpelEngineException {
        _failureType = type;
        _explanation = description;
        _fault = null;
        _response = null;
        ack(AckType.FAILURE);
        save();
    }

    public void replyOneWayOk() {
        ack(AckType.ONEWAY);
        save();
    }

    @Override
    void load(MessageExchangeDAO dao) {
        super.load(dao);
        String resStr = dao.getResource();
        int sepIdx = resStr.indexOf("~");
        _resource = new Resource(resStr.substring(0, sepIdx), "application/xml", resStr.substring(sepIdx+1));
    }

    @Override
    void save(MessageExchangeDAO dao) {
        super.save(dao);
        dao.setResource(_resource.getUrl() + "~" + _resource.getMethod());
        if (_response != null) {
            MessageDAO responseDao = dao.createMessage(_response.getType());
            responseDao.setData(_response.getMessage());
            responseDao.setHeader(_response.getHeader());
            dao.setResponse(responseDao);
        }        
    }

    public void setState(State s) {
        _state = s;
    }

    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.UNRELIABLE;
    }
}
