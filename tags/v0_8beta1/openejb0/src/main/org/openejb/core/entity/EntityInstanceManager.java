/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.core.entity;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.transaction.Transaction;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.core.DeploymentInfo;
import org.openejb.core.EnvProps;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.TransactionManagerWrapper;
import org.openejb.util.LinkedListStack;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;
import org.openejb.util.Stack;
import org.openejb.ApplicationException;
import org.openejb.SystemException;
/**
 * One instance of this class is used with each EntityContainer. It is responsible for managing
 * the Method Ready and Transaction Ready pools where bean instances are stored between client
 * calls.
 * <p>
 * To obtain an instance the obtainInstance( ) method must be called with the primary key and
 * bean type to obtained.  The bean instance will be returned form the method ready pool if its not already
 * enrolled in the current transaction, or from the transaction ready pool if it is.
 * <p>
 * If the bean identity requested is registered with a different tx, a different bean instance is returned
 * which results in both transaction accessing the same bean identity, but using different bean
 * instances.
 * <p>
 * The poolInstance( ) method is invoked when the client call is finished using the bean instance.
 * the instance will be returned to the transaction ready pool if its registered with a transaction or
 * it will be placed in the method ready pool if its not.
 * <p>
 * The freeInstance( ) method is invoked in an EJBException or some other RuntimeException occurs. In these
 * situations, the bean instance must be de-registered from the transaction and de-referenced for garbage collection.
 * the freeInstance( ) handles these operations.
 * <p>
 * This class automatically handles transferring bean instances to the Transaction Ready Pool and transferring
 * them back to the Method Ready pool as necessary.
 * <p>
 * This class also handles the ejbStore operations by registering the instance with the transaction. The ejbStore
 * Operation doesn't not need to be executed by the container. The container is, however, responsible for the ejbLoad method.
 */
public class EntityInstanceManager {
    
    /* The default size of the bean pools. Every bean class gets its own pool of this size */
    protected int poolsize = 0;
    /* The container that owns this InstanceManager. Its needed to use the invoke() method 
       in the obtainInstance( ), which is needed for transactionally safe invokation.
    */
    protected EntityContainer container;
    /*
    * Every entity that is registered with a transaciton is kept in this pool until the tx
    * completes.  The pool contains SyncronizationWrappers (each holding a reference) keyed
    * by using an instance of the inner Key class. The Key class is a compound key composed
    * of the tx, deployment, and primary key identifiers.
    */
    protected Hashtable txReadyPool = new Hashtable( );
    /* 
    * contains a collection of LinkListStacks indexed by deployment id. Each indexed stack 
    * represents the method ready pool of for that class. 
    */
    protected HashMap poolMap = null;
    /*
    * Instances of the inner Key class are pooled so that they can be recycled instead of
    * GCed. They are stored in this Stack instance until needed.
    */
    protected Stack keyPool = new LinkedListStack(10);
    /*
    * Instances of the inner SyncrhonrizationWrapper class are pooled so that they 
    * can be recycled instead of GCed. They are stored in this Stack instance until needed.
    */
    protected Stack syncronizationWrapperPool = new LinkedListStack(10);
    
    
    /*
    * The javax.ejb.EntityBean.setEntityContext(...) method is used for processing bean instances returing to the method ready pool
    * in the poolInstance( ) method.  This variable is esbalished in the contructor so that it doesn't
    * have to be re-obtained every time we want to passivate an entity instance.
    */
    protected static Method SET_ENTITY_CONTEXT_METHOD;
    
    /*
    * The javax.ejb.EntityBean.unsetEntityContext(...) method is used for processing bean instances that are being evicted from memory.  
    * This variable is esbalished in the contructor so that it doesn't
    * have to be re-obtained every time we want to passivate an entity instance.
    */
    protected static Method UNSET_ENTITY_CONTEXT_METHOD;
    
