package com.sabres;

final class DropTableCommand {
    private final String table;
    private boolean ifExists;

    DropTableCommand(String table) {
        this.table = table;
    }

    String toSql() {
        StringBuilder sb = new StringBuilder("DROP TABLE ");
        if (ifExists) {
            sb.append("IF EXISTS ");
        }
        sb.append(String.format("'%s'", this.table));

        return sb.toString();
    }

    DropTableCommand ifExists() {
        this.ifExists = true;
        return this;
    }
}
