package network;

public class OTPCommand {
    public static final String ERR_ROOM_FULL = err("The room is full");
    public static final String ERR_COORD_INVALID = err("The coordinates are not valid");

    public static final String INFO_ROOM_CREATED = info("Room created");
    public static final String INFO_ROOM_JOINED = info("Room joined");

    public static final String COMMAND_AWAITING = command("AWAITING");
    public static final String COMMAND_READY = command("READY");
    public static final String COMMAND_START = command("START");
    public static final String COMMAND_END = command("END");
    public static final String COMMAND_WIN = command("WIN");
    public static final String COMMAND_LOOSE = command("LOOSE");

    public static String err(String err) {
        return "! " + err;
    }

    public static String info(String info) {
        return "# " + info;
    }

    public static String result(String result) {
        return "= " + result;
    }

    public static String command(String command) {
        return "$ " + command;
    }
}