    /*
    * This static block sets up the EJB_PASSIVATE_METHOD, EJB_LOAD_METHOD, SET_ENTITY_CONTEXT_METHOD static methods, which are used
    * in the poolInstance() and obtainInstance() methods of this type. Saves method lookup cycles at runtime.
    */
    static{
        try{
        SET_ENTITY_CONTEXT_METHOD = javax.ejb.EntityBean.class.getMethod("setEntityContext", new Class []{javax.ejb.EntityContext.class});
        UNSET_ENTITY_CONTEXT_METHOD = javax.ejb.EntityBean.class.getMethod("unsetEntityContext", null);
        }catch(NoSuchMethodException nse){
            // Do Nothing
        }
    }
    
    
    

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("EntityInstanceManager");
    /******************************************************************
                        CONSTRUCTOR METHODS
    *******************************************************************/
    public EntityInstanceManager( ){
        for(int i = 0; i < 10; i++)
            keyPool.push(new Key());
    }
    public void init(EntityContainer myContainer, HashMap deployments, Properties props)
    throws OpenEJBException{

        SafeProperties safeProps = toolkit.getSafeProperties(props);
        poolsize = safeProps.getPropertyAsInt(EnvProps.IM_POOL_SIZE, 100);
        container = myContainer;
        
        poolMap = new HashMap();// put size in later
        java.util.Iterator iterator = deployments.values().iterator();
        while(iterator.hasNext()){
             poolMap.put(((DeploymentInfo)iterator.next()).getDeploymentID(),new LinkedListStack(poolsize/2));
        }
        
    }

