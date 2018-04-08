import java.lang.NumberFormatException;

class Arguments {
    public String dst;
    public String nick;
    public String msg;
    public String file;
    public int port = 1337;

    static String basename = "asciiclient";

    private void printHelp() {
        System.out.println("Usage:");
        System.out.println(basename + " [-p <port>] {-m <message>|-f <file>} <nick> <destination>");
        System.out.println("");
        System.out.println("Arguments:");
        System.out.println("-p <port>      the port the client should connect to (optional, default: 1337)");
        System.out.println("-m <message>   the message to send, spaces might need to be quoted in the shell");
        System.out.println("-f <file>      a file to use as message, -m will be ignored");
        System.out.println("<nick>         the nick that should be displayed on the server");
        System.out.println("<destination>  the destination IPv6 address");
        System.out.println("");
        System.out.println("-m <message> or -f <file> must be given!");
    }

    Arguments(String[] argv) {
        if (argv.length == 0) {
            printHelp();
            System.exit(1);
        }
        //For_each would be nice, but we may have to skip/access next
        int i, j = 0;
        String[] fargs = new String[2];
        for (i = 0; i < argv.length; ++i) {
            String arg = argv[i];
            if (arg.equals("-?") || arg.equals("-h") || arg.equals("--help")) {
                printHelp();
                System.exit(1);

            } else if (arg.equals("-f") || arg.equals("--file")) {
                file = argv[++i];

            } else if (arg.equals("-m") || arg.equals("--message")) {
                msg = argv[++i];

            } else if (arg.equals("-p") || arg.equals("--port")) {
                try {
                    port = Integer.parseInt(argv[++i]);
                } catch (NumberFormatException e) {
                    System.err.println(argv[i - 1] + "is not a valid number");
                    System.exit(-1);
                }

            } else {
                if (j == fargs.length) {
                    System.out.println("Encountered an unexpected number of positional arguments");
                    System.exit(1);
                }
                fargs[j++] = arg;

            }
        }
        if (fargs[0] == null) {
            System.out.println("Did not find positional argument: nick");
            System.exit(1);
        }
        nick = fargs[0];

        if (fargs[1] == null) {
            System.out.println("Did not find positional argument: destination");
            System.exit(1);
        }

        dst = fargs[1];

        if (msg == null && file == null) {
            System.out.println("-m <message> or -f <file> is required");
            System.exit(1);
        }
    }
}
