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
package org.openejb.corba.services.transaction;

/**
 * This class implements an XA XID.
 * 
 * @author	Jerome DANIEL
 * @version	1.0
 */
public class XID implements javax.transaction.xa.Xid
{
	/**
	 * Reference to the format ID
	 */
	private int _format_id;
	
	/**
	 * Reference to the gtrid
	 */
	private byte [] _gtrid;
	
	/**
	 * Reference to the bqual
	 */
	private byte [] _bqual;
	
	/**
	 * Constructor
	 */
	public XID( org.omg.CosTransactions.otid_t otid )
	{
		_format_id = otid.formatID;
		
		_bqual = new byte[ otid.bqual_length ];
		
		_gtrid = new byte[ otid.tid.length - otid.bqual_length ];
		
		System.arraycopy( otid.tid, 0, _bqual, 0, otid.bqual_length );
		
		System.arraycopy( otid.tid, otid.bqual_length, _gtrid, 0,  otid.tid.length - otid.bqual_length );
	}
	
	// ---
	//
	// Xid interface implementation
	//
	// ---
	
	public int getFormatId()
	{
		return _format_id;
	}

	public byte[] getGlobalTransactionId()
	{
		return _gtrid;
	}
	
	public byte[] getBranchQualifier()
	{
		return _bqual;
	}
}
