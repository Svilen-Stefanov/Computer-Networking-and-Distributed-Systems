import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReader {
	BufferedReader br;

	/**
	 * Create a FileReader for file at path file
	 * If file == "-" open stdin as file
	 *
	 * @file path to the file to read from
	 */
	public FileReader(String file) throws FileNotFoundException {

		if ("-".equals(file))
			br = new BufferedReader(new InputStreamReader(System.in));
		else
			br = new BufferedReader(new java.io.FileReader(file));
	}

	/**
	 * Get the next line from the FileReader.
	 * Returns null when eof.
	 */
	public String getLine() {
		try {
			return br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}
}
