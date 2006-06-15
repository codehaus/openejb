/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;

/**
 * @version $Rev: 356097 $ $Date: 2005-12-11 17:29:03 -0800 (Sun, 11 Dec 2005) $
 */
public class ObjectInputStreamExt extends ObjectInputStream {

    private ClassLoader classloader;

    public ObjectInputStreamExt(InputStream in, ClassLoader loader) throws IOException {
        super(in);
        this.classloader = loader;
    }

    protected Class resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        return ClassLoading.loadClass(classDesc.getName(), classloader);
    }

    protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        Class[] cinterfaces = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++)
            cinterfaces[i] = classloader.loadClass(interfaces[i]);

        try {
            return Proxy.getProxyClass(classloader, cinterfaces);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

    ClassLoader getClassloader() {
        return classloader;
    }

}
