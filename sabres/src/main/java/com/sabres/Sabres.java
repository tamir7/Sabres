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

    static Sabres self() {
        return self;
    }

    void execSQL(String sql) throws SabresException {
        try {
            database.execSQL(sql);
        } catch (SQLException e) {
            throw new SabresException(SabresException.SQL_ERROR,
                    String.format("Failed to exec sql: %s", sql), e);
        }
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

    Cursor select(String sql) {
        Utils.checkNotMain();
        return database.rawQuery(sql, null);
    }

    long count(String sql) {
        Utils.checkNotMain();
        return DatabaseUtils.longForQuery(database, sql, null);
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

    public static void printTables() {
        Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Sabres sabres = Sabres.self;
                sabres.open();
                try {
                    return SqliteMasterTable.getTables(sabres);
                } finally {
                    sabres.close();
                }
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
                Sabres sabres = Sabres.self;
                sabres.open();
                try {
                    return SqliteMasterTable.getIndices(sabres);
                } finally {
                    sabres.close();
                }
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

    public static void testFunction() {
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SelectCommand command = new SelectCommand("Movie");
                command.join("Director", "director");
                command.where(Where.equalTo("title", "Fight Club"));
                Sabres sabres = Sabres.self;
                sabres.open();
                Cursor c = sabres.database.rawQuery(command.toString(), null);
                DatabaseUtils.dumpCursor(c);
                c.close();
                sabres.close();
                return null;
            }
        });




    }
}
