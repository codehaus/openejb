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

import javax.xml.bind.annotation.XmlEnumValue;


/**
 * The cmr-field-type element specifies the class of a
 * collection-valued logical relationship field in the entity
 * bean class. The value of an element using cmr-field-typeType
 * must be either: java.util.Collection or java.util.Set.
 */
public enum CmrFieldType {
    @XmlEnumValue("java.util.Collection") COLLECTION,
    @XmlEnumValue("java.util.Set") SET
}
