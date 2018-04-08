/**
 * Assignment 4
 * @author Svilen Stefanov
 */
/* DO NOT!!! put a package statement here, that would break the build system */

/* Since this file is mildly incompatible with IDEs and java without an IDE
 * can be rather annoying here are some useful imports
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Assignment4 {
	private static ArrayList<String> routingTable = new ArrayList<>();

	/* Since we don't have GRNVS_RAW this time, here is a simple hexdump */
	private static void printArray(byte[] array) {
		int i = 0;
		for (byte b : array) {
			System.err.format("%02x ", b);
			if (++i == 8) {
				System.err.format("\n");
				i = 0;
			}
		}
		System.err.format("\n");
	}

	/* Our version of parseIP */
	private static byte[] parseIP(String ipAddr) {
		try{
			return InetAddress.getByName(ipAddr).getAddress();
		}
		catch (UnknownHostException e) {
			return null;
		}
	}

	/**
	 * Feel free to use the java lib for data structure and conversion.
	 * The provided imports and parseIP should be a hint what we think you
	 * should use.
	 * Higher level functions should NOT be used for decision making.
	 * For example:
	 * Don't use: java.net.InetAddress.isLinkLocalAddress()
	 * Do use:    Arrays.equals()
	 */


	/**
	 * This is your entry point
	 * You should read from `table' until eof is reached (getLine returns
	 * null), then start serving the routing requests from `requests'.
	 *
	 * NOTICE: You will have to print each answer to a request before you
	 * read the next one. The tester blocks until it gets an answer and
	 * doesn't give the next request.
	 *
	 * OUTPUT: The output has to be in the form "$hop $interface\n".
	 * Since System.out.println() does buffering and thus breaks the
	 * assumption that your string will be printed before you block in
	 * readLine() use System.out.format().
	 * System.out.format("%s\n", str) is equivalent to System.out.println(str)
	 * For more information about the format have a look at:
	 * http://www.cplusplus.com/reference/cstdio/printf/
	 * This is a C(++) reference but javas `format' is compatible.
	 * Example output: System.out.format("%s\n", "::1 eth0");
	 */
	public static void run(FileReader table, FileReader requests, String local) {
		String route = new String();
		Scanner in = new Scanner(route);
		
		String prefix = new String(), comparablePrefix = new String();
		Entry entry = new Entry();
		boolean isDouble = false;
		
		while((route=table.getLine())!=null){
			isDouble = false;
			prefix = route.substring(route.indexOf("/") + 1, route.length());
			in = new Scanner(prefix);
			prefix = in.next();
			
			int i;
			for (i = 0; i < routingTable.size(); i++) {
				comparablePrefix = routingTable.get(i);
				comparablePrefix = comparablePrefix.substring(comparablePrefix.indexOf("/") + 1, comparablePrefix.length());
				in = new Scanner(comparablePrefix);
				comparablePrefix = in.next();
				
				if(Integer.parseInt(prefix) - Integer.parseInt(comparablePrefix) > 0)
					break;
				else if (Integer.parseInt(prefix) - Integer.parseInt(comparablePrefix) == 0){
					if(route.substring(0, route.indexOf("/")).compareTo(routingTable.get(i).substring(0, routingTable.get(i).indexOf("/"))) < 0){
						break;
					} 
					if(route.substring(0, route.indexOf("/")).equals(routingTable.get(i).substring(0, routingTable.get(i).indexOf("/")))){
						isDouble = true;
						break;
					}
				} else continue;
			}
			if(!isDouble)
				routingTable.add(i, route);	
		}
		
		while((route=requests.getLine())!=null){
			byte[] ip = parseIP(route);
			if(route.equals(local))
				System.out.format("%s\n", "It's a me!");		
			else if(((ip[0] & (byte)0xFF) == (byte)0xFE) && ((ip[1] & 0xC0) == 0x80))		
				System.out.format("%s\n", "Link local address");
			else {
				route = entry.check(routingTable, route);
				System.out.format("%s\n", route);
			}
		}
		in.close();
		return;
	}


	public static void main(String[] argv) {
		Arguments args = new Arguments(argv);
		try{
			try {
				FileReader table = new FileReader(args.table);
				FileReader reqs  = new FileReader(args.requests);
				run(table, reqs, args.local);
			} catch (java.io.FileNotFoundException e) {
				System.err.println("Could not find one of the files :(");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
