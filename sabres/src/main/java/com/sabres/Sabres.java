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

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import bolts.Continuation;
import bolts.Task;

public final class Sabres {
    private final static String TAG = Sabres.class.getSimpleName();
    private final static String DATABASE_NAME = "sabres.db";
    private static Sabres self;
    private boolean debug = false;
    private final Context context;
    private final Semaphore sem = new Semaphore(1, true);
    private SQLiteDatabase database;

    private Sabres(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public static void initialize(Context context) {
        if (self == null)  {
            self = new Sabres(context);
            try {
                self.sem.acquire();
                Task.callInBackground(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        Sabres sabres = Sabres.self;
                        sabres.openWithoutLock();
                        Schema.initialize(sabres);
                        sabres.closeWithoutLock();
                        self.sem.release();
                        return null;
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException("Sabres Initialize failed", e);
            }

        }
    }

    static Sabres self() {
        return self;
    }

    private void log(String sql) {
        if (debug) {
            Log.d(TAG, sql);
        }
    }

    void execSQL(String sql) throws SabresException {
        Utils.checkNotMain();
        try {
            log(sql);
            database.execSQL(sql);
        } catch (SQLException e) {
            throw new SabresException(SabresException.SQL_ERROR,
                    String.format("Failed to exec sql: %s", sql), e);
        }
    }

    long insert(String sql) throws SabresException {
        Utils.checkNotMain();
        log(sql);
        SQLiteStatement statement = null;
        try {
            statement = database.compileStatement(sql);
            return statement.executeInsert();
        } catch (SQLException e) {
            throw new SabresException(SabresException.SQL_ERROR,
                    String.format("Failed to execute insert sql %s", sql), e);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    Cursor select(String sql) {
        Utils.checkNotMain();
        log(sql);
        return database.rawQuery(sql, null);
    }

    long count(String sql) {
        Utils.checkNotMain();
        log(sql);
        return DatabaseUtils.longForQuery(database, sql, null);
    }

    private void openWithoutLock() throws SabresException {
        if (database == null || !database.isOpen()) {
            createDatabase();
        } else {
            database.acquireReference();
        }
    }

    void open() throws SabresException {
        Utils.checkNotMain();
        try {
            sem.acquire();
            openWithoutLock();
            sem.release();
        } catch (InterruptedException e) {
            throw new SabresException(SabresException.OTHER_CAUSE, e.getMessage(), e);
        }
    }

    private void closeWithoutLock() {
        if (database != null && database.isOpen()) {
            database.releaseReference();
        }
    }

    void close() throws SabresException {
        Utils.checkNotMain();
        try {
            sem.acquire();
            closeWithoutLock();
            sem.release();
        } catch (InterruptedException e) {
            throw new SabresException(SabresException.OTHER_CAUSE, e.getMessage(), e);
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
            execSQL("PRAGMA foreign_keys = ON;");
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
                    return SqliteMaster.getTables(sabres);
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
                    return SqliteMaster.getIndices(sabres);
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

    public static <T extends SabresObject> void printSchemaTable(Class<T> clazz) {
        Schema.printSchema(clazz.getSimpleName());
    }
}
