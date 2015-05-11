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

public class UpdateCommand {
    private final String table;
    private final Map<String, ObjectValue> objects;
    private Where where;

    UpdateCommand(String table, Map<String, ObjectValue> objects) {
        this.table = table;
        this.objects = objects;
    }

    UpdateCommand where(Where where) {
        this.where = where;
        return this;
    }

    String toSql() {
        StringBuilder sb = new StringBuilder(String.format("UPDATE %s SET ", table));
        boolean first = true;


        for (Map.Entry<String, ObjectValue> entry: objects.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(String.format("%s = '%s'", entry.getKey(), entry.getValue().toSql()));
        }

        if (where != null) {
            sb.append(String.format(" WHERE %s", where.toSql()));
        }

        return sb.append(";").toString();
    }

    @Override
    public String toString() {
        return toSql();
    }
}
