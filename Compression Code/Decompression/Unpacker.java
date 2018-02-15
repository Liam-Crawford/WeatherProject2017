import java.util.*;
//TODO document this class
public class Unpacker {
	int[] updateBits = {12, 14, 11, 20, 21, 22};
	int[] recordBits = {11, 13, 10, 23, 24, 25};
	int[] minValues = {-40, 300, 0, 0, 0, 0}; 
	//TODO change map name
	HashMap<Integer, int[]> m = new HashMap<Integer, int[]>();
	int[] offsetAnd = {1, 3, 7, 15, 31, 63, 127};// subtract one to access
	
	boolean isUpdate = false;
	
	//TODO change data manager name
	DataManager d;
	Record prevRecord;
	
	Unpacker(DataManager d) {
		populateMap();
		this.d = d;
	}
	
	//TODO tidy away system.outs and other unreqd code
	private void populateMap() {
		
		for(Integer i = 0; i < 256; i++) {
			int[] numberList = new int[6];
			if(i == 2) {
				for(int j = 0; j < numberList.length; j++) {
					numberList[j] = 0;
					m.put(i, numberList);					
				}
			} else if(i % 2 == 1 || i == 254 || i == 0) {
				numberList[0] = -1;
				m.put(i, numberList);
			}else if(i % 4 == 0) {
				Integer headerKey = i;
			
				if(headerKey == 0) {
					numberList[0] = 0;
					m.put(i, numberList);
				}
				while(headerKey != 0){
					if(headerKey >= 128) {
						numberList[0] = recordBits[0];
						headerKey -= 128;
					}
					if(headerKey >= 64) {
						numberList[1] = recordBits[1];
						headerKey -= 64;
					}
					if(headerKey >= 32) {
						numberList[2] = recordBits[2];
						headerKey -= 32;
					}
					if(headerKey >= 16) {
						numberList[3] = recordBits[3];
						headerKey -= 16;
					}
					if(headerKey >= 8) {
						numberList[4] = recordBits[4];
						headerKey -= 8;
					}
					if(headerKey >= 4) {
						numberList[5] = recordBits[5];
						headerKey -= 4;
					}
				}
				m.put(i, numberList);
			}else{
				int headerKey = i - 2;
				if(headerKey == 0) {
					numberList[0] = 0;
					m.put(i, numberList);
				}
				while(headerKey != 0){
					if(headerKey >= 128) {
						numberList[0] = updateBits[0];
						headerKey -= 128;
					}
					if(headerKey >= 64) {
						numberList[1] = updateBits[1];
						headerKey -= 64;
					}
					if(headerKey >= 32) {
						numberList[2] = updateBits[2];
						headerKey -= 32;
					}
					if(headerKey >= 16) {
						numberList[3] = updateBits[3];
						headerKey -= 16;
					}
					if(headerKey >= 8) {
						numberList[4] = updateBits[4];
						headerKey -= 8;
					}
					if(headerKey >= 4) {
						numberList[5] = updateBits[5];
						headerKey -= 4;
					}
				}
				m.put(i, numberList);
			}
		}
	}
	
	void readHeader(byte[] data) {
		if(d.getOffset() == 0) {
			d.setBitGroup(m.get(d.getReceivedByteAtCounter()));
			if(d.getReceivedByteAtCounter() % 4 == 2)
				isUpdate = true;
			d.setDataCounter(d.getDataCounter() + 1);
			
		}else {
			int header = d.getReceivedByteAtCounter();
			
			header = header & offsetAnd[8 - d.getOffset() - 1];
			d.setDataCounter(d.getDataCounter() + 1);
			
			header = header << 8;
			
			int headerOverflow = d.getReceivedByteAtCounter();
			
			headerOverflow = headerOverflow & ~offsetAnd[8 - d.getOffset()];
			header = header | headerOverflow;
			header = header >> 8 - d.getOffset();
			if(header % 4 == 2)
				isUpdate = true;
			d.setBitGroup(m.get(header));
		}
	}
	
	Record decode(byte[] data) { 
		int dataAccessIndex = 1;

		Record record = new Record();
		
		for(int i = 0; i < d.getBitGroup().length; i++) {
				
			int dataPointBits = d.getBitGroup()[i];
				
			if(dataPointBits > 0) {
				if(d.getOffset() > 0) {
						
					int unpackedValue = 0;
					int readByte = d.getRecordByte(dataAccessIndex);
					dataAccessIndex++;
						
					unpackedValue = readByte & offsetAnd[8 - d.getOffset() - 1];
					unpackedValue = unpackedValue << 8;						
					dataPointBits -= 8 - d.getOffset();
					
					int overflow = dataPointBits % 8;
						
						if (overflow == 0) {
							while(dataPointBits > 0) {
								unpackedValue = unpackedValue | d.getRecordByte(dataAccessIndex);
								dataAccessIndex++;
								dataPointBits -= 8;
							}
						}else {
							while(dataPointBits > 8) {
								unpackedValue = unpackedValue | d.getRecordByte(dataAccessIndex);
								unpackedValue = unpackedValue << 8 ;
								dataAccessIndex++;
								dataPointBits -= 8;
							}
							int num =  d.getRecordByte(dataAccessIndex);
							unpackedValue = unpackedValue | num & ~offsetAnd[(8 - overflow) - 1];
							unpackedValue = unpackedValue >> 8 - overflow;
						}
						System.out.println(unpackedValue + "oValue");
						if(isUpdate) {
							System.out.println(descaleUpdate(unpackedValue, d.getBitGroup()[i]));
							record.populateRecord(prevRecord.orderedAccess(i) + descaleUpdate(unpackedValue, d.getBitGroup()[i]), i);
						}else
							record.populateRecord(descale(unpackedValue, minValues[i]), i);
						d.setOffset(overflow);
					}else {
						int overflow = dataPointBits % 8;
						int unpackedValue = 0;
						
						while(dataPointBits > overflow) {
							unpackedValue = unpackedValue << 8;
							unpackedValue =  d.getRecordByte(dataAccessIndex);	
							dataAccessIndex++;
							dataPointBits -= 8;
						}
						if(overflow > 0) {
							unpackedValue = unpackedValue << overflow;
							int overflowByte =  d.getRecordByte(dataAccessIndex);
							overflowByte = overflowByte & ~offsetAnd[(8 - overflow) - 1];
							overflowByte = overflowByte >> 8 - overflow;
							unpackedValue = unpackedValue | overflowByte;
						}
						System.out.println(unpackedValue + "value");
						if(isUpdate) {
							record.populateRecord(prevRecord.orderedAccess(i) + descaleUpdate(unpackedValue, d.getBitGroup()[i]), i);
						}else
							record.populateRecord(descale(unpackedValue, minValues[i]), i);
						d.setOffset(overflow);
						
					}
				}
			}
			d.setDataCounter(d.getDataCounter() + dataAccessIndex -1);
			if(!isUpdate)
				prevRecord = record;
			return record;  
	}
	
	double descale(int scaledValue, int minNum) {
		double realValue = (scaledValue / 10.0) + minNum;
		return realValue;
	}
	
	double descaleUpdate(int value, int bits) {
		int orBits = -(int)Math.pow(2, bits);
		System.out.println((value | orBits) + "here");
		return (value | orBits) / 10.0;
	}
}

