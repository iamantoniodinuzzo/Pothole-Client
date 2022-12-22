package com.indisparte.pothole.util;

import androidx.annotation.NonNull;

/**
 * Helps the user create a well-formatted server command.
 *
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class ServerCommand {
    /**
     * The set of commands accepted by the server
     */
    public enum CommandType {
        SET_USERNAME("u"), // usage: u [username]
        NEW_HOLE("h"),//usage: h [lat;lng;variation]
        HOLE_LIST_BY_RANGE("r"),//usage: r [lat;lng;range]
        THRESHOLD("t"),
        EXIT("e");

        public final String value;

        CommandType(final String command) {
            this.value = command;
        }
    }

    private final CommandType type;
    private final String[] query;

    /**
     * Create a server command
     *
     * @param type  The type of the accepted command, {@link CommandType}, can't be null
     * @param query The query attached to the command, can be null or empty
     */
    public ServerCommand(@NonNull CommandType type, String... query) {
        this.type = type;
        this.query = query;
    }

    public CommandType getType() {
        return type;
    }

    public String[] getQuery() {
        return query;
    }

    /**
     * @return The command formatted as a request to the server
     * For example to set the username : u[username].
     * For a list of parameters, on the other hand, each value is separated by a semicolon.
     */
    public String getFormattedRequest() {
        String result = type.value;
        if (query != null) {
            if (query.length > 0) {
                if (query.length == 1) {
                    result += "[" + query[0] + "]";
                } else {
                    result += "[" + String.join(";", query) + "]";
                }
            }
        }
        return result;
    }
}
