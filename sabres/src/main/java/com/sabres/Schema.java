/*
 * Copyright 2015 Tamir Shomer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sabres;

import java.util.HashMap;
import java.util.Map;

final class Schema {
    private final Map<String, ObjectDescriptor> objectDescriptors = new HashMap<>();

    Schema() {}

    void put(String key, ObjectDescriptor type) {
        objectDescriptors.put(key, type);
    }

    void putAll(Schema schema) {
        objectDescriptors.putAll(schema.getObjectDescriptors());
    }

    Map<String, ObjectDescriptor> getObjectDescriptors() {
        return objectDescriptors;
    }

    ObjectDescriptor.Type getType(String key) {
        return objectDescriptors.get(key).getType();
    }

    boolean isEmpty() {
        return objectDescriptors.isEmpty();
    }

    int size() {
        return objectDescriptors.size();
    }

    Schema createDiffSchema(Schema schema) throws SabresException {
        Schema newSchema = new Schema();
        for (Map.Entry<String, ObjectDescriptor> entry: schema.getObjectDescriptors().entrySet()) {
            if (objectDescriptors.containsKey(entry.getKey())) {
                if (!objectDescriptors.get(entry.getKey()).getType().equals(entry.getValue().getType())) {
                    throw new SabresException(SabresException.INCORRECT_TYPE,
                            String.format("cannot set key %s to type %s. Already set to type %s",
                                    entry.getKey(), entry.getValue().toString(),
                                    objectDescriptors.get(entry.getKey()).toString()));

                }
            } else {
                newSchema.put(entry.getKey(), entry.getValue());
            }
        }
        return newSchema;
    }
}
