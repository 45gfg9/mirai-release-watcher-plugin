package net.im45.bot.watcher.constants;

public final class Status {
    public static final String OK = "OK";
    public static final String INVALID_TOKEN = "Invalid token format";
    public static final String UNAUTHORIZED = "HTTP 401 Unauthorized";

    private Status() {
        throw new UnsupportedOperationException();
    }
}
