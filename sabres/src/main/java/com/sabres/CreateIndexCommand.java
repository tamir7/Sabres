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

final class CreateIndexCommand {
    private static final String INDEX_NAME_PREFIX = "index_on_";
    private final String name;
    private final String key;

    private boolean ifNotExists = false;

    CreateIndexCommand(String name, String key) {
        this.name = name;
        this.key = key;
    }

    CreateIndexCommand ifNotExists() {
        ifNotExists = true;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CREATE ");

        sb.append("INDEX ");

        if (ifNotExists) {
            sb.append("IF NOT EXISTS ");
        }

        sb.append(String.format("%s%s ON %s(%s);", INDEX_NAME_PREFIX, key, name, key));
        return sb.toString();
    }
}
