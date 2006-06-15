package org.openejb.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

public final class ArrayEnumeration implements Enumeration, Externalizable {
    static final long serialVersionUID = -1194966576855523042L;

    private Object[] elements;
    private int elementsIndex;

    public ArrayEnumeration(Vector elements) {
        this.elements = new Object[elements.size()];
        elements.copyInto(this.elements);
    }

    public ArrayEnumeration(java.util.List list) {
        this.elements = new Object[list.size()];
        list.toArray(this.elements);
    }

    public ArrayEnumeration() {
    }

    public java.lang.Object get(int index) {
        return elements[index];
    }

    public void set(int index, java.lang.Object o) {
        elements[index] = o;
    }

    public int size() {
        return elements.length;
    }

    public boolean hasMoreElements() {
        return (elementsIndex < elements.length);
    }

    public Object nextElement() {
        if (!hasMoreElements()) throw new NoSuchElementException("No more elements exist");
        return elements[elementsIndex++];
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(elements.length);
        out.writeInt(elementsIndex);
        for (int i = 0; i < elements.length; i++) {
            out.writeObject(elements[i]);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        elements = new Object[in.readInt()];
        elementsIndex = in.readInt();
        for (int i = 0; i < elements.length; i++) {
            elements[i] = in.readObject();
        }
    }

}

