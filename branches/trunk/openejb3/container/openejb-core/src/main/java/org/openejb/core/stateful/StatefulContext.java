package org.openejb.core.stateful;

import javax.xml.rpc.handler.MessageContext;
import javax.transaction.TransactionManager;

import org.openejb.RpcContainer;
import org.openejb.spi.SecurityService;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.ivm.EjbObjectProxyHandler;

public class StatefulContext extends org.openejb.core.CoreContext implements javax.ejb.SessionContext {
    
    public StatefulContext(TransactionManager transactionManager, SecurityService securityService) {
        super(transactionManager, securityService);
    }

    public void checkBeanState(byte methodCategory) throws IllegalStateException {
        /*  
        The methodCategory will be one of the following constants.

        SECURITY_METHOD:
        ROLLBACK_METHOD:
        EJBOBJECT_METHOD:
        EJBHOME_METHOD
        USER_TRANSACTION_METHOD:

        The super class, CoreContext determines if Context.getUserTransaction( ) method 
        maybe called before invoking this.checkBeanState( ).  Only "bean managed" transaction
        beans may access this method.

        The USER_TRANSACTION_METHOD will never be passed as a methodCategory in the SessionSynchronization 
        interface methods. The CoreContext won't allow it.

        */
        ThreadContext callContext = ThreadContext.getThreadContext();

        switch (callContext.getCurrentOperation()) {
            case Operations.OP_SET_CONTEXT:
                /* 
                Allowed Operations: 
                    getEJBHome
                Prohibited Operations:
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction
                */
                if (methodCategory != EJBHOME_METHOD)
                    throw new IllegalStateException("Invalid operation attempted");
                break;
            case Operations.OP_CREATE:
            case Operations.OP_REMOVE:
            case Operations.OP_ACTIVATE:
            case Operations.OP_PASSIVATE:
            case Operations.OP_AFTER_COMPLETION:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getCallerPrincipal
                    isCallerInRole
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction (not allowed in OP_AFTER_COMPLETION)
                Prohibited Operations:
                    getRollbackOnly,
                    setRollbackOnly
                */
                if (methodCategory == ROLLBACK_METHOD)
                    throw new IllegalStateException("Invalid operation attempted");
                else
                    break;
            case Operations.OP_BUSINESS:
            case Operations.OP_AFTER_BEGIN:
            case Operations.OP_BEFORE_COMPLETION:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getCallerPrincipal
                    isCallerInRole
                    getEJBObject
                    getPrimaryKey
                    getRollbackOnly,
                    setRollbackOnly
                    getUserTransaction (business methods only)
                Prohibited Operations:
                */
                break;
        }

    }

    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID) {
        return new StatefulEjbObjectHandler(container, pk, depID);
    }

    public MessageContext getMessageContext() {
        throw new UnsupportedOperationException("not implemented");
    }

    public Object getBusinessObject(Class businessInterface) {
        throw new UnsupportedOperationException("not implemented");
    }

    public Class getInvokedBusinessInterface() {
        throw new UnsupportedOperationException("not implemented");
    }
}