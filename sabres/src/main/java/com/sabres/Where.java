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

import java.util.List;

final class Where {
    private String where;

    private Where(String key, Object value, Operator operator) {
        where = key + operator.toString() + String.format("'%s'", String.valueOf(value));
    }

    private Where(String key, List<Object> objects, Operator operator) {
        StringBuilder sb = new StringBuilder(key).append(operator).append("(");
        boolean first = true;
        for (Object o : objects) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(String.valueOf(o));
        }
        where = sb.append(")").toString();
    }

    public static Where in(String key, List<String> objects) {
        return new Where(key, objects, Operator.In);
    }

    public static Where notIn(String key, List<String> objects) {
        return new Where(key, objects, Operator.NotIn);
    }

    public static Where equalTo(String key, Object value) {
        return new Where(key, value, Operator.Equal);
    }

    public static Where startsWith(String key, String prefix) {
        return new Where(key, String.format("%s%%", prefix), Operator.Like);
    }

    public static Where endsWith(String key, String suffix) {
        return new Where(key, String.format("%%%s", suffix), Operator.Like);
    }

    public static Where doesNotEndWith(String key, String suffix) {
        return new Where(key, String.format("%%%s", suffix), Operator.NotLike);
    }

    public static Where contains(String key, String substring) {
        return new Where(key, String.format("%%%s%%", substring), Operator.Like);
    }

    public static Where doesNotStartWith(String key, String prefix) {
        return new Where(key, String.format("%s%%", prefix), Operator.NotLike);
    }

    public static Where notEqualTo(String key, Object value) {
        return new Where(key, value, Operator.NotEqual);
    }

    public static Where greaterThan(String key, Object value) {
        return new Where(key, value, Operator.GreaterThan);
    }

    public static Where greaterThanOrEqual(String key, Object value) {
        return new Where(key, value, Operator.GreaterThanOrEqual);
    }

    public static Where lessThan(String key, Object value) {
        return new Where(key, value, Operator.LessThan);
    }

    public static Where lessThanOrEqual(String key, Object value) {
        return new Where(key, value, Operator.LessThanOrEqual);
    }

    public static Where is(String key, Object value) {
        return new Where(key, value, Operator.Is);
    }

    public static Where isNot(String key, Object value) {
        return new Where(key, value, Operator.IsNot);
    }

    public Where and(Where andWhere) {
        where = String.format("( %s AND %s )", where, andWhere.toString());
        return this;
    }

    public Where or(Where orWhere) {
        where = String.format("( %s OR %s )", where, orWhere.toString());
        return this;
    }

    String toSql() {
        return where;
    }

    @Override
    public String toString() {
        return toSql();
    }

    private enum Operator {
        Equal("="),
        NotEqual("!="),
        GreaterThan(">"),
        GreaterThanOrEqual(">="),
        LessThan("<"),
        LessThanOrEqual("<="),
        Like(" LIKE "),
        NotLike(" NOT LIKE "),
        Is(" IS "),
        IsNot(" IS NOT "),
        In(" IN "),
        NotIn(" NOT IN ");

        private final String value;

        Operator(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
