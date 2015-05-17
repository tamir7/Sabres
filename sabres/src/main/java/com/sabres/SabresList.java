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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class SabresList {
    private static final String LIST_PREFIX = "_sabres_list";
    private static final String PARENT_ID_KEY = "_parentId";
    private static final String VALUE_KEY = "_value";
    private static final String[] selectKeys = new String[] {PARENT_ID_KEY, VALUE_KEY};
    private final String parent;
    private final String parentKey;

    private SabresList(String parent, String parentKey) {
        this.parent = parent;
        this.parentKey = parentKey;
    }

    static SabresList get(Sabres sabres, String parent, String parentKey)
        throws SabresException {
        SabresList collection = new SabresList(parent, parentKey);
        collection.create(sabres);
        return collection;
    }

    static String getPrefix() {
        return LIST_PREFIX;
    }

    static String getTableName(String parentName, String parentKey) {
        return String.format("%s_%s_%s", LIST_PREFIX, parentName, parentKey);
    }

    static String getParentIdKey() {
        return PARENT_ID_KEY;
    }

    static String getValueKey() {
        return VALUE_KEY;
    }

    private void create(Sabres sabres) throws SabresException {
        CreateTableCommand createCommand = new CreateTableCommand(getTableName()).ifNotExists().
            withColumn(new Column(PARENT_ID_KEY, SqlType.Integer).foreignKeyIn(parent).notNull()).
            withColumn(new Column(VALUE_KEY, SqlType.Text).notNull()).
            unique(new String[] {PARENT_ID_KEY, VALUE_KEY}).
            withConflictResolution(CreateTableCommand.ConflictResolution.REPLACE);

        CreateIndexCommand indexCommand = new CreateIndexCommand(getTableName(),
            Collections.singletonList(VALUE_KEY)).ifNotExists();

        sabres.beginTransaction();
        try {
            sabres.execSQL(createCommand.toSql());
            sabres.execSQL(indexCommand.toSql());
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
        }
    }

    <T> List<T> select(Sabres sabres, long parentId, SabresDescriptor descriptor) {
        List<Object> list = new ArrayList<>();
        Cursor c = null;
        try {
            SelectCommand command = new SelectCommand(getTableName(),
                Arrays.asList(selectKeys)).where(Where.equalTo(PARENT_ID_KEY,
                new LongValue(parentId)));
            c = sabres.select(command.toSql());
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                switch (descriptor.getOfType()) {
                    case Integer:
                        list.add(CursorHelper.getInt(c, VALUE_KEY));
                        break;
                    case Double:
                        list.add(CursorHelper.getDouble(c, VALUE_KEY));
                        break;
                    case Float:
                        list.add(CursorHelper.getFloat(c, VALUE_KEY));
                        break;
                    case String:
                        list.add(CursorHelper.getString(c, VALUE_KEY));
                        break;
                    case Byte:
                        list.add(CursorHelper.getByte(c, VALUE_KEY));
                        break;
                    case Short:
                        list.add(CursorHelper.getShort(c, VALUE_KEY));
                        break;
                    case Long:
                        list.add(CursorHelper.getLong(c, VALUE_KEY));
                        break;
                    case Boolean:
                        list.add(CursorHelper.getBoolean(c, VALUE_KEY));
                        break;
                    case Date:
                        list.add(CursorHelper.getDate(c, VALUE_KEY));
                        break;
                    case Pointer:
                        list.add(SabresObject.createWithoutData(descriptor.getName(),
                            CursorHelper.getLong(c, VALUE_KEY)));
                        break;
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        //noinspection unchecked
        return (List<T>)list;
    }

    void insert(Sabres sabres, long parentId, List<?> list)
        throws SabresException {
        Iterator it = list.iterator();
        sabres.beginTransaction();
        try {
            while (it.hasNext()) {
                Map<String, SabresValue> values = new HashMap<>();
                values.put(PARENT_ID_KEY, new LongValue(parentId));
                Object value = it.next();
                values.put(VALUE_KEY, SabresValue.create(value));
                sabres.insert(new InsertCommand(getTableName(), values).toSql());
            }
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
        }
    }

    private String getTableName() {
        return getTableName(parent, parentKey);
    }
}
