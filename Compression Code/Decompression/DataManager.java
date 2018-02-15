import java.util.Arrays;

public class DataManager {
	//The number of bits a previous value has overflowed into a byte occupiedby the next value
	private int offset;
	
	//The number of bytes that have currently be read through, used primarily for tracking header position
	private int dataCounter;
	
	//The number of bytes a record occupies used primarily to geet the sub array of bytes needed
	private int recordBits;
	
	//An array containg numbers of bits as determined by the header
	private int[] bitGroup;
	
	//An array containing all data read from BLE
	byte[] receivedData;
	
	//An array containing a subset of the data that makes up a record
	byte[] recordData;
	
	public DataManager(byte[] bytes){
		receivedData = bytes;
	}
	
	//--Setters and getters for numbers and counters--//
	public int getRecordBits() {
		return recordBits;
	}

	public void setRecordBits(int recordBits) {
		this.recordBits = recordBits;
	}
	
	public int getDataCounter() {
		return dataCounter;
	}

	public void setDataCounter(int dataCounter) {
		this.dataCounter = dataCounter;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		//If an offset is a whole byte just read the next byte
		if(offset == 8)
			this.offset = 0;
		else
			this.offset = offset;
	}
	
	//--setters and getters of array of all read data--//
	public byte[] getSpecificData(int startIndex, int endIndex) {
		/* Function for future proofing/testing.  Largely to wrap the copyOfRange funtion with a
		 * +1 to end index to force it to be inclusive for convenience (normally end index is exclusive)
		 */ 
		return Arrays.copyOfRange(receivedData, startIndex, endIndex+1);
	}
	
	/* Returns a byte from all read data at the specific index.  Because Java bytes are signed, this needs to be anded 
	 * in order to ensure that the right data is output.  An unsigned byte of 255 (c++) is -1 when signed (java) 
	 * meaning that normal casting to an int would result in -1)
	 */
	public int getReceivedByte(int index) {
		return receivedData[index] & 255;
	}
	
	/* Returns a byte from all read data using the current data counter as an index, see above for an
	 * explanation of the necessity of the & 255.
	 */
	public int getReceivedByteAtCounter() {
		return receivedData[dataCounter] & 255;
	}
	
	public void setDataReceived(byte[] data) {
		this.receivedData = data;
	}
	
	public byte[] getDataReceived() {
		 return receivedData;
	}
	
	//--setters and getters for data that makes up an individual record--//
	public void setRecordData(int startIndex, int endIndex) {
		recordData = Arrays.copyOfRange(receivedData, startIndex, endIndex+1);
	}
	
	public byte[] getRecordData() {
		return recordData;
	}
	
	/* Gets a byte of data from the record array using the given index. See getReceivedByte() 
	 */
	public int getRecordByte(int index) {
		return recordData[index] & 255;
	}
	
	//methods that manage the array of bits that determines which sensor data is being unpacked// 	
	public int[] getBitGroup() {
		return bitGroup;
	}
	
	/* Sets the bit group and iterates through it to also calculate the record bits.
	 */
	public void setBitGroup(int[] bitGroup) {
		int counter = 0;
		for(int bits : bitGroup) {
			counter += bits;
		}
			
		recordBits = (counter + 8) / 8;
		this.bitGroup = bitGroup;
	}

	
	
	
}
