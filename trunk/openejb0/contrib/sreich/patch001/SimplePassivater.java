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


package org.openejb.core.stateful;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.*;
import javax.ejb.SessionBean;
import org.openejb.core.EnvProps;
import org.openejb.util.io.FilesystemUtilities;

public class SimplePassivater implements PassivationStrategy {

    protected String _pathPrefix;

    public void init(Properties props) throws org.openejb.SystemException{
	// props can't be null
        String prefix = props.getProperty(EnvProps.IM_PASSIVATOR_PATH_PREFIX);

	try{
	    final File tmpDir = (prefix!=null)?
		FilesystemUtilities.createTempDirectory(prefix):
		FilesystemUtilities.createTempDirectory();
	    tmpDir.deleteOnExit();
	    _pathPrefix=tmpDir.getAbsolutePath()+File.separator;
	    
	    if(!tmpDir.mkdirs()) {
		throw new org.openejb.SystemException(getClass().getName()+".init(): can't create directory "+_pathPrefix);
	    }
	}catch(java.io.IOException e) {
	    throw new org.openejb.SystemException(getClass().getName()+".init(): can't use directory prefix "+prefix+":"+e);
	}
    }
    
    public synchronized void passivate(Object primaryKey, Object state)
    throws org.openejb.SystemException{
        try{
           // The replace(':','-') ensures the filename is correct under Microsoft Windows OS
            
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(_pathPrefix+primaryKey.toString().replace(':', '-' ) +".ser"));
            
            oos.writeObject(state);// passivate just the bean instance
            oos.flush();
            oos.close();
            
        }
	catch(java.io.NotSerializableException nse )
	{
	    throw new org.openejb.SystemException("The type " + nse.getMessage() + " in the bean class " + ((BeanEntry)state).bean.getClass().getName() + " is not serializable as mandated by the EJB specification."); 
	}
	catch(Exception t){
            // FIXME: More intelligent exception handling needed
            throw new org.openejb.SystemException(t);
        }
        
    }
    public synchronized void passivate(Hashtable hash)throws org.openejb.SystemException{
        Enumeration enum = hash.keys();
        while(enum.hasMoreElements()){
            Object id = enum.nextElement();
            passivate(id, hash.get(id));
        }
    }
    public synchronized Object activate(Object primaryKey)
    throws org.openejb.SystemException{
        
        try{
            
            // The replace(':','-') ensures the filename is correct under Microsoft Windows OS
            String fileName = _pathPrefix+primaryKey.toString().replace(':', '-' ) +".ser";     
            File file = new File(fileName);
            if(file.exists()){
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                Object state = ois.readObject();
                file.delete();
                ois.close();
                return state; 
            }else
                return null;
        
        }catch(Exception t){
            
            throw new org.openejb.SystemException(t);
        }
        
    }
    
}
