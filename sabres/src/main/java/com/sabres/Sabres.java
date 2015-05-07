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
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import com.jakewharton.fliptables.FlipTable;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public final class Sabres {
    private final static String DATABASE_NAME = "sabres.db";
    private static Sabres self;
    private final Context context;
    private final Object lock = new Object();
    private SQLiteDatabase database;

    private Sabres(Context context) {
        this.context = context.getApplicationContext();
    }

    public static void initialize(Context context) {
        if (self != null)  {
            throw new IllegalStateException("Sabres library was already initialized");
        }

        self = new Sabres(context);
    }

    void execSQL(String sql) throws SabresException {
        try {
            database.execSQL(sql);
        } catch (SQLException e) {
            throw new SabresException(SabresException.SQL_ERROR,
                    String.format("Failed to exec sql: %s", sql), e);
        }
    }

    static Sabres self() {
        return self;
    }

    long insert(String table, ContentValues values) throws SabresException {
        Utils.checkNotMain();
        try {
            return database.insertOrThrow(table, null, values);
        } catch (SQLException e) {
            throw new SabresException(SabresException.SQL_ERROR,
                    String.format("Failed to insert into table %s, values: %s", table,
                            values.toString()), e);
        }
    }

    long update(String table, ContentValues values, Where where) {
        Utils.checkNotMain();
        return database.update(table, values, where.toString(), null);
    }

    int delete(String table, Where where) {
        Utils.checkNotMain();
        return database.delete(table, where == null ? null : where.toString(), null);
    }

    Cursor select(String table, Where where) {
        Utils.checkNotMain();
        return database.query(table, null, where == null ? null : where.toString(), null, null,
                null, null);
    }

    void open() throws SabresException {
        Utils.checkNotMain();
        synchronized (lock) {
            if (database == null || !database.isOpen()) {
                createDatabase();
            } else {
                database.acquireReference();
            }
        }
    }

    void close() {
        Utils.checkNotMain();
        synchronized (lock) {
            if (database != null && database.isOpen()) {
                database.releaseReference();
            }
        }
    }

    void beginTransaction() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            database.beginTransactionNonExclusive();
        } else {
            database.beginTransaction();
        }
    }

    void endTransaction() {
        database.endTransaction();
    }

    void setTransactionSuccessful() {
        database.setTransactionSuccessful();
    }

    private void createDatabase() throws SabresException {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                database = context.openOrCreateDatabase(DATABASE_NAME,
                        SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING, null);
            } else {
                database = context.openOrCreateDatabase(DATABASE_NAME, 0, null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    database.enableWriteAheadLogging();
                }
            }
            database.execSQL("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            throw new SabresException(SabresException.SQL_ERROR, "Failed to construct database", e);
        }
    }

    private static String getTables() throws SabresException {
        Utils.checkNotMain();
        self.open();
        Cursor c = null;
        try {
            String[] headers = new String[]{"table", "count"};
            c = self.select("sqlite_master",
                    Where.equalTo("type", "table").and(Where.notEqualTo("name", "android_metadata").
                            and(Where.notEqualTo("name", SchemaTable.getTableName()))));
            String[][] data = new String[c.getCount()][2];
            int i = 0;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                final String table = CursorHelper.getString(c, "name");
                data[i++] = new String[]
                        {table, String.valueOf(DatabaseUtils.queryNumEntries(self.database, table))};
            }
            return FlipTable.of(headers, data);
        } finally {
            if (c != null) {
                c.close();
            }
            self.close();
        }
    }

    private static String getIndices() throws SabresException {
        Utils.checkNotMain();
        self.open();
        Cursor c = null;
        try {
            String[] headers = new String[]{"table", "index"};
            c = self.select("sqlite_master", Where.equalTo("type", "index").
                    and(Where.notEqualTo("tbl_name", SchemaTable.getTableName())));
            String[][] data = new String[c.getCount()][2];
            int i = 0;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                data[i++] = new String[] {CursorHelper.getString(c, "tbl_name"),
                        CursorHelper.getString(c, "name")};
            }
            return FlipTable.of(headers, data);
        } finally {
            if (c != null) {
                c.close();
            }
            self.close();
        }
    }

    public static void printTables() {
        Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getTables();
            }
        }).continueWith(new Continuation<String, Void>() {
            @Override
            public Void then(Task<String> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(getClass().getSimpleName(), "getTables failed", task.getError());
                } else {
                    Log.i(getClass().getSimpleName(), String.format("tables:\n%s", task.getResult()));
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public static void printIndices() {
        Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getIndices();
            }
        }).continueWith(new Continuation<String, Void>() {
            @Override
            public Void then(Task<String> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(getClass().getSimpleName(), "getIndices failed", task.getError());
                } else {
                    Log.i(getClass().getSimpleName(), String.format("indices:\n%s", task.getResult()));
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private static String getSchemaTable() throws SabresException {
        Utils.checkNotMain();
        self.open();
        String[] headers = SchemaTable.getHeaders();
        String [][] data = SchemaTable.getData(self);
        self.close();
        return FlipTable.of(headers, data);
    }

    public static void printSchemaTable() {
        Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                self.open();
                SchemaTable.create(self);
                String schemaTable = getSchemaTable();
                self.close();
                return schemaTable;
            }
        }).continueWith(new Continuation<String, Void>() {
            @Override
            public Void then(Task<String> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(getClass().getSimpleName(), "getSchemaTable failed", task.getError());
                } else {
                    Log.i(getClass().getSimpleName(), String.format("schema_table:\n%s", task.getResult()));
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    static boolean tableExists(Sabres sabres, String table) {
        return DatabaseUtils.longForQuery(sabres.database,
                String.format("SELECT count(*) FROM sqlite_master WHERE type='table' AND  name='%s'",
                        table), null) > 0;
    }
}
