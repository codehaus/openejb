/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.openejb.jee;

import java.util.Locale;

/**
 * @version $Revision$ $Date$
 */
public class LocalList<K,V> extends KeyedList<K,V> {
    public LocalList(Class type, String key) {
        super(type, key);
    }

    public LocalList(Class type) {
        super(type, "lang");
    }

    public V getLocal() {
        String lang = Locale.getDefault().getLanguage();
        return (map.get(lang) != null ? map.get(lang) : map.get(null));
    }

}
