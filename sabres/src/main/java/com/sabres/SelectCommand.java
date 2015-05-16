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
import java.util.Collection;
import java.util.List;

final class SelectCommand {
    private final String table;
    private final List<String> keys = new ArrayList<>();
    private final List<Join> joins = new ArrayList<>();
    private List<OrderBy> orderByList = new ArrayList<>();
    private Integer limit;
    private Integer skip;

    private Where where;

    SelectCommand(String table, Collection<String> selectKeys) {
        this.table = table;
        this.keys.addAll(selectKeys);
    }

    SelectCommand join(String table, String column, Collection<String> selectKeys) {
        joins.add(new Join(table, column, selectKeys));
        return this;
    }

    SelectCommand orderBy(OrderBy orderBy) {
        orderByList.add(orderBy);
        return this;
    }

    SelectCommand where(Where where) {
        this.where = where;
        return this;
    }

    SelectCommand withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    SelectCommand withSkip(int skip) {
        this.skip = skip;
        return this;
    }

    @Override
    public String toString() {
        return toSql();
    }

    String toSql() {
        StringBuilder sb = new StringBuilder("SELECT ");
        boolean first = true;

        for (String key : keys) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(String.format("%s.%s", table, key));
        }

        StringBuilder joinSb = new StringBuilder();

        for (Join join : joins) {
            joinSb.append(String.format(" LEFT JOIN %s ON %s.%s = %s.objectId", join.table, table,
                join.column, join.table));
            for (String key : join.keys) {
                sb.append(String.format(", %s.%s AS %s_%s", join.table, key, join.column, key));
            }
        }

        sb.append(String.format(" FROM %s", table));

        sb.append(joinSb);

        if (where != null) {
            sb.append(String.format(" WHERE %s", where.toString()));
        }

        first = true;
        for (OrderBy orderBy : orderByList) {
            if (first) {
                sb.append(" ORDER BY ");
            } else {
                sb.append(", ");
                first = false;
            }
            sb.append(orderBy.toSql());
        }

        if (limit != null) {
            sb.append(String.format(" LIMIT %d", limit));
        }

        if (skip != null) {
            sb.append(String.format(" OFFSET %d", skip));
        }

        return sb.append(";").toString();
    }

    private static class Join {
        private final String table;
        private final String column;
        private final List<String> keys = new ArrayList<>();

        Join(String table, String column, Collection<String> selectKeys) {
            this.table = table;
            this.column = column;
            keys.addAll(selectKeys);
        }
    }
}
