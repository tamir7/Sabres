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
    private StringBuilder where;

    private Where(String key, String value, Operator operator) {
        where = new StringBuilder(key).append(operator.toString()).append(value);
    }

    private Where(String key, List<?> objects, Operator operator) {
        where = new StringBuilder(key).append(operator).append("(");
        boolean first = true;
        for (Object o : objects) {
            if (first) {
                first = false;
            } else {
                where.append(", ");
            }
            where.append(SabresValue.create(o).toSql());
        }

        where.append(")");
    }

    public static Where in(String key, List<?> objects) {
        return new Where(key, objects, Operator.In);
    }

    public static Where in(String key, String statement) {
        return new Where(key, statement, Operator.In);
    }

    public static Where notIn(String key, List<?> objects) {
        return new Where(key, objects, Operator.NotIn);
    }

    public static Where equalTo(String key, SabresValue value) {
        return new Where(key, value.toSql(), Operator.Equal);
    }

    public static Where startsWith(String key, String prefix) {
        return new Where(key, String.format("'%s%%'", prefix), Operator.Like);
    }

    public static Where endsWith(String key, String suffix) {
        return new Where(key, String.format("'%%%s'", suffix), Operator.Like);
    }

    public static Where doesNotEndWith(String key, String suffix) {
        return new Where(key, String.format("'%%%s'", suffix), Operator.NotLike);
    }

    public static Where contains(String key, String substring) {
        return new Where(key, String.format("'%%%s%%'", substring), Operator.Like);
    }

    public static Where doesNotStartWith(String key, String prefix) {
        return new Where(key, String.format("'%s%%'", prefix), Operator.NotLike);
    }

    public static Where notEqualTo(String key, SabresValue value) {
        return new Where(key, value.toSql(), Operator.NotEqual);
    }

    public static Where greaterThan(String key, SabresValue value) {
        return new Where(key, value.toSql(), Operator.GreaterThan);
    }

    public static Where greaterThanOrEqual(String key, SabresValue value) {
        return new Where(key, value.toSql(), Operator.GreaterThanOrEqual);
    }

    public static Where lessThan(String key, SabresValue value) {
        return new Where(key, value.toSql(), Operator.LessThan);
    }

    public static Where lessThanOrEqual(String key, SabresValue value) {
        return new Where(key, value.toSql(), Operator.LessThanOrEqual);
    }

    public static Where is(String key, SabresValue value) {
        return new Where(key, value.toSql(), Operator.Is);
    }

    public static Where isNot(String key, SabresValue value) {
        return new Where(key, value.toSql(), Operator.IsNot);
    }

    public Where and(Where andWhere) {
        where = new StringBuilder(String.format("( %s AND %s )", where.toString(),
            andWhere.toString()));
        return this;
    }

    public Where or(Where orWhere) {
        where = new StringBuilder(String.format("( %s OR %s )", where.toString(),
            orWhere.toString()));
        return this;
    }

    String toSql() {
        return where.toString();
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
