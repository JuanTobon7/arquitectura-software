package com.pipefilters.commands;

public enum CommandsList {

    IMAGE(1, "image"),
    BASE64(2, "base64"),
    BINARY(3, "binary"),
    SECURITY(4, "security"),
    SAVE(5, "save");

    private final int order;
    private final String command;

    CommandsList(int order, String command) {
        this.order = order;
        this.command = command;
    }

    public int getOrder() {
        return order;
    }

    public String getCommand() {
        return command;
    }

    public static CommandsList fromCommand(String command) {

        for (CommandsList value : values()) {

            if (value.command.equalsIgnoreCase(command)) {
                return value;
            }
        }

        throw new IllegalArgumentException(
                "Unknown command: " + command
        );
    }
}