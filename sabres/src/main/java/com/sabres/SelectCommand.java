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

final class SelectCommand {
    private final String table;
    private String joinTable;
    private String joinColumn;
    private Where where;

    SelectCommand(String table) {
        this.table = table;
    }

    void join(String table, String column) {
        joinTable = table;
        joinColumn = column;
    }

    void where(Where where) {
        this.where = where;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SELECT * FROM ").append(table);

        if (joinTable != null) {
            sb.append(String.format(" LEFT JOIN %s ON %s.%s = %s.objectId", joinTable, table, joinColumn,
                    joinTable));
        }

        if (where != null) {
            sb.append(String.format(" WHERE %s", where.toString()));
        }

        return sb.append(";").toString();
    }
}
