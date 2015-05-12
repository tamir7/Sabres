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

import java.util.Date;

final class SabresValue {
    private final Object value;
    private final SabresDescriptor descriptor;

    SabresValue(Object value, SabresDescriptor descriptor) {
        this.value = value;
        this.descriptor = descriptor;
    }

    <T> T get(Class<T> clazz) {
        if (value.getClass().isAssignableFrom(clazz)) {
            return clazz.cast(value);
        }

        return null;
    }

    String toSql() {
        switch (descriptor.getType()) {
            case Date:
                return String.valueOf(((Date)value).getTime());

            case Boolean:
                return ((Boolean)value) ? "1" : "0";
        }

        return value.toString();
    }

    @Override
    public String toString () {
        return value.toString();
    }
}
