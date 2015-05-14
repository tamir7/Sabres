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

final class SabresDescriptor {
    private final Type type;
    private final Type ofType;
    private final String name;

    SabresDescriptor(Type type, Type ofType) {
        this(type, ofType, null);
    }

    SabresDescriptor(Type type) {
        this(type, null, null);
    }

    SabresDescriptor(Type type, Type ofType, String name) {
        this.type = type;
        this.ofType = ofType;
        this.name = name;
    }

    SabresDescriptor(Type type, String name) {
        this(type, null, name);
    }

    SqlType toSqlType() {
        return type.toSqlType();
    }

    Type getType() {
        return type;
    }

    Type getOfType() {
        return ofType;
    }

    String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (type.equals(Type.Pointer)) {
            return String.format("%s to %s", type.toString(), name);
        }

        if (type.equals(Type.List)) {
            if (ofType.equals(Type.Pointer)) {
                return String.format("list of %s to %s", type.toString(), name);
            }

            return String.format("List of %s", ofType.toString());
        }

        return type.toString();
    }

    @Override
    public int hashCode() {
        int hash = type.name().hashCode();
        if (ofType != null) {
            hash += ofType.name().hashCode();
        }

        if (name != null) {
            hash += name.hashCode();
        }

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SabresDescriptor))
            return false;
        if (obj == this)
            return true;

        SabresDescriptor other = (SabresDescriptor) obj;

        return type.equals(other.type) &&
                !(ofType != null && !(ofType.equals(other.ofType))) &&
                !(name != null && !(name.equals(other.name)));
    }

    enum Type {
        Integer("Integer") {
            @Override
            SqlType toSqlType() {
                return SqlType.Integer;
            }
        },
        Double("Double") {
            @Override
            SqlType toSqlType() {
                return SqlType.Real;
            }
        },
        Float("Float") {
            @Override
            SqlType toSqlType() {
                return SqlType.Real;
            }
        },
        String("String") {
            @Override
            SqlType toSqlType() {
                return SqlType.Text;
            }
        },
        Byte("Byte") {
            @Override
            SqlType toSqlType() {
                return SqlType.Integer;
            }
        },
        Short("Short") {
            @Override
            SqlType toSqlType() {
                return SqlType.Integer;
            }
        },
        Long("Long") {
            @Override
            SqlType toSqlType() {
                return SqlType.Integer;
            }
        },
        Boolean("Boolean") {
            @Override
            SqlType toSqlType() {
                return SqlType.Integer;
            }
        },
        Date("Date") {
            @Override
            SqlType toSqlType() {
                return SqlType.Integer;
            }
        },
        Pointer("Pointer") {
            @Override
            SqlType toSqlType() {
                return SqlType.Integer;
            }
        },
        List("List") {
            @Override
            SqlType toSqlType() {
                return SqlType.Text;
            }
        };

        private String text;

        Type(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        abstract SqlType toSqlType();
    }
}

