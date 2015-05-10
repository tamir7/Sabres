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

public class SqliteMasterTable {
    private static final String TABLE_NAME = "sqlite_master";
    private static final String NAME_KEY = "name";
    private static final String TYPE_KEY = "type";
    private static final String TABLE_NAME_KEY = "tbl_name";

    private enum Type {
        Table("table"),
        Index("index");

        private String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static boolean tableExists(Sabres sabres, String table) {
        CountCommand command = new CountCommand(TABLE_NAME);
        command.where(Where.equalTo(TYPE_KEY, Type.Table.toString()).
                and(Where.equalTo(NAME_KEY, table)));
        return sabres.count(command.toSql()) != 0;
    }
}
