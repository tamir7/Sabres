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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class SabresCollection {
    private static final String PARENT_ID_KEY = "_parentId";
    private static final String VALUE_KEY = "_value";
    private final String parent;
    private final String parentKey;
    private static final String[] selectKeys = new String[] {PARENT_ID_KEY, VALUE_KEY};

    private SabresCollection(String parent, String parentKey) {
        this.parent = parent;
        this.parentKey = parentKey;
    }

    private void create(Sabres sabres) throws SabresException {
        CreateTableCommand createCommand = new CreateTableCommand(getTableName()).ifNotExists().
                withColumn(new Column(PARENT_ID_KEY,
                        SqlType.Integer).foreignKeyIn(parent).notNull()).
                withColumn(new Column(VALUE_KEY, SqlType.Text).notNull());

        CreateIndexCommand indexCommand = new CreateIndexCommand(getTableName(),
                Collections.singletonList(VALUE_KEY)).ifNotExists();

        sabres.beginTransaction();
        try {
            sabres.execSQL(createCommand.toString());
            sabres.execSQL(indexCommand.toString());
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
        }
    }

    static SabresCollection get(Sabres sabres, String parent, String parentKey)
            throws SabresException{
        SabresCollection collection = new SabresCollection(parent, parentKey);
        collection.create(sabres);
        return collection;
    }

    <T> List<T> select(Sabres sabres, long parentId, ObjectDescriptor descriptor) {
        List<Object> list = new ArrayList<>();
        Cursor c = null;
        try {
            SelectCommand command = new SelectCommand(getTableName(),
                    Arrays.asList(selectKeys)).where(Where.equalTo(PARENT_ID_KEY, parentId));
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

    void insert(Sabres sabres, long parentId, Collection<?> collection, ObjectDescriptor descriptor)
            throws SabresException {
        Iterator it = collection.iterator();
        sabres.beginTransaction();
        try {
            while (it.hasNext()) {
                Map<String, ObjectValue> values = new HashMap<>();
                values.put(PARENT_ID_KEY, new ObjectValue(parentId,
                        new ObjectDescriptor(ObjectDescriptor.Type.Integer)));
                Object value = it.next();
                values.put(VALUE_KEY, new ObjectValue(value,
                        new ObjectDescriptor(descriptor.getOfType())));
                sabres.insert(new InsertCommand(getTableName(), values).toSql());
            }
            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
        }
    }

    private String getTableName() {
        return String.format("_%s_%s", parent, parentKey);
    }

    static String getTableName(String parent, String parentKey) {
        return new SabresCollection(parent, parentKey).getTableName();
    }

    static String getParentIdKey() {
        return PARENT_ID_KEY;
    }

    static Collection<String> getSelectKeys() {
        return Arrays.asList(selectKeys);
    }
}
