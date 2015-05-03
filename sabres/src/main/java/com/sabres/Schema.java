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
    private final Map<String, JavaType> types = new HashMap<>();

    Schema() {}

    void put(String key, JavaType type) {
        types.put(key, type);
    }

    void putAll(Schema schema) {
        types.putAll(schema.getTypes());
    }

    Map<String, JavaType> getTypes() {
        return types;
    }

    JavaType getType(String key) {
        return types.get(key);
    }

    boolean isEmpty() {
        return types.isEmpty();
    }

    // TODO: that's a stupid name for the function. It does not infer that a new object is created.
    Schema update(Schema schema) throws SabresException {
        Schema newSchema = new Schema();
        for (Map.Entry<String, JavaType> entry: schema.getTypes().entrySet()) {
            if (types.containsKey(entry.getKey())) {
                if (!types.get(entry.getKey()).equals(entry.getValue())) {
                    throw new SabresException(SabresException.INCORRECT_TYPE,
                            String.format("cannot set key %s to type %s. Already set to type %s",
                                    entry.getKey(), entry.getValue().toString(),
                                    types.get(entry.getKey()).toString()));

                }
            } else {
                newSchema.put(entry.getKey(), entry.getValue());
            }
        }
        return newSchema;
    }

    String[] toHeaders() {
        String[] headers = new String[types.size() + 1];
        int i = 1;
        headers[0] = "objectId";
        for (Map.Entry<String, JavaType> entry: types.entrySet()) {
            headers[i++] = String.format("%s(%s)", entry.getKey(), entry.getValue());
        }

        return headers;
    }
}
