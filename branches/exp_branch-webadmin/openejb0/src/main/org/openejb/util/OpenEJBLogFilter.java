package org.openejb.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class OpenEJBLogFilter implements FilenameFilter {

    /**
     * Constructor for OpenEJBLogFilter.
     */
    public OpenEJBLogFilter() {
        super();
    }

    /**
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    public boolean accept(File dir, String name) {
        return (name.indexOf(".log") != -1);
    }

}
