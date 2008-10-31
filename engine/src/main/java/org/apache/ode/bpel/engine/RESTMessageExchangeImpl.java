package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.concurrent.*;

public class RESTMessageExchangeImpl extends MessageExchangeImpl implements RESTMessageExchange {

    private static final Log __log = LogFactory.getLog(RESTMessageExchangeImpl.class);

    private boolean _done = false;
    private ResponseFuture _future;

    private Resource _resource;

    public RESTMessageExchangeImpl(ODEProcess process, String mexId, Resource resource) {
        super(process, null, mexId, null, null, null);
        _resource = resource;
    }

    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.UNRELIABLE;
    }

    public Resource getResource() {
        return _resource;
    }

    public void setRequest(final Message request) {
        _request = (MessageImpl) request;
        _changes.add(Change.REQUEST);
    }
    
    public Status invokeBlocking() throws BpelEngineException, TimeoutException {
        if (_done) return getStatus();

        Future<Status> future = _future != null ? _future : invokeAsync();
        try {
            future.get(Math.max(_timeout, 1), TimeUnit.MILLISECONDS);
            _done = true;
            return getStatus();
        } catch (InterruptedException e) {
            throw new BpelEngineException(e);
        } catch (ExecutionException e) {
            throw new BpelEngineException(e.getCause());
        }
    }

    public Future<Status> invokeAsync() {
        if (_future != null) return _future;
        if (_request == null) throw new IllegalStateException("Must call setRequest(...)!");

        _future = new ResponseFuture();
        _process.enqueueTransaction(new Callable<Void>() {
            public Void call() throws Exception {
                MessageExchangeDAO dao = doInvoke();
                if (dao.getStatus() == Status.ACK) {
                    // not really an async ack, same idea.
                    onAsyncAck(dao);
                }
                return null;
            }
        });
        return _future;
    }

    protected MessageExchangeDAO doInvoke() {
        if (getStatus() != Status.NEW) throw new IllegalStateException("Invalid state: " + getStatus());
        request();

        MessageExchangeDAO dao = _process.createMessageExchange(getMessageExchangeId(), MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
        save(dao);
        if (__log.isDebugEnabled()) __log.debug("invoke() EPR= " + _epr + " ==> " + _process);
        try {
            _process.invokeProcess(dao);
        } finally {
            if (dao.getStatus() == Status.ACK) {
                _failureType = dao.getFailureType();
                _fault = dao.getFault();
                _explanation  = dao.getFaultExplanation();
                ack(dao.getAckType());
            }
        }
        return dao;
    }

    protected void onAsyncAck(MessageExchangeDAO mexdao) {
        final MemBackedMessageImpl response;
        final QName fault = mexdao.getFault();
        final FailureType failureType = mexdao.getFailureType();
        final AckType ackType = mexdao.getAckType();
        final String explanation = mexdao.getFaultExplanation();
        switch (mexdao.getAckType()) {
        case RESPONSE:
        case FAULT:
            response = new MemBackedMessageImpl(mexdao.getResponse().getHeader(),
                    mexdao.getResponse().getData(), mexdao.getResponse().getType(), false);
            break;
        default:
            response = null;
        }

        final ResponseFuture f = _future;
        // Lets be careful, the TX can still rollback!
        _process.scheduleRunnable(new Runnable() {
            public void run() {
                _response = response;
                _fault = fault;
                _failureType = failureType;
                _explanation = explanation;

                ack(ackType);
                _future.done(Status.ACK);

            }
        });
    }

    @Override
    void save(MessageExchangeDAO dao) {
        super.save(dao);
        dao.setResource(_resource.getUrl() + "~" + _resource.getMethod());

        if (_changes.contains(Change.REQUEST)) {
            _changes.remove(Change.REQUEST);
            MessageDAO requestDao = dao.createMessage(_request.getType());
            requestDao.setData(_request.getMessage());
            requestDao.setHeader(_request.getHeader());
            dao.setRequest(requestDao);
        }
    }

    @Override
    void load(MessageExchangeDAO dao) {
        super.load(dao);
        _resource = ((ODERESTProcess)_process).getResource(dao.getResource());
    }
}
