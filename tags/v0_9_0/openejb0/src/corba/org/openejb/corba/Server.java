/**
 * Redistribution and use of this software and associated
 * documentation ("Software"), with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright statements
 *    and notices.  Redistributions must also contain a copy of this
 *    document.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio Inc.  For written permission, please
 *    contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Inc. Exolab is a registered trademark of
 *    Intalio Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL INTALIO OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Copyright 1999-2001 (c) Intalio,Inc. All Rights Reserved.
 *
 * $Id$
 *
 * Date         Author  Changes
 */

package org.openejb.corba;
import org.omg.PortableServer.*;
import org.apache.log4j.Category;

/**
 * This class is the main part of the OpenEJB Server over CORBA. It creates all containers, adapters and proxy.
 *
 * @author Jerome DANIEL ( jdaniel@intalio.com )
 */
public class Server
{
    /**
    * Reference to the ORB
     */
    private static org.omg.CORBA.ORB orb;

    /**
    * Reference to the POA
     */
    private static org.omg.PortableServer.POA poa;

    /**
        * The domain config file path
     */
    private static java.lang.String domainPath;

    /**
        * Bind the Tyrex TM to Naming Service
     */
    private static boolean doTyrexBind = false;

    /**
        * Reference to the Initializer
     */
    private static org.openejb.corba.core.ContainerSystem system;

    private static Category logger = Category.getInstance("CORBA-Adapter");

    /**
        * The server entry point.
     */
    public static org.omg.CORBA.ORB initialize( String [] args )
        throws org.openejb.OpenEJBException
    {
        orb = org.omg.CORBA.ORB.init( args, null );

        initialize(args, orb);

        return orb;
    }

    public static void initialize( String [] args, org.omg.CORBA.ORB neworb )
        throws org.openejb.OpenEJBException
    {


        // -- Scan arguments --

        orb = neworb;

        scan_arguments( args );

        if(domainPath!=null) {
            // -- Starts Tyrex as a local OTS --
            try {
                org.openejb.corba.services.transaction.LocalTyrex tyrex = new org.openejb.corba.services.transaction.LocalTyrex( orb, domainPath, doTyrexBind );
            }
            catch ( org.openejb.OpenEJBException ex ) {
                logger.fatal("org.openejb.corba.initializer.run: can't start local transaction manager", ex);
                throw ex;
            }
        }
        
        // -- Create and run the CORBA Adapter Initializer --
        system = new org.openejb.corba.core.ContainerSystem( orb);
        system.initialize( System.getProperties() );
    }

    public static void main(String[] args) {
        try{
            orb = initialize(args);
            System.out.println("OpenEJB over CORBA is now ready...");
            orb.run();
        }catch (org.openejb.OpenEJBException ex) {
            System.err.println("Unable to start OpenEJB over CORBA");
        }catch(IllegalArgumentException e) {
            usage();
        }
    }

    private static void usage() {
        System.out.println("Usage :");
        System.out.println();
        System.out.println("\tjava org.openejb.corba.Server [ properties file name ] [ options ]");
        System.out.println();
        System.out.println("Options :");
        System.out.println("\t-domain <domain file path>");
        System.out.println("\t\tSpecify a domain configuration file for Tyrex");
        System.out.println("\t-naming");
        System.out.println("\t\tBind the Tyrex TM to the Naming Service");
        System.out.println("\t-verbose");
        System.out.println("\t\tEnable verbose mode");
        System.out.println("\t-ORBInitRef");
        System.out.println("\t\tTo specify with CORBA URL references to naming service, transaction service and security service");
    }
    /**
        * Scan the command line arguments
     * @throws IllegalArgumentException if an argument is unknown, or the number of arguments is zero
     */
    private static void scan_arguments( String [] args )
    {
        if ( args.length == 0 )
        {
            throw new IllegalArgumentException();
        }

        for ( int i=0; i<args.length; i++ )
        {
	    if ( args[i].equals("-domain") )
                {
                    if( (i+1) < args.length )
                        domainPath = args[i+1];
                }
            else
                if( args[i].equals("-naming") ) doTyrexBind = true;
            // ignore unknown arguments, e.g. -ORBXXX
        }
    }

    /**
        * Return the ORB reference
     */
    public static org.omg.CORBA.ORB getORB()
    {
        return orb;
    }

    /**
        * Return the root POA reference
     */
    public static org.omg.PortableServer.POA getPOA()
    {
        return poa;
    }
}