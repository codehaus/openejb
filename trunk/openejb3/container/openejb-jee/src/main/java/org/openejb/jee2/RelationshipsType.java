/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
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

package org.openejb.jee2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The relationshipsType describes the relationships in
 * which entity beans with container-managed persistence
 * participate. The relationshipsType contains an optional
 * description; and a list of ejb-relation elements, which
 * specify the container managed relationships.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relationshipsType", propOrder = {
        "description",
        "ejbRelation"
        })
public class RelationshipsType {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "ejb-relation", required = true)
    protected List<EjbRelationType> ejbRelation;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDescription().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    /**
     * Gets the value of the ejbRelation property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbRelation property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getEjbRelation().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbRelationType }
     */
    public List<EjbRelationType> getEjbRelation() {
        if (ejbRelation == null) {
            ejbRelation = new ArrayList<EjbRelationType>();
        }
        return this.ejbRelation;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}