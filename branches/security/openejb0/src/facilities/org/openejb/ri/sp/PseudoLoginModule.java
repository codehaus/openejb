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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2003 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.ri.sp;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;


/**
 *
 * @author  adc
 * @version 
 */
public class PseudoLoginModule implements javax.security.auth.spi.LoginModule {

    private Subject _subject;
    private CallbackHandler _callbackHandler;
    private Map _options;

    // the authentication status
    private boolean _succeeded = false;
    private boolean _commitSucceeded = false;

    public PseudoLoginModule() {
    }

    public boolean abort() throws LoginException {

        if ( _succeeded == false ) {
            return false;
        } else if ( _succeeded == true && _commitSucceeded == false ) {
            _succeeded = false;
        } else {
            logout();
        }
        return true;
    }
    
    public boolean commit() throws LoginException {

	if (_succeeded == false) {
            return false;
        } else {	    
	    PseudoPrincipal principal = new PseudoPrincipal( "PSEUDO" );

            if ( !_subject.getPrincipals().contains( principal ) ) {
		_subject.getPrincipals().add( principal );
	    }
            
	    _commitSucceeded = true;

            return true;
        }
    }
    
    public void initialize( Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options ) {

        _subject = subject;
        _callbackHandler = callbackHandler;
        _options = options;
    }
    
    public boolean login() throws LoginException {

        if (_callbackHandler == null) {
            throw new LoginException( "No callback handler" );
	}

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback( "username" );
        callbacks[1] = new PasswordCallback( "password", false );

        try {

            _callbackHandler.handle(callbacks);

            String username = ((NameCallback)callbacks[0]).getName();
            String password = new String(((PasswordCallback)callbacks[1]).getPassword());
            ((PasswordCallback)callbacks[1]).clearPassword();
        } catch (java.io.IOException ioe) {
            throw new LoginException( ioe.toString() );
        } catch (UnsupportedCallbackException uce) {
	    throw new LoginException( "Unsupported Callback Exception" );
        }
        
	_succeeded = true;
 	
	return true;
    }
    
    public boolean logout() throws javax.security.auth.login.LoginException {
        _succeeded = false;
        _succeeded = _commitSucceeded;

        return true;
    }
    
}