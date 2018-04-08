import java.lang.NumberFormatException;

class Arguments {
	public String table;
	public String requests = "-";
	public String local;

	private void printHelp() {
		System.out.println("This is the fourth exercise for GRNVS 2016");
		System.out.println("Usage: router [-r REQUESTS] TABLE OWNADDR");
		System.out.println("-r/--requests: A file containing the routing requests. One per line");
		System.out.println("TABLE:         Path to the file containing the routing table");
		System.out.println("OWNADDR:       Own IP address of the router");
		System.out.println("-?/--help	   Print this help message");
	}

	Arguments(String[] argv) {
		if(argv.length == 0) {
			printHelp();
			System.exit(0);
		}
		//For_each would be nice, but we may have to skip/access next
		int i, j = 0;
		String[] fargs = new String[2];
		for(i = 0; i < argv.length; ++i) {
			String arg = argv[i];
			switch(arg) {
				case "-?":
				case "--help":
					printHelp();
					System.exit(0);
					break;
				case "-r":
				case "--requests":
					requests = argv[++i];
					break;
				default:
					if(j == fargs.length) {
						System.out.println("Encountered an unexpected number of positional arguments");
						System.exit(1);
					}
					fargs[j++] = arg;
					break;
			}
		}
		if(fargs[0] == null) {
			System.out.println("Did not find positional argument: TABLE");
			System.exit(1);
		}

		if(fargs[1] == null) {
			System.out.println("Did not find positional argument: OWNADDR");
			System.exit(1);
		}

		table = fargs[0];
		local = fargs[1];
	}
}
