package org.openejb.core.stateful;

import javax.ejb.EnterpriseBean;
import javax.ejb.SessionSynchronization;

import org.openejb.ApplicationException;
import org.openejb.InvalidateReferenceException;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.core.transaction.TransactionPolicy;
/**
 * Don't wrap the following method transaction types
 * 
 * TX_NEVER 
 * TX_NOT_SUPPORTED 
 * 
 * They never execute in a transaction and there is nothing to syncronize on.
 * 
 * @author <a href="mailto=david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class SessionSynchronizationTxPolicy extends org.openejb.core.transaction.TransactionPolicy {
    
    protected TransactionPolicy policy;

    public SessionSynchronizationTxPolicy(TransactionPolicy policy){
        this.policy     = policy;
        this.container  = policy.getContainer();
        this.policyType = policy.policyType;
        if(container instanceof org.openejb.Container &&
           ((org.openejb.Container)container).getContainerType()!=org.openejb.Container.STATEFUL ||
           policyType==TransactionPolicy.Never ||
           policyType==TransactionPolicy.NotSupported) {
            throw new IllegalArgumentException();
        }
        
    }

    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
        policy.beforeInvoke( instance, context );

        // This can happen with TxSupports policies
        if( context.currentTx == null ) return;
            
        try{
            SessionSynchronization session = (SessionSynchronization)instance;
            SessionSynchronizationCoordinator.registerSessionSynchronization( session, context );
        } catch (javax.transaction.RollbackException e){
            logger.error("Cannot register the SessionSynchronization bean with the transaction, the transaction has been rolled back");
            handleSystemException( e, instance, context );            
        } catch (javax.transaction.SystemException e){
            logger.error("Cannot register the SessionSynchronization bean with the transaction, received an unknown system exception from the transaction manager: "+e.getMessage());
            handleSystemException( e, instance, context );            
        } catch (Throwable e){
            logger.error("Cannot register the SessionSynchronization bean with the transaction, received an unknown exception: "+e.getClass().getName()+" "+ e.getMessage());
            handleSystemException( e, instance, context );            
        }
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        policy.afterInvoke( instance, context );
    }

    public void handleApplicationException( Throwable appException, TransactionContext context) throws ApplicationException{
        policy.handleApplicationException( appException, context );
    }
    
    public void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        try {
            policy.handleSystemException( sysException, instance, context );
        } catch ( ApplicationException e ){
            throw new InvalidateReferenceException( e.getRootCause() );
        }
    }

}

