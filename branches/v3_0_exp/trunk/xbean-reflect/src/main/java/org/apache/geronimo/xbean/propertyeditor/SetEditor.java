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

package org.apache.geronimo.xbean.propertyeditor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for editing array types.
 *
 * @version $Rev: 6680 $ $Date: 2005-12-24T04:38:27.427468Z $
 */
public final class SetEditor extends AbstractCollectionConverter {
    public SetEditor() {
        super(Set.class);
    }

    protected Object createCollection(List list) {
        return new LinkedHashSet(list);
    }
}