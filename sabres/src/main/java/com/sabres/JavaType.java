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

enum JavaType {
    Integer("Integer") {
        @Override
        SqlType toSqlType() {
            return SqlType.Integer;
        }
    },
    Double("Double") {
        @Override
        SqlType toSqlType() {
            return SqlType.Real;
        }
    },
    Float("Float") {
        @Override
        SqlType toSqlType() {
            return SqlType.Real;
        }
    },
    String("String") {
        @Override
        SqlType toSqlType() {
            return SqlType.Text;
        }
    },
    Byte("Byte") {
        @Override
        SqlType toSqlType() {
            return SqlType.Integer;
        }
    },
    Short("Short") {
        @Override
        SqlType toSqlType() {
            return SqlType.Integer;
        }
    },
    Long("Long") {
        @Override
        SqlType toSqlType() {
            return SqlType.Integer;
        }
    },
    Boolean("Boolean") {
        @Override
        SqlType toSqlType() {
            return SqlType.Integer;
        }
    },
    Date("Date") {
        @Override
        SqlType toSqlType() {
            return SqlType.Integer;
        }
    };


    private String text;

    JavaType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    abstract SqlType toSqlType();
}

