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

import java.util.Collection;
import java.util.Date;

final class ObjectDescriptor {
    private final Type type;
    private final Type ofType;
    private final String name;

    ObjectDescriptor(Type type, Type ofType) {
        this(type, ofType, null);
    }

    ObjectDescriptor(Type type) {
        this(type, null, null);
    }

    ObjectDescriptor(Type type, Type ofType, String name) {
        this.type = type;
        this.ofType = ofType;
        this.name = name;
    }

    ObjectDescriptor(Type type, String name) {
        this(type, null, name);
    }

    private static Type getTypeFromObject(Object o) {
        if (o instanceof String) {
            return Type.String;
        } else if (o instanceof Integer) {
            return Type.Integer;
        } else if (o instanceof Date) {
            return Type.Date;
        } else if (o instanceof Boolean) {
            return Type.Boolean;
        } else if (o instanceof Long) {
            return Type.Long;
        } else if (o instanceof Short) {
            return Type.Short;
        } else if (o instanceof Byte) {
            return Type.Byte;
        } else if (o instanceof Float) {
            return Type.Float;
        } else if (o instanceof Double){
            return Type.Double;
        } else if (o instanceof SabresObject) {
            return Type.Pointer;
        } else if (o instanceof Collection) {
            return Type.Collection;
        } else {
            throw new IllegalArgumentException(String.format("Class %s is not supported",
                    o.getClass().getSimpleName()));
        }
    }

    static ObjectDescriptor fromObject(Object o) {
        Type type = getTypeFromObject(o);
        switch (type) {
            case Integer:
            case Double:
            case Float:
            case String:
            case Byte:
            case Short:
            case Long:
            case Boolean:
            case Date:
                return new ObjectDescriptor(type);
            case Pointer:
                return new ObjectDescriptor(type, o.getClass().getSimpleName());
            case Collection:
                Object internalObject = ((Collection)o).iterator().next();
                Type ofType = getTypeFromObject(internalObject);
                switch (ofType) {
                    case Integer:
                    case Double:
                    case Float:
                    case String:
                    case Byte:
                    case Short:
                    case Long:
                    case Boolean:
                    case Date:
                        return new ObjectDescriptor(type, ofType);
                    case Pointer:
                        return new ObjectDescriptor(type, ofType,
                                internalObject.getClass().getSimpleName());
                    case Collection:
                        throw new IllegalArgumentException(
                                String.format("List of type %s is not supported",
                                        internalObject.getClass().getSimpleName()));
                }
        }

        throw new IllegalArgumentException(String.format("Class %s is not supported",
                o.getClass().getSimpleName()));
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

        return type.toString();
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
        Collection("Collection") {
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

