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

import java.util.ArrayList;
import java.util.List;

final class Where {
    private enum Operator {
        Equal("="),
        NotEqual("!="),
        GreaterThen(">"),
        GreaterThenOrEqual(">="),
        LessThen("<"),
        LessThenOrEqual("<=");

        private final String value;

        Operator(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private String where;
    private final List<String> keyIndices = new ArrayList<>();

    private Where(String key, Object value, Operator operator) {
        keyIndices.add(key);
        where = key + operator.toString() + String.format("'%s'", String.valueOf(value));
    }

    public static Where equalTo(String key, Object value) {
        return new Where(key, value, Operator.Equal);
    }

    public static Where notEqualTo(String key, Object value) {
        return new Where(key, value, Operator.NotEqual);
    }

    public static Where greaterThen(String key, Object value) {
        return new Where(key, value, Operator.GreaterThen);
    }

    public static Where greaterThenOrEqual(String key, Object value) {
        return new Where(key, value, Operator.GreaterThenOrEqual);
    }

    public static Where lessThen(String key, Object value) {
        return new Where(key, value, Operator.LessThen);
    }

    public static Where lessThenOrEqual(String key, Object value) {
        return new Where(key, value, Operator.LessThenOrEqual);
    }

    public Where and(Where andWhere) {
        keyIndices.addAll(andWhere.getKeyIndices());
        where = String.format("( %s AND %s )", where, andWhere.toString());
        return this;
    }

    List<String> getKeyIndices() {
        return keyIndices;
    }

    String toSql() {
        return where;
    }

    @Override
    public String toString() {
        return toSql();
    }
}
