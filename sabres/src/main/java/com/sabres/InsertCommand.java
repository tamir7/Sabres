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

import java.util.Map;

final class InsertCommand {
    private final String table;
    private final Map<String, SabresValue> objects;

    InsertCommand(String table, Map<String, SabresValue> objects) {
        this.table = table;
        this.objects = objects;
    }

    String toSql() {
        StringBuilder sb = new StringBuilder(String.format("INSERT INTO %s", table));
        StringBuilder keys = new StringBuilder();
        StringBuilder values = new StringBuilder();
        boolean first = true;


        for (Map.Entry<String, SabresValue> entry : objects.entrySet()) {
            if (first) {
                first = false;
            } else {
                keys.append(", ");
                values.append(", ");
            }

            keys.append(entry.getKey());
            values.append(entry.getValue().toSql());
        }

        return sb.append(String.format("(%s) VALUES (%s);", keys.toString(),
            values.toString())).toString();
    }

    @Override
    public String toString() {
        return toSql();
    }
}
