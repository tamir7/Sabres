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

final class CreateIndexCommand {
    private static final String INDEX_NAME_PREFIX = "index_on_";
    private final String name;
    private final List<String> keys;
    private boolean ifNotExists = false;

    CreateIndexCommand(String name, List<String> keys) {
        this.name = name;
        this.keys = keys;
    }

    CreateIndexCommand ifNotExists() {
        ifNotExists = true;
        return this;
    }

    @Override
    public String toString() {
        return toSql();
    }

    String toSql() {
        final StringBuilder sb = new StringBuilder("CREATE ");

        sb.append("INDEX ");

        if (ifNotExists) {
            sb.append("IF NOT EXISTS ");
        }

        final StringBuilder columns = new StringBuilder();
        final StringBuilder indexName = new StringBuilder(INDEX_NAME_PREFIX);

        boolean first = true;

        for (String key : keys) {
            if (!first) {
                indexName.append("_");
                columns.append(", ");
            } else {
                first = false;
            }

            indexName.append(key);
            columns.append(key);
        }

        sb.append(String.format("%s ON %s(%s);", indexName.toString(), name, columns.toString()));

        return sb.toString();
    }
}
