class Arguments {
	public String dst;
	public String iface = "eth0";
	public int timeout = 5;

	private void printHelp() {
		System.out.println("Wake-on-Lan (GRNVS 2016)");
		System.out.println("Usage:");
		System.out.println("-i/--interface: The interface on which the frame should be sent");
		System.out.println("DST:            The address to use for the WoL frame");
		System.out.println("-?/--help	    Print this help message");
	}

	Arguments(String[] argv) {
		if(argv.length == 0) {
			printHelp();
			System.exit(0);
		}
		//For_each would be nice, but we may have to skip/access next
		int i, j = 0;
		String[] fargs = new String[1];
		for(i = 0; i < argv.length; ++i) {
			String arg = argv[i];
			switch(arg) {
				case "-?":
				case "--help":
					printHelp();
					System.exit(0);
					break;
				case "-i":
				case "--interface":
					iface = argv[++i];
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
			System.out.println("Did not find positional argument: destination");
			System.exit(1);
		}
		dst = fargs[0];
	}
}
