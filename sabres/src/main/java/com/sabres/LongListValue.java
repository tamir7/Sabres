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

final class LongListValue extends ListValue<Long> {

    LongListValue(List<Long> value) {
        super(value);
    }

    @Override
    void add(Object value) {
        if (value instanceof Long) {
            getValue().add((Long)value);
        } else {
            throwCastException();
        }
    }

    @Override
    void remove(Object value) {
        if (value instanceof Long) {
            getValue().remove(value);
        } else {
            throwCastException();
        }
    }

    @Override
    SabresDescriptor getDescriptor() {
        return new SabresDescriptor(SabresDescriptor.Type.List, SabresDescriptor.Type.Long);
    }
}
