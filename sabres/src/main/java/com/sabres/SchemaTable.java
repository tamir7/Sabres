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

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Map;

final class SchemaTable {
    private static final String SCHEMA_TABLE_NAME = "_schema_table";
    private static final String TABLE_KEY = "_table";
    private static final String COLUMN_KEY = "_column";
    private static final String TYPE_KEY = "_type";

    private SchemaTable() {}

    static void create(Sabres sabres) throws SabresException {
        CreateTableCommand createCommand = new CreateTableCommand(SCHEMA_TABLE_NAME).
                ifNotExists().
                withColumn(new Column(SabresObject.ID_KEY, SqlType.Integer).primaryKey().notNull()).
                withColumn(new Column(TABLE_KEY, SqlType.Text).notNull()).
                withColumn(new Column(COLUMN_KEY, SqlType.Text).notNull()).
                withColumn(new Column(TYPE_KEY, SqlType.Text).notNull());

        CreateIndexCommand indexCommand =
                new CreateIndexCommand(SCHEMA_TABLE_NAME, TABLE_KEY).ifNotExists();

        sabres.beginTransaction();
        try {
            sabres.execSQL(createCommand.toString());
            sabres.execSQL(indexCommand.toString());
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
        }
    }

    static Schema select(Sabres sabres, String name) {
        Cursor c = sabres.select(SCHEMA_TABLE_NAME, Where.equalTo(TABLE_KEY, name));
        Schema schema = new Schema();
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            schema.put(CursorHelper.getString(c, COLUMN_KEY),
                    JavaType.valueOf(CursorHelper.getString(c, TYPE_KEY)));
        }

        c.close();
        return schema;
    }

    static String[] getHeaders() {
        return new String[]{TABLE_KEY, COLUMN_KEY, TYPE_KEY};
    }

    static String[][] getData(Sabres sabres) {
        final Cursor c = sabres.select(SCHEMA_TABLE_NAME, null);
        String[][] data = new String[c.getCount()][c.getColumnCount()];
        int i = 0;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String[] row = new String[] {CursorHelper.getString(c, TABLE_KEY),
            CursorHelper.getString(c, COLUMN_KEY), CursorHelper.getString(c, TYPE_KEY)};
            data[i++] = row;
        }
        c.close();
        return data;
    }

    static void insert(Sabres sabres, String name, Schema schema) throws SabresException {
        sabres.beginTransaction();
        try {
            for (Map.Entry<String, JavaType> entry: schema.getTypes().entrySet()) {
                ContentValues values = new ContentValues();
                values.put(TABLE_KEY, name);
                values.put(COLUMN_KEY, entry.getKey());
                values.put(TYPE_KEY, entry.getValue().name());
                sabres.insert(SCHEMA_TABLE_NAME, values);
            }

            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
        }
    }

    static String getTableName() {
        return SCHEMA_TABLE_NAME;
    }
}