    /**********************************************************************
            InstanceManager INTERFACE METHODS
    ***********************************************************************/
   /**
    * Obtains a bean instance from the either the method ready pool or the Tx method ready pool. (Tx == Transaction)
    * An entity bean is obtained from the ready pool if its not enrolled in the current tx.
    * An entity bean is obtained from the tx ready pool if it is enrolled in the current tx.
    *   A tx ready entity can only be obtained if:
    *       a: It not currently servicing a thread in that tx.
    *       b: It allows for reentrant access.
    * Reentrant beans are accessed by multiple threads and are therefore unsafe and must be
    * coded to be multi-threaded.
    *
    * If the primaryKey is null when the method is invoked, the bean will be retrived from the 
    * MethodReady pool and will not be associated with the current tx
    *
    * The call method is required for automatically peforming the ejbLoad when a bean instance is first
    * registered with a transaction.
    */
    public EntityBean obtainInstance(ThreadContext callContext)
    throws OpenEJBException{
        Transaction currentTx = null;
        try{
        currentTx = OpenEJB.getTransactionManager().getTransaction();
        }catch(javax.transaction.SystemException se){
            throw new org.openejb.SystemException("TransactionManager failure");
        }
        
        Object primaryKey = callContext.getPrimaryKey();// null if its a servicing a home methods (create, find, ejbHome)
        if(currentTx != null && primaryKey != null){// primkey is null if create operation is called
            Key key = (Key)keyPool.pop();
            if(key==null)key = new Key();
            key.set(currentTx, callContext.getDeploymentInfo().getDeploymentID(),primaryKey);
            SyncronizationWrapper wrapper = (SyncronizationWrapper)txReadyPool.get(key);
            
            if(wrapper != null){// if true, the requested bean instance is already enrolled in a transaction
                
                if( !wrapper.isAssociated()){// is NOT associated
                    /*
                    * If the bean identity was removed (via ejbRemove()) within the same transaction,
                    * then it's SynchronizationWrapper will be in the txReady pool but marked as disassociated. 
                    * This allows us to prevent a condition where the caller removes the bean and then attempts to
                    * call a business method on that bean within the same transaction.  After a bean is removed any
                    * subsequent invocations on that bean with the same transaction should throw a NoSuchEntityException.
                    * its likely that the application server would have already made the reference invalid, but this bit of 
                    * code is an extra precaution.
                    */
                    throw new org.openejb.InvalidateReferenceException(new javax.ejb.NoSuchEntityException());
                }else if(callContext.getCurrentOperation() == Operations.OP_REMOVE){
                    /*
                    *  To avoid calling ejbStore( ) on a bean that after its removed, we can not delegate 
                    *  the wrapper is marked as disassociated from the transaction to avoid processing the
                    *  beforeCompletion( ) method on the SynchronizationWrapper object.
                    */
                    wrapper.disassociate();   
                }
                
                if(wrapper.isAvailable()){
                    // isAvailable = true means that the bean is in the txReadyPool and is avaiable to continue servicing
                    // the same transaction.
                    return wrapper.getEntityBean();
                }else{
                    // isAvailable = false means that the bean instance is currently servicing this transaction. Asking for it now indicates that
                    // retentrant access requested. Two clients attempting to access the same bean instance in the same transaction.
                    org.openejb.core.DeploymentInfo depInfo = (org.openejb.core.DeploymentInfo)ThreadContext.getThreadContext().getDeploymentInfo();
                    if(depInfo.isReentrant()){
                        /*
                         * If the bean is declared as reentrant then the instance may be accessed
                         * by more then one thread at a time.  This is one of the reasons that reentrancy
                         * is bad. In this case beans must be programmed to be multi threaded. The other reason
                         * reentrancy is bad has to do with transaction isolation. Multiple instances writing to
                         * the same database records will inevitably cancel out previous writes within the same tx.
                         * 
                         * In the future we may change this to return a new instance of the bean and to 
                         * link it and its wrapper to the original wrapper, but for now we choose this strategy because
                         * its simpler to implement.
                        */
                        return wrapper.getEntityBean();
                    }else
                        throw new org.openejb.ApplicationException(new java.rmi.RemoteException("Attempted reentrant access. Bean is not reentrant"));
                }
            }
            else{
                /* 
                * If no synchronized wrapper for the key exists 
                * Then the bean entity is being access by this transaction for the first time,
                * so it needs to be enrolled in the transaction.
                */
                EntityBean bean = getPooledInstance(callContext);
                wrapper = (SyncronizationWrapper)syncronizationWrapperPool.pop();
                if(wrapper==null)
                    wrapper = new SyncronizationWrapper(bean, key, false);
                else
                    wrapper.set(bean,key,false);
                    
                if(callContext.getCurrentOperation()== Operations.OP_REMOVE){
                    /*
                    *  To avoid calling ejbStore( ) on a bean that after its removed, we can not delegate 
                    *  the wrapper is marked as disassociated from the transaction to avoid processing the
                    *  beforeCompletion( ) method on the SynchronizationWrapper object.
                    *
                    *  We have to still use a wrapper so we can detect when a business method is called after
                    *  a ejbRemove() and act to prevent it from being processed.
                    */
                    wrapper.disassociate();   
                }
                    
                try{
                /* 
                 * registering the sync as priority one ensures that it will be handled before lower priority sync objects. 
                 * See TransactionManagerWrapper for more details.
                */
                ((TransactionManagerWrapper.TransactionWrapper)currentTx).registerSynchronization(wrapper,1);
                }catch(javax.transaction.SystemException se){
                    throw new org.openejb.SystemException(se);
                }catch(javax.transaction.RollbackException re){
                    throw new org.openejb.ApplicationException(new javax.transaction.TransactionRolledbackException(re.getMessage()));
                }
                  
                byte orginalOperation = callContext.getCurrentOperation();
                callContext.setCurrentOperation(org.openejb.core.Operations.OP_LOAD);
                try{
                    ((javax.ejb.EntityBean)bean).ejbLoad();
                }catch(Exception e){
                    // this will always be handled by the invoke( ) method
                    throw new org.openejb.OpenEJBException(e);
                }
                callContext.setCurrentOperation(orginalOperation);
                txReadyPool.put(key, wrapper);

                return bean;
            }    
        }else{  /*
                If no transaction is associated with the thread or if its a create, find or home method (primaryKey == null), then no synchronized wrapper is needed.
                if bean instance is used for a create method then a syncrhonziation wrapper may be assigned
                when the bean is returned to the pool -- depending on if the tx is a client initiated or container initiated.
                */
            return getPooledInstance(callContext);
        }
    }
    /**
    * Obtains a bean instance from the proper bean pool.  Each bean class has its own pool
    * of instances. If the bean class' pool is empty, a new Instance is created.
    *
    * If a bean instance is not avaible one will be created.
    */
    protected EntityBean getPooledInstance(ThreadContext callContext)
    throws org.openejb.OpenEJBException{
        DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        Stack methodReadyPool = (Stack)poolMap.get(deploymentInfo.getDeploymentID());
        if(methodReadyPool==null)
            throw new org.openejb.SystemException("Invalid deployment id "+deploymentInfo.getDeploymentID()+" for this container");
            
        EntityBean bean = (EntityBean)methodReadyPool.pop();
        if(bean == null){
            try{
                bean = (EntityBean)deploymentInfo.getBeanClass().newInstance();
            }catch(Exception e){
                throw new org.openejb.SystemException(e);
            }
            // setEntityContext needs to be invoked, so need a special MethodInvocation 
            // that specifies the setEntityContext on the EntityBean interface with 
            // one argument, entity context
            
            
            Transaction currentTx = null;
            try{
            currentTx = OpenEJB.getTransactionManager().getTransaction();
            }catch(javax.transaction.SystemException se){
                throw new org.openejb.SystemException("TransactionManager failure");
            }
            
            
            byte currentOp = callContext.getCurrentOperation();
            callContext.setCurrentOperation(org.openejb.core.Operations.OP_SET_CONTEXT);

            try{
                /*
                * setEntityContext executes in an unspecified transactional context. In this case we choose to 
                * allow it to have what every transaction context is current. Better then suspending it 
                * unnecessarily.
                *
                * We also chose not to invoke EntityContainer.invoke( ) method, which duplicate the exception handling
                * logic but also attempt to manage the begining and end of a transaction. It its a container managed transaciton
                * we don't want the TransactionScopeHandler commiting the transaction in afterInvoke() which is what it would attempt 
                * to do.
                */
                this.SET_ENTITY_CONTEXT_METHOD.invoke(bean, new javax.ejb.EntityContext []{(javax.ejb.EntityContext)callContext.getDeploymentInfo().getEJBContext()});
                //return bean;
            }catch(java.lang.reflect.InvocationTargetException target) {
                // contains most likely an EJBException, so forward that one
                throw new org.openejb.ApplicationException(target.getTargetException());
            }catch(java.lang.Exception e){
                /*
                * The EJB 1.1 specification does not specify how exceptions thrown by setEntityContext impact the 
                * transaction, if there is one.  In this case we choose the least disruptive operation, throwing an 
                * application exception and NOT automatically marking the transaciton for rollback.
                */
                throw new org.openejb.ApplicationException(e);
            }finally{
                callContext.setCurrentOperation(currentOp);
            }
        }

	if( ( callContext.getCurrentOperation()== org.openejb.core.Operations.OP_BUSINESS) ||
	    ( callContext.getCurrentOperation()== org.openejb.core.Operations.OP_REMOVE ) )
	{
            /*
            * When a bean is retrieved from the bean pool to service a client's business method request it must be 
            * notified that its about to enter service by invoking its ejbActivate( ) method. A bean instance 
            * does not have its ejbActivate() invoked when:
            * 1. Its being retreived to service an ejbCreate()/ejbPostCreate(). 
            * 2. Its being retrieved to service an ejbFind method. 
            * 3. Its being retrieved to service an ejbRemove() method.
            * See section 9.1.4 of the EJB 1.1 specification.
            */
            byte currentOp = callContext.getCurrentOperation();

            callContext.setCurrentOperation(org.openejb.core.Operations.OP_ACTIVATE);
            try{
                /*
                In the event of an exception, OpenEJB is required to log the exception, evict the instance,
                and mark the transaction for rollback.  If there is a transaction to rollback, then the a
                javax.transaction.TransactionRolledbackException must be throw to the client.
                See EJB 1.1 specification, section 12.3.2
                */
                 ((javax.ejb.EntityBean)bean).ejbActivate();
            }catch(Throwable t){// handle reflection exception
                /*
                Any exception thrown by reflection; not by the enterprise bean. Possible
                Exceptions are:
                    IllegalAccessException - if the underlying method is inaccessible.
                    IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                    NullPointerException - if the specified object is null and the method is an instance method.
                    ExceptionInInitializerError - if the initialization provoked by this method fails.
                */
                
                try{
                    Transaction tx = OpenEJB.getTransactionManager().getTransaction();
                    if(tx!=null){
                        tx.setRollbackOnly();
                        throw new ApplicationException(new javax.transaction.TransactionRolledbackException("Reflection exception thrown while attempting to call ejbActivate() on the instance. Exception message = "+t.getMessage()));
                    }
                }catch(javax.transaction.SystemException se){
                    throw new org.openejb.SystemException(se);
                }
                throw new ApplicationException(new java.rmi.RemoteException("Reflection exception thrown while attempting to call ejbActivate() on the instance. Exception message = "+t.getMessage()));
            }finally{
                callContext.setCurrentOperation(currentOp);
            }
            
        }
        return bean;
    }
    /**
    * Returns a bean instance to the method ready or tx method ready pool.
    * A bean is returned to the tx method ready pool if the current thread is associated with a transaction.
    * If the current thread is not associated with a transaction then the bean is returned to the Method Ready pool
    * If the primary key is null, this would indicate that the returning bean instance was used for a find or home
    * business methods -- in this case the bean is returned to the MethodReady pool even if the current thread is associated 
    * with a transaction.
    */
    public void poolInstance(ThreadContext callContext,EntityBean bean)
    throws OpenEJBException{
        Object primaryKey = callContext.getPrimaryKey();// null if servicing a home ejbFind or ejbHome method.
        Transaction currentTx = null;
        try{
        currentTx = OpenEJB.getTransactionManager().getTransaction();
        }catch(javax.transaction.SystemException se){
            throw new org.openejb.SystemException("TransactionManager failure");
        }
        if(currentTx != null && primaryKey != null){// primary key is null for find and home methods
            Key key = (Key)keyPool.pop();
            if(key==null)key = new Key();
            key.set(currentTx, callContext.getDeploymentInfo().getDeploymentID(),primaryKey);
            SyncronizationWrapper wrapper = (SyncronizationWrapper)txReadyPool.get(key);
            if(wrapper != null){
                if(callContext.getCurrentOperation()== Operations.OP_REMOVE){
                    /*
                    * The bean is being returned to the pool after it has been removed. Its 
                    * important at this point to mark the bean as disassociated to prevent 
                    * it's ejbStore method from bean called (see SynchronizationWrapper.beforeCompletion() method)
                    * and that subsequent methods can not be invoked on the bean identity (see obtainInstance() method).
                    */
                    wrapper.disassociate();
                    /* 
                    * If the bean has been removed then the bean instance is no longer needed and can return to the methodReadyPool
                    * to service another identity.
                    */
                    Stack methodReadyPool = (Stack)poolMap.get(callContext.getDeploymentInfo().getDeploymentID());
                    methodReadyPool.push(bean);
                }else
                    wrapper.setEntityBean(bean);
            }else{
                /* 
                A wrapper will not exist if the bean is being returned after a create operation.
                In this case the transaction scope is broader then the create method itself; its a client 
                initiated transaction, so the bean must be registered with the tranaction and moved to the
                tx ready pool
                */
                
                wrapper = (SyncronizationWrapper)syncronizationWrapperPool.pop();
                
                if(wrapper==null)
                    wrapper = new SyncronizationWrapper(bean, key, true);
                else
                    wrapper.set(bean,key,true);
                
                try{
                /* 
                 * registering the sync as priority one ensures that it will be handled before lower priority sync objects. 
                 * See TransactionManagerWrapper for more details.
                */
                ((TransactionManagerWrapper.TransactionWrapper)currentTx).registerSynchronization(wrapper,1);
                }catch(javax.transaction.SystemException se){
                    throw new org.openejb.SystemException(se);
                }catch(javax.transaction.RollbackException re){
                    throw new org.openejb.ApplicationException(new javax.transaction.TransactionRolledbackException(re.getMessage()));
                }
                  
                txReadyPool.put(key, wrapper);
            }
        }else{
            /* 
            If there is no transaction associated with the thread OR if the operation was a find or home method (PrimaryKey == null)
            Then the bean instance is simply returned to the methodReady pool
            */
            
            if(primaryKey !=null && callContext.getCurrentOperation()!= Operations.OP_REMOVE){
                /*
                * If the bean has a primary key; And its not being returned following a remove operation;
                * then the bean is being returned to the method ready pool after successfully executing a business method or create
                * method. In this case we need to call the bean instance's ejbPassivate before returning it to the pool per EJB 1.1
                * Section 9.1.
                */
                byte currentOp = callContext.getCurrentOperation();

                callContext.setCurrentOperation(org.openejb.core.Operations.OP_PASSIVATE);
                
                
                try{
                    /*
                    In the event of an exception, OpenEJB is required to log the exception, evict the instance,
                    and mark the transaction for rollback.  If there is a transaction to rollback, then the a
                    javax.transaction.TransactionRolledbackException must be throw to the client.
                    See EJB 1.1 specification, section 12.3.2
                    */
                    ((javax.ejb.EntityBean)bean).ejbPassivate();
                }catch(Throwable t){// handle reflection exception
                    /*
                    Any exception thrown by reflection; not by the enterprise bean. Possible
                    Exceptions are:
                        IllegalAccessException - if the underlying method is inaccessible.
                        IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                        NullPointerException - if the specified object is null and the method is an instance method.
                        ExceptionInInitializerError - if the initialization provoked by this method fails.
                    */
                
                    try{
                        Transaction tx = OpenEJB.getTransactionManager().getTransaction();
                        if(tx!=null){
                            tx.setRollbackOnly();
                            throw new ApplicationException(new javax.transaction.TransactionRolledbackException("Reflection exception thrown while attempting to call ejbPassivate() on the instance. Exception message = "+t.getMessage()));
                        }
                    }catch(javax.transaction.SystemException se){
                        throw new org.openejb.SystemException(se);
                    }
                    throw new ApplicationException(new java.rmi.RemoteException("Reflection exception thrown while attempting to call ejbPassivate() on the instance. Exception message = "+t.getMessage()));
                }finally{
                    callContext.setCurrentOperation(currentOp);
                }
            }
            
            /*
            * The bean is returned to the method ready pool if its returned after servicing a find, ejbHome, business or create 
            * method and is not still part of a tx.  While in the method ready pool the bean instance is not associated with a 
            * primary key and may be used to service a request for any bean of the same class.
            */
            Stack methodReadyPool = (Stack)poolMap.get(callContext.getDeploymentInfo().getDeploymentID());
            methodReadyPool.push(bean);
        }
            
        
    }
    /*
    * An instance is freed if a EJBException or some other runtime exception occurs.
    * To free an instance is to discard the instance so it can be garbage collected.
    * If the instance is in the tx ready pool it is removed and discarded so that it will
    * not process callbacks( ejbStore and ejbLoad).  The ejbRemove method is not invoked
    * when a bean instance is discarded following an exception.
    */
    public void freeInstance(ThreadContext callContext, EntityBean bean)
    throws org.openejb.SystemException{
        Transaction currentTx = null;
        try{
        currentTx = OpenEJB.getTransactionManager().getTransaction();
        }catch(javax.transaction.SystemException se){
            throw new org.openejb.SystemException("TransactionManager failure");
        }
        if(currentTx != null){
	    if ( callContext.getPrimaryKey() == null )
		return;
            // a freed bean instance that is part of a tx must be removed from the 
            // tx ready pool and discarded.
            Key key = (Key)keyPool.pop();
            if( key == null ) key = new Key();
            key.set(currentTx, callContext.getDeploymentInfo().getDeploymentID(),callContext.getPrimaryKey());
            
            /* 
               The wrapper is removed (if pooled) so that it can not be accessed again. This is 
               especially important in the obtainInstance( ) method where a disassociated wrapper 
               in the txReadyPool is indicative of an entity bean that has been removed via 
               ejbRemove() rather than freed because of an error condition as is the case here. 
            */
            SyncronizationWrapper wrapper = (SyncronizationWrapper)txReadyPool.remove(key);
            
            if(wrapper != null){
                /* 
                 It's not possible to deregister a wrapper with the transaction,
                 but it can be removed from the tx pool and made inoperative by
                 calling its disassociate method. The wrapper will be returned to the 
                 wrapper pool after the transaction completes 
                 (see SynchronizationWrapper.afterCompletion( ) method).  The wrapper must
                 be returned after the transaction completes so that it is not in the service
                 of another bean when the TransactionManager calls its Synchronization methods.
                 
                 In addition, the bean instance is dereferenced so it can be garbage
                 collected.
                */
                wrapper.disassociate();
            }
        }
        
        byte currentOp = callContext.getCurrentOperation();
        callContext.setCurrentOperation(org.openejb.core.Operations.OP_UNSET_CONTEXT);

        try{
            /*
            * unsetEntityContext executes in an unspecified transactional context. In this case we choose to 
            * allow it to have what every transaction context is current. Better then suspending it 
            * unnecessarily.
            *
            * We also chose not to invoke EntityContainer.invoke( ) method, which duplicate the exception handling
            * logic but also attempt to manage the begining and end of a transaction. It its a container managed transaciton
            * we don't want the TransactionScopeHandler commiting the transaction in afterInvoke() which is what it would attempt 
            * to do.
            */
                this.UNSET_ENTITY_CONTEXT_METHOD.invoke(bean, null);
        }catch(java.lang.reflect.InvocationTargetException target) {
            org.apache.log4j.Category.getInstance("OpenEJB").info(getClass().getName()+".freeInstance: ignoring exception "+target.getTargetException()+" on bean instance "+bean);
            // To throw, or not to throw, this is the question! See below.
        }catch(java.lang.Exception e){
            /*
            * The EJB 1.1 specification does not specify how exceptions thrown by unsetEntityContext impact the 
            * transaction, if there is one.  In this case we choose to do nothing since the instance is being disposed 
            * of anyway.
            */
            // TODO: Not spec compliant. Must fix. The spec clearly classifies this in the callback 
            // exception handling section 18.3.3 "Exceptions from container-invoked callbacks"
            org.apache.log4j.Category.getInstance("OpenEJB").info(getClass().getName()+".freeInstance: ignoring exception "+e+" on bean instance "+bean);
        }finally{
            callContext.setCurrentOperation(currentOp);
        }
        
        
    }
    
