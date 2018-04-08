public class GRNVS_RAW {
	static {
		System.loadLibrary("GRNVS");
	}

	private final int handle;

	private final native int getSocket(String dev, int level);
	private final native int write_(int handle, byte[] buffer, int size);
	private final native int read_(int handle, byte[] buffer, Timeout time);
	private final native void hexdump_(byte[] buffer, int size);
	private final native int close_(int handle);
	private final native byte[] mac_(int handle);
	private final native byte[] ip_(int handle);

	private static final int SOCK_RAW = 3;
	private static final int SOCK_DGRAM = 2;

	public GRNVS_RAW(String dev, int level) {
		handle = getSocket(dev, level);
	}

	public final int write(byte[] buffer, int size) {
		return write_(handle, buffer, size);
	}

	public final int read(byte[] buffer, Timeout time) {
		return read_(handle, buffer, time);
	}

	public final byte[] getIP() {
		return ip_(handle);
	}

	public final byte[] getMac() {
		return mac_(handle);
	}

	public final int close() {
		return close_(handle);
	}

	public final void hexdump(byte[] buffer, int size) {
		hexdump_(buffer, size);
	}

	protected void finalize() {
		close_(handle);
	}
}
