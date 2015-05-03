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

import com.jakewharton.fliptables.FlipTable;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

abstract public class SabresObject {
    static final String ID_KEY = "_id";
    private final ContentValues values = new ContentValues();
    private final Schema schema = new Schema();
    private final String name;
    private long id = 0;

    protected SabresObject() {
        name = getClass().getSimpleName();
    }

    public SabresObject(String name) {
        this.name = name;
    }

    public void put(String key, Object value) {
        if (value instanceof String) {
            schema.put(key, JavaType.String);
            values.put(key, (String)value);
        } else if (value instanceof Integer) {
            schema.put(key, JavaType.Integer);
            values.put(key, (Integer)value);
        } else if (value instanceof Date) {
            schema.put(key, JavaType.Date);
            values.put(key, ((Date)value).getTime());
        } else if (value instanceof Boolean) {
            schema.put(key, JavaType.Boolean);
            values.put(key, (Boolean)value);
        } else if (value instanceof Long) {
            schema.put(key, JavaType.Long);
            values.put(key, (Long)value);
        } else if (value instanceof Short) {
            schema.put(key, JavaType.Short);
            values.put(key, (Short)value);
        } else if (value instanceof Byte) {
            schema.put(key, JavaType.Byte);
            values.put(key, (Byte)value);
        } else {
            throw new IllegalArgumentException(String.format("Class %s is not supported",
                    value.getClass().getSimpleName()));
        }
    }

    public Object get(String key) {
        JavaType type = schema.getType(key);
        if (type != null) {
            switch (type) {
                case Integer:
                    return getInt(key);
                case Double:
                    return getDouble(key);
                case Float:
                    return getFloat(key);
                case String:
                    return getString(key);
                case Byte:
                    return getByte(key);
                case Short:
                    return getShort(key);
                case Long:
                    return getLong(key);
                case Boolean:
                    return getBoolean(key);
                case Date:
                    return getDate(key);
            }
        }

        return null;
    }

    public long getObjectId() {
        return id;
    }

    public String getString(String key) {
        return values.getAsString(key);
    }

    public Boolean getBoolean(String key)  {
        return values.getAsBoolean(key);
    }

    public Integer getInt(String key) {
        return values.getAsInteger(key);
    }

    public Byte getByte(String key) {
        return values.getAsByte(key);
    }

    public Short getShort(String key) {
        return values.getAsShort(key);
    }

    public Long getLong(String key) {
        return values.getAsLong(key);
    }

    public Float getFloat(String key) {
        return values.getAsFloat(key);
    }

    public Double getDouble(String key) {
        return values.getAsDouble(key);
    }

    public Date getDate(String key) {
        Long time = values.getAsLong(key);
        return time != null ? new Date(time) : null;
    }

    public Task<Void> saveInBackground() {
        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                save();
                return null;
            }
        });
    }

    public void saveInBackground(final SaveCallback callback) {
        saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                callback.done(SabresException.construct(task.getError()));
                return null;
            }
        });
    }

    // TODO: Build indices automatically.
    public void save() throws SabresException {
        final Sabres sabres = Sabres.self();
        sabres.open();
        sabres.beginTransaction();

        try {
            SchemaTable.create(sabres);
            Schema currentSchema = SchemaTable.select(sabres, name);
            if (currentSchema.isEmpty()) {
                SchemaTable.insert(sabres, name, schema);
                createTable(sabres, schema);
            } else {
                Schema newSchema = currentSchema.update(schema);
                if (!newSchema.isEmpty()) {
                    SchemaTable.insert(sabres, name, newSchema);
                    alterTable(sabres, newSchema);
                }
            }

            if (id == 0) {
                id = insert(sabres);
            } else {
                update(sabres);
            }

            sabres.setTransactionSuccessful();
        } finally {
            sabres.endTransaction();
            sabres.close();
        }
    }

    private void createTable(Sabres sabres, Schema schema) throws SabresException {
        CreateTableCommand createCommand = new CreateTableCommand(name).ifNotExists();
        createCommand.withColumn(new Column(ID_KEY, SqlType.Integer).primaryKey().notNull());
        for (Map.Entry<String, JavaType> entry: schema.getTypes().entrySet()) {
            createCommand.withColumn(new Column(entry.getKey(), entry.getValue().toSqlType()));
        }

        sabres.execSQL(createCommand.toString());
    }

    private void alterTable(Sabres sabres, Schema schema) throws SabresException {
        AlterTableCommand alterCommand = new AlterTableCommand(name);
        for (Map.Entry<String, JavaType> entry: schema.getTypes().entrySet()) {
            alterCommand.addColumn(new Column(entry.getKey(), entry.getValue().toSqlType()));
        }

        sabres.execSQL(alterCommand.toString());
    }

    private long insert(Sabres sabres) throws SabresException {
        return sabres.insert(name, values);
    }

    // TODO: Keep dirty flags and update only stuff that's actually needed to be updated.
    private void update(Sabres sabres) {
        sabres.update(name, values, Where.equalTo(ID_KEY, id));
    }

    void populate(Cursor c, Schema schema) {
        id = CursorHelper.getLong(c, ID_KEY);
        this.schema.putAll(schema);
        for (Map.Entry<String, JavaType> entry: schema.getTypes().entrySet()) {
            switch (entry.getValue()) {
                case Integer:
                    values.put(entry.getKey(), CursorHelper.getInt(c, entry.getKey()));
                    break;
                case Double:
                    values.put(entry.getKey(), CursorHelper.getDouble(c, entry.getKey()));
                    break;
                case Float:
                    values.put(entry.getKey(), CursorHelper.getFloat(c, entry.getKey()));
                    break;
                case String:
                    values.put(entry.getKey(), CursorHelper.getString(c, entry.getKey()));
                    break;
                case Byte:
                    values.put(entry.getKey(), CursorHelper.getInt(c, entry.getKey()));
                    break;
                case Short:
                    values.put(entry.getKey(), CursorHelper.getShort(c, entry.getKey()));
                    break;
                case Long:
                    values.put(entry.getKey(), CursorHelper.getLong(c, entry.getKey()));
                    break;
                case Boolean:
                    values.put(entry.getKey(), CursorHelper.getInt(c, entry.getKey()));
                    break;
                case Date:
                    values.put(entry.getKey(), CursorHelper.getLong(c, entry.getKey()));
                    break;
            }
        }
    }

    @Override
    public String toString() {
        String[] headers = schema.toHeaders();
        String[][] data = new String[1][values.size() + 1];
        int i = 0;
        for (Map.Entry<String, Object> entry: values.valueSet()) {
            data[i++] = new String[] {String.valueOf(id), entry.getValue().toString()};
        }

        return FlipTable.of(headers, data);
    }
}
