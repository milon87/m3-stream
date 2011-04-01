
public class Frame {
	private final int type;
	private final int length;
	private final byte[] data;
	
	
	public Frame(int type, int length, byte[] data) {
		this.type = type;
		this.length = length;
		this.data = data;
	}
	
	public int getType() {
		return type;
	}

	public int getLength() {
		return length;
	}

	public byte[] getData() {
		return data;
	}
		
}