    /*
    * Instances of this class are used as keys for storing bean instances in the tx method 
    * ready pool.  A compound key composed of the transaction, primary key, and deployment id
    * identifiers is required to uniquely identify a bean in the tx method ready pool.
    */
    public class Key {
        Object deploymentID, primaryKey;
        Transaction transaction;
        
        public void set(Transaction tx, Object depID, Object prKey){
            transaction = tx;
            deploymentID = depID;
            primaryKey = prKey;
        }
	public Object getPK()
	{
		return primaryKey;
	}
        public int hashCode( ){
            return transaction.hashCode()^deploymentID.hashCode()^primaryKey.hashCode();
        }
        public boolean equals(Object other){
            if(other != null && other.getClass() == EntityInstanceManager.Key.class){
                Key otherKey = (Key)other;
                if(otherKey.transaction.equals(transaction) && otherKey.deploymentID.equals(deploymentID) && otherKey.primaryKey.equals(primaryKey))
                    return true;
            }
            return false;
        }
    }
          
    /*
    * Instances of this class are used to wrapper entity instances so that they can be registered
    * with a tx.  When the Synchronization.beforeCompletion is called, the bean's ejbStore method 
    * is invoked.  When the Synchroniztion.afterCompletion is called, the bean instance is returned
    * to the method ready pool and the wrapper is recycled in the wrapper pool.
    */
    public  class SyncronizationWrapper 
    implements javax.transaction.Synchronization{
         EntityBean bean;
         /*
         * <tt>isAvailable<tt> determines if the wrapper is still associated with a bean.  If the bean identity is removed (ejbRemove)
         * or if the bean instance is discarded, the wrapper will not longer be associated with a bean instances
         * and therefore its beforeCompletion method will not process the ejbStore method.
         */
         boolean isAvailable = true;
         boolean isAssociated = true;
         Key myIndex;
         
         public SyncronizationWrapper(EntityBean ebean, Key key, boolean available){
            set(ebean,key,available);
         }
         public void set(EntityBean ebean, Key key, boolean available){
            bean = ebean;
            isAvailable = available;
            myIndex =key;
         }
         public void disassociate( ){
            isAssociated = false;
         }
         public boolean isAssociated(){
            return isAssociated;
         }
         public synchronized boolean isAvailable(){
            return isAvailable;
         }
         public synchronized void setEntityBean(EntityBean ebean){
            isAvailable = true;
            bean = ebean;
         }
         public synchronized EntityBean getEntityBean(){
            isAvailable = false;
            return bean;
         }
         public void beforeCompletion(){
            if(isAssociated){
                try{
                    //DMB: You can't change the PK of the thread without caching the old pk 
                    // and restoring it after the operation
		    // ThreadContext.getThreadContext().setPrimaryKey( myIndex.getPK() );
                    bean.ejbStore();
                }catch(Exception re){
                    // TODO: Use spec compliant callback exception cleanup.
                    javax.transaction.TransactionManager txmgr = OpenEJB.getTransactionManager();
                    try{
                    txmgr.setRollbackOnly();
                    }catch(javax.transaction.SystemException se){
                        // log the exception
                    }
                   
                }
            }
         }
         public void afterCompletion(int status){
            txReadyPool.remove(myIndex);
            bean = null;
            isAvailable = true;
            isAssociated = true;
            keyPool.push(myIndex);
            syncronizationWrapperPool.push(this);
         }
         
    }
    

}

