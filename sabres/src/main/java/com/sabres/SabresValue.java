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
import java.util.List;

abstract class SabresValue<T> {
    private final T value;

    SabresValue(T value) {
        this.value = value;
    }

    abstract String toSql();

    @Override
    public abstract String toString();
    abstract SabresDescriptor getDescriptor();

    T getValue() {
        return value;
    }

    static SabresValue create(SabresObject o) {
        return new ObjectValue<>(o);
    }

    static SabresValue create(List<?> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Cannot create SabresValue from an empty List");
        }

        Object o = list.get(0);

        if (o instanceof Integer) {
            return new IntListValue(Utils.copyList(list, Integer[].class));
        }

        if (o instanceof Byte) {
            return new ByteListValue(Utils.copyList(list, Byte[].class));
        }

        if (o instanceof Short) {
            return new ShortListValue(Utils.copyList(list, Short[].class));
        }

        if (o instanceof Long) {
            return new LongListValue(Utils.copyList(list, Long[].class));
        }

        if (o instanceof Float) {
            return new FloatListValue(Utils.copyList(list, Float[].class));
        }

        if (o instanceof Double) {
            return new DoubleListValue(Utils.copyList(list, Double[].class));
        }

        if (o instanceof Boolean) {
            return new BooleanListValue(Utils.copyList(list, Boolean[].class));
        }

        if (o instanceof Date) {
            return new DateListValue(Utils.copyList(list, Date[].class));
        }

        if (o instanceof String) {
            return new StringListValue(Utils.copyList(list, String[].class));
        }

        if (o instanceof SabresObject) {
            return new ObjectListValue<>(Utils.copyList(list, SabresObject[].class));
        }

        throw new IllegalArgumentException("Cannot create SabresListValue out of class " +
                o.getClass().getSimpleName());

    }

    static SabresValue create(Object o) {
        if (o instanceof Integer) {
            return new IntValue((Integer)o);
        }

        if (o instanceof Byte) {
            return new ByteValue((Byte)o);
        }

        if (o instanceof Short) {
            return new ShortValue((Short)o);
        }

        if (o instanceof Long) {
            return new LongValue((Long)o);
        }

        if (o instanceof Float) {
            return new FloatValue((Float)o);
        }

        if (o instanceof Double) {
            return new DoubleValue((Double)o);
        }

        if (o instanceof Boolean) {
            return new BooleanValue((Boolean)o);
        }

        if (o instanceof Date) {
            return new DateValue((Date)o);
        }

        if (o instanceof String) {
            return new StringValue((String)o);
        }

        if (o instanceof SabresObject) {
            return new ObjectValue<>((SabresObject)o);
        }

        if (o instanceof List) {
            return create((List<?>)o);
        }

        throw new IllegalArgumentException("Cannot create SabresValue out of class " +
                o.getClass().getSimpleName());
    }
}
