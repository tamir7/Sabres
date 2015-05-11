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

import java.util.Date;

final class CursorHelper {
    private CursorHelper() {}

    static String getString(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndex(column));
    }

    static Boolean getBoolean(Cursor cursor, String column) {
        Integer intValue =  cursor.getInt(cursor.getColumnIndex(column));
        if (intValue != null) {
            return intValue != 0;
        }

        return null;
    }

    static Integer getInt(Cursor cursor, String column) {
        return cursor.getInt(cursor.getColumnIndex(column));
    }

    static Byte getByte(Cursor cursor, String column) {
        Integer intValue =  cursor.getInt(cursor.getColumnIndex(column));
        if (intValue != null) {
            return intValue.byteValue();
        }

        return null;

    }

    static Short getShort(Cursor cursor, String column) {
        return cursor.getShort(cursor.getColumnIndex(column));
    }

    static Date getDate(Cursor cursor, String column) {
        Long longValue =  cursor.getLong(cursor.getColumnIndex(column));
        if (longValue != null) {
            return new Date(longValue);
        }

        return null;
    }

    static Long getLong(Cursor cursor, String column) {
        return cursor.getLong(cursor.getColumnIndex(column));
    }

    static Float getFloat(Cursor cursor, String column) {
        return cursor.getFloat(cursor.getColumnIndex(column));
    }

    static Double getDouble(Cursor cursor, String column) {
        return cursor.getDouble(cursor.getColumnIndex(column));
    }
}
