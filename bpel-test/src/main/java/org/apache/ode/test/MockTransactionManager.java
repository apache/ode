package org.apache.ode.test;

import java.util.ArrayList;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

/**
 * A minimal transaction manager that can be used for testing.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * 
 */
public class MockTransactionManager implements TransactionManager {
    ThreadLocal<TX> _transaction = new ThreadLocal<TX>();

    public void begin() throws NotSupportedException, SystemException {
        if (_transaction.get() != null)
            throw new NotSupportedException("Transaction active (nested tx not supported): " + _transaction.get());

        _transaction.set(new TX());
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException,
            SecurityException, SystemException {
        if (_transaction.get() == null)
            throw new IllegalStateException("Transaction not active. ");

        try {
            _transaction.get().commit();
        } finally {
            _transaction.set(null);
        }
    }

    public int getStatus() throws SystemException {
        if (_transaction.get() == null)
            return Status.STATUS_NO_TRANSACTION;

        return _transaction.get().getStatus();

    }

    public Transaction getTransaction() throws SystemException {
        return _transaction.get();
    }

    public void resume(Transaction tx) throws IllegalStateException, InvalidTransactionException, SystemException {
        if (_transaction.get() != null)
            throw new IllegalStateException("Transaction is active in current thread: " + _transaction.get());
        try {
            _transaction.set((TX) tx);
        } catch (ClassCastException cce) {
            throw new InvalidTransactionException();
        }

    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        if (_transaction.get() == null)
            throw new IllegalStateException("Transaction not active. ");

        try {
            _transaction.get().rollback();
        } finally {
            _transaction.set(null);
        }
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (_transaction.get() == null)
            throw new IllegalStateException("Transaction not active. ");

        _transaction.get().setRollbackOnly();

    }

    public void setTransactionTimeout(int arg0) throws SystemException {
        // TODO Auto-generated method stub

    }

    public Transaction suspend() throws SystemException {
        try {
            return _transaction.get();
        } finally {
            _transaction.set(null);
        }
    }
    

    protected void doBegin(TX tx) {}
    protected void doCommit(TX tx) {}
    protected void doRollback(TX tx){}

    public class TX implements Transaction {

        final ArrayList<XAResource> _resources = new ArrayList<XAResource>();

        final ArrayList<Synchronization> _synchros = new ArrayList<Synchronization> ();

        private int _status;

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException,
                SystemException {
            switch (_status) {
            case Status.STATUS_COMMITTED:
                return;
            case Status.STATUS_MARKED_ROLLBACK:
                rollback();
                throw new RollbackException("Transaction was marked for rollback!");
            case Status.STATUS_ACTIVE:
                fireBefore();
                if (_status == Status.STATUS_MARKED_ROLLBACK) {
                    rollback();
                    throw new RollbackException("Transaction was marked for rollback in beforeCompletion handler.");
                }
                _status = Status.STATUS_COMMITTING;
                try {
                    doCommit(this);
                    _status = Status.STATUS_COMMITTED;
                } catch (Exception ex) {
                    _status = Status.STATUS_ROLLEDBACK;
                    throw new RollbackException("Transaction was rolled back due to commit failure." );
                } finally {
                    fireAfter();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected transaction state.");
            }
        }

        public boolean delistResource(XAResource arg0, int arg1) throws IllegalStateException, SystemException {
            // TODO: perhaps we should do something with the resources?
            _resources.remove(arg0);
            return true;
        }

        public boolean enlistResource(XAResource r) throws IllegalStateException, RollbackException, SystemException {
            return _resources.add(r);
        }

        public int getStatus() throws SystemException {
            return _status;
        }

        public void registerSynchronization(Synchronization synch) throws IllegalStateException, RollbackException, SystemException {
            _synchros.add(synch);
        }

        public void rollback() throws IllegalStateException, SystemException {
            // TODO Auto-generated method stub
            switch (_status) {
            case Status.STATUS_ROLLEDBACK:
                return;
            case Status.STATUS_MARKED_ROLLBACK:
            case Status.STATUS_ACTIVE:
                _status = Status.STATUS_ROLLING_BACK;
                try {
                    doRollback(this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    _status = Status.STATUS_ROLLEDBACK;
                    fireAfter();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected transaction state.");
            }
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            switch (_status) {
            case Status.STATUS_ACTIVE:
            case Status.STATUS_MARKED_ROLLBACK:
                _status = Status.STATUS_MARKED_ROLLBACK;
                break;
            case Status.STATUS_ROLLEDBACK:
            case Status.STATUS_ROLLING_BACK:
                break;
            default:
                throw new IllegalStateException();
            }
        }

        private void fireBefore() {
            for (Synchronization s : _synchros)
                try {
                    s.beforeCompletion();
                } catch (Throwable t) {
                    ; // ignore errors.
                }

        }

        private void fireAfter() {
            for (Synchronization s : _synchros)
                try {
                    s.afterCompletion(_status);
                } catch (Throwable t) {
                    ; // ignore errors.
                }

        }

    }

}
