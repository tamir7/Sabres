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

import android.database.Cursor;

import com.jakewharton.fliptables.FlipTable;

final class SqliteMasterTable {
    private static final String TABLE_NAME = "sqlite_master";
    private static final String NAME_KEY = "name";
    private static final String TYPE_KEY = "type";
    private static final String TABLE_NAME_KEY = "tbl_name";
    private static final String ANDROID_METADATA_TABLE = "android_metadata";
    private static final String SCHEMA_TABLE = SchemaTable.getTableName();

    private static final String[] tableHeaders = new String[] {"table", "count"};
    private static final String[] indexHeaders = new String[] {"table", "index"};

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

    static String getTables(Sabres sabres) throws SabresException {
        Cursor c = null;
        try {
            SelectCommand command = new SelectCommand(TABLE_NAME);
            command.where(Where.equalTo(TYPE_KEY, Type.Table.toString()).
                    and(Where.notEqualTo(NAME_KEY, ANDROID_METADATA_TABLE).
                    and(Where.notEqualTo(NAME_KEY, SCHEMA_TABLE))));
            c = sabres.select(command.toSql());
            String[][] data = new String[c.getCount()][tableHeaders.length];
            int i = 0;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                final String table = CursorHelper.getString(c, NAME_KEY);
                data[i++] = new String[] {table,
                        String.valueOf(sabres.count(new CountCommand(table).toSql()))};
            }

            return FlipTable.of(tableHeaders, data);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    static String getIndices(Sabres sabres) throws SabresException {
        Cursor c = null;
        try {
            SelectCommand command = new SelectCommand(TABLE_NAME);
            command.where(Where.equalTo(TYPE_KEY, Type.Index.toString()).
                    and(Where.notEqualTo(TABLE_NAME_KEY, SCHEMA_TABLE)));
            c = sabres.select(command.toSql());
            String[][] data = new String[c.getCount()][indexHeaders.length];
            int i = 0;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                data[i++] = new String[] {CursorHelper.getString(c, TABLE_NAME_KEY),
                        CursorHelper.getString(c, NAME_KEY)};
            }
            return FlipTable.of(indexHeaders, data);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
