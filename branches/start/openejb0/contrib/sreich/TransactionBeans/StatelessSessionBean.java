package transactiontests;

import javax.ejb.*;
import javax.transaction.UserTransaction;

public class StatelessSessionBean extends TransactionsImpl implements SessionBean {

    //
    // Creation methods
    //

    public StatelessSessionBean() {
    }

    public void ejbCreate() throws CreateException {
        /* Stateless session bean create methods never have parameters */
    }


    //
    // SessionBean interface implementation
    //

    public void setSessionContext(SessionContext ctx) {
        _ctx=ctx;
    }

    public void ejbPassivate() {

        /* does not apply to stateless session beans */
    }

    public void ejbActivate() {

        /* does not apply to stateless session beans */
    }

    public void ejbRemove() {
    }
    
}