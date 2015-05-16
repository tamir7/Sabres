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

final class DeleteCommand {
    private final String table;
    private Where where;

    DeleteCommand(String table) {
        this.table = table;
    }

    DeleteCommand where(Where where) {
        this.where = where;
        return this;
    }

    String toSql() {
        StringBuilder sb = new StringBuilder(String.format("DELETE FROM %s", table));

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
