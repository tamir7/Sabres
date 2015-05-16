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

import java.util.ArrayList;
import java.util.List;

final class CreateTableCommand {
    private final List<Column> columns = new ArrayList<>();
    private final String name;
    private final List<String[]> uniqueColumns = new ArrayList<>();
    private ConflictResolution resolution;
    private boolean ifNotExists = false;

    CreateTableCommand(String name) {
        this.name = name;
    }

    CreateTableCommand ifNotExists() {
        ifNotExists = true;
        return this;
    }

    CreateTableCommand withConflictResolution(ConflictResolution resolution) {
        this.resolution = resolution;
        return this;
    }

    CreateTableCommand withColumn(Column column) {
        columns.add(column);
        return this;
    }

    CreateTableCommand unique(String[] columnNames) {
        uniqueColumns.add(columnNames);
        return this;
    }

    @Override
    public String toString() {
        return toSql();
    }

    String toSql() {
        final StringBuilder sb = new StringBuilder("CREATE TABLE ");

        if (ifNotExists) {
            sb.append("IF NOT EXISTS ");
        }

        sb.append(name).append("(");

        boolean first = true;
        for (Column column : columns) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(column.toString());
        }

        if (!uniqueColumns.isEmpty()) {
            for (String[] columns : uniqueColumns) {
                sb.append(", UNIQUE(");
                first = true;
                for (String column : columns) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }

                    sb.append(column);
                }
                sb.append(")");
            }
        }

        if (resolution != null) {
            sb.append(String.format(" %s", resolution.toString()));
        }

        return sb.append(");").toString();
    }

    enum ConflictResolution {
        REPLACE("REPLACE");

        private final String text;

        ConflictResolution(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "ON CONFLICT " + text;
        }
    }
}
