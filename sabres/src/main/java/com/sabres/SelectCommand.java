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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SelectCommand {
    private final String table;
    private final List<String> keys = new ArrayList<>();
    private final List<Join> joins = new ArrayList<>();
    private final List<OrderBy> orderByList = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();
    private final Map<String, String> alias = new HashMap<>();
    private Integer limit;
    private Integer skip;
    private Where having;
    private SelectCommand innerSelect;
    private String outerKey;
    private boolean withSemicolon = true;

    private Where where;

    SelectCommand(String table, List<String> selectKeys) {
        this.table = table;
        this.keys.addAll(selectKeys);
    }

    SelectCommand as(String key, String alias) {
        this.alias.put(key, alias);
        return this;
    }

    SelectCommand withoutSemicolon() {
        withSemicolon = false;
        return this;
    }

    SelectCommand join(String table, String column, List<String> selectKeys) {
        joins.add(new Join(table, column, selectKeys));
        return this;
    }

    SelectCommand groupBy(String column) {
        groupBy.add(column);
        return this;
    }

    SelectCommand having(Where having) {
        if (this.having == null) {
            this.having = having;
        } else {
            this.having.and(having);
        }
        return this;
    }

    SelectCommand orderBy(OrderBy orderBy) {
        orderByList.add(orderBy);
        return this;
    }

    SelectCommand inInnerSelect(SelectCommand innerSelect, String key) {
        this.innerSelect = innerSelect;
        this.outerKey = key;
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

            if (alias.containsKey(key)) {
                sb.append(String.format(" AS %s", alias.get(key)));
            }
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

        if (innerSelect != null) {
            Where where = Where.in(outerKey, String.format("(%s)", innerSelect.toSql()));
            if (this.where == null) {
                this.where = where;
            } else {
                this.where.and(where);
            }
        }

        if (where != null) {
            sb.append(String.format(" WHERE %s", where.toSql()));
        }

        first = true;
        for (String column : groupBy) {
            if (first) {
                sb.append(" GROUP BY ");
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(column);
        }

        if (having != null) {
            sb.append(String.format(" HAVING %s", having.toSql()));
        }

        first = true;
        for (OrderBy orderBy : orderByList) {
            if (first) {
                sb.append(" ORDER BY ");
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(orderBy.toSql());
        }

        if (limit != null) {
            sb.append(String.format(" LIMIT %d", limit));
        }

        if (skip != null) {
            sb.append(String.format(" OFFSET %d", skip));
        }

        if (withSemicolon) {
            sb.append(";");
        }

        return sb.toString();
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
