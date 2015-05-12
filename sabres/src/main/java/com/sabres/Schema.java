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
import android.util.Log;

import com.jakewharton.fliptables.FlipTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class Schema {
    private static final String TAG = Schema.class.getSimpleName();
    private static final Map<String, Map<String, ObjectDescriptor>> schemas = new HashMap<>();
    private static final String UNDEFINED = "(undefined)";
    private static final String SCHEMA_TABLE_NAME = "_schema_table";
    private static final String TABLE_KEY = "_table";
    private static final String COLUMN_KEY = "_column";
    private static final String TYPE_KEY = "_type";
    private static final String OF_TYPE_KEY = "_ofType";
    private static final String NAME_KEY = "_name";
    private static final String[] headers =
            new String[]{COLUMN_KEY, TYPE_KEY, OF_TYPE_KEY, NAME_KEY};
    private static final String[] selectKeys =
            new String[]{TABLE_KEY, COLUMN_KEY, TYPE_KEY, OF_TYPE_KEY, NAME_KEY};

    private Schema() {}

    static void initialize(Sabres sabres) throws SabresException {
        if (SqliteMaster.tableExists(sabres, SCHEMA_TABLE_NAME)) {
            Cursor c = null;
            Set<String> subClassNames = SabresObject.getSubClassNames();
            if (!subClassNames.isEmpty()) {
                try {
                    SelectCommand command = new SelectCommand(SCHEMA_TABLE_NAME,
                            Arrays.asList(selectKeys));
                    boolean first = true;
                    Where where = null;

                    for (String name: subClassNames) {
                        schemas.put(name, new HashMap<String, ObjectDescriptor>());
                        if (first) {
                            where = Where.equalTo(TABLE_KEY, name);
                            first = false;
                        }  else {
                            where.or(Where.equalTo(TABLE_KEY, name));
                        }
                    }
                    command.where(where);
                    c = sabres.select(command.toSql());
                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        String table = CursorHelper.getString(c, TABLE_KEY);
                        String column = CursorHelper.getString(c, COLUMN_KEY);
                        ObjectDescriptor.Type type =
                                ObjectDescriptor.Type.valueOf(CursorHelper.getString(c, TYPE_KEY));
                        ObjectDescriptor.Type ofType = null;
                        String objectName = null;
                        if (type.equals(ObjectDescriptor.Type.Collection)) {
                            ofType = ObjectDescriptor.Type.valueOf(CursorHelper.getString(c,
                                            OF_TYPE_KEY));
                        }

                        if (type.equals(ObjectDescriptor.Type.Pointer) ||
                                (ofType != null && ofType.equals(ObjectDescriptor.Type.Pointer))) {
                            objectName = CursorHelper.getString(c, NAME_KEY);
                        }
                        schemas.get(table).put(column, new ObjectDescriptor(type, ofType,
                                objectName));
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        } else {
            create(sabres);
        }
    }

    private static void create(Sabres sabres) throws SabresException {
        CreateTableCommand createCommand = new CreateTableCommand(SCHEMA_TABLE_NAME).
                ifNotExists().
                withColumn(new Column(TABLE_KEY, SqlType.Text).notNull()).
                withColumn(new Column(COLUMN_KEY, SqlType.Text).notNull()).
                withColumn(new Column(TYPE_KEY, SqlType.Text).notNull()).
                withColumn(new Column(OF_TYPE_KEY, SqlType.Text)).
                withColumn(new Column(NAME_KEY, SqlType.Text));

        CreateIndexCommand indexCommand = new CreateIndexCommand(SCHEMA_TABLE_NAME,
                Collections.singletonList(TABLE_KEY)).ifNotExists();

        sabres.beginTransaction();
        try {
            sabres.execSQL(createCommand.toString());
            sabres.execSQL(indexCommand.toString());
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
        }
    }

    static Map<String, ObjectDescriptor> getSchema(String name) {
        return schemas.get(name);
    }

    static ObjectDescriptor getDescriptor(String name, String key) {
        Map<String, ObjectDescriptor> schema = getSchema(name);
        if (schema != null) {
            return schema.get(key);
        }

        return null;
    }

    static void update(Sabres sabres, String name, Map<String, ObjectDescriptor> schema)
            throws SabresException {
        ObjectDescriptor objectDescriptor = new ObjectDescriptor(ObjectDescriptor.Type.String);
        sabres.beginTransaction();
        try {
            for (Map.Entry<String, ObjectDescriptor> entry : schema.entrySet()) {
                Map<String, ObjectValue> values = new HashMap<>();
                values.put(TABLE_KEY, new ObjectValue(name, objectDescriptor));
                values.put(COLUMN_KEY, new ObjectValue(entry.getKey(), objectDescriptor));
                values.put(TYPE_KEY, new ObjectValue(entry.getValue().getType().name(),
                        objectDescriptor));
                if (entry.getValue().getOfType() != null) {
                    values.put(OF_TYPE_KEY, new ObjectValue(entry.getValue().getOfType().name(),
                            objectDescriptor));
                }
                if (entry.getValue().getName() != null) {
                    values.put(NAME_KEY, new ObjectValue(entry.getValue().getName(), objectDescriptor));
                }
                sabres.insert(new InsertCommand(SCHEMA_TABLE_NAME, values).toSql());
            }

            Map<String, ObjectDescriptor> currentSchema = getSchema(name);
            if (currentSchema == null) {
                schemas.put(name, schema);
            } else {
                currentSchema.putAll(schema);
            }
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
        }
    }

    static Collection<String> getKeys(String name) {
        List<String> keys = new ArrayList<>();
        keys.add(SabresObject.getObjectIdKey());
        keys.addAll(schemas.get(name).keySet());
        return keys;
    }

    static Collection<String> getListsKeys(String name) {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, ObjectDescriptor> entry: schemas.get(name).entrySet()) {
            if (entry.getValue().getType().equals(ObjectDescriptor.Type.Collection)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    static void printSchema(String table) {
        Map<String, ObjectDescriptor> schema = getSchema(table);

        if (schema == null || schema.isEmpty()) {
            Log.w(TAG, String.format("Schema for object %s does not exist", table));
            return;
        }

        String[][] data = new String[schema.size()][headers.length];
        int i = 0;
        for (Map.Entry<String, ObjectDescriptor> entry: schema.entrySet()) {
            String[] row = new String[] {entry.getKey(), entry.getValue().getType().toString(),
                    entry.getValue().getOfType() == null ? UNDEFINED :
                            entry.getValue().getOfType().name(),
                    entry.getValue().getName() == null ? UNDEFINED : entry.getValue().getName()};
            data[i++] = row;
        }

        Log.i(TAG, String.format("Schema for object %s:\n%s", table, FlipTable.of(headers, data)));
    }

    static String getTableName() {
        return SCHEMA_TABLE_NAME;
    }
}
