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

final class AlterTableCommand {
    private final List<Column> columns = new ArrayList<>();
    private final String name;

    AlterTableCommand(String name) {
        this.name = name;
    }

    AlterTableCommand addColumn(Column column) {
        columns.add(column);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(String.format("AlTER TABLE %s ADD (", name));

        final int count = columns.size();
        int i = 0;
        while (count > i) {
            sb.append(columns.get(i).toString());

            if (++i == count) {
                sb.append(");");
            } else {
                sb.append(", ");
            }
        }

        return sb.toString();
    }
}
