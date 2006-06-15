package org.openejb.core.stateless;

import javax.xml.rpc.handler.MessageContext;
import javax.transaction.TransactionManager;

import org.openejb.RpcContainer;
import org.openejb.spi.SecurityService;
import org.openejb.core.DeploymentInfo;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.ivm.EjbObjectProxyHandler;

public class StatelessContext
        extends org.openejb.core.CoreContext implements javax.ejb.SessionContext {
    public StatelessContext(TransactionManager transactionManager, SecurityService securityService) {
        super(transactionManager, securityService);
    }

    public void checkBeanState(byte methodCategory) throws IllegalStateException {
        /*  
        SECURITY_METHOD:
        USER_TRANSACTION_METHOD:
        ROLLBACK_METHOD:
        EJBOBJECT_METHOD:

        The super class, CoreContext determines if Context.getUserTransaction( ) method 
        maybe called before invoking this.checkBeanState( ).  Only "bean managed" transaction
        beans may access this method.

        */
        ThreadContext callContext = ThreadContext.getThreadContext();
        DeploymentInfo di = callContext.getDeploymentInfo();

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
                /* 
                Allowed Operations: 
                    getEJBHome
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction
                Prohibited Operations:
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                */
                if (methodCategory == EJBHOME_METHOD
                        || methodCategory == EJBOBJECT_METHOD
                        || methodCategory == USER_TRANSACTION_METHOD)
                    break;
                else
                    throw new IllegalStateException("Invalid operation attempted");
            case Operations.OP_BUSINESS:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                Prohibited Operations:
                */
                break;
        }

    }

    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID) {
        return new StatelessEjbObjectHandler(container, pk, depID);
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