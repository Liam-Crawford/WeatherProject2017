//============================================================================
// Name        : DataPacking.cpp
// Author      : Jesse Bricknell
// Version     :
// Copyright   : Your copyright notice
// Description : .cpp for DataPacking Class
//============================================================================

#include "DataPacking.h"
#include <iostream>
#include <math.h>

DataPacking::DataPacking(){};

//TODO insert checking to ensure value exceed acceptable range
/*
 * Scales the provided number using the formula as dictated in design documentation.
 */
int DataPacking::scaleNumber(double number, double lowestPoint){
	if(number < lowestPoint){
		return -1;
	}else{
		int value = (number - lowestPoint) * 10;
		return value;
	}
}

/*
 * determines the offset (the amount of bits a number 'spills over' into the next byte)
 * e.g. 1100 1011 11 is an offset of 2
 */
int DataPacking::offsetValue(int offset, int overflow){
		if(offset + overflow == 8)
			return 0;
		else if(offset < overflow)
				return overflow - offset;
			else
				return offset + overflow;
}

/*
 * sets the shifting instructions using the offset, stores in the appropriate struct
 */
void DataPacking::setShiftInstructions(DataPacking::shiftingInstructions &instr, int offset, int waste){
	//True = shift right
	//False = shift left
	int toShift = waste - offset;

		if(toShift >= 0){
			instr.shiftAmount = toShift;
			instr.shiftType = false;
		}else{
			instr.shiftAmount = -toShift;
			instr.shiftType = true;
		}
}

//Sets the values of wasteShift and nibbleShift in the given shiftVals struct based on the values passed
void DataPacking::setShiftValues(DataPacking::shiftVals &shiftValues, int bits){
	/*
	 * Sets the counterValue by calculating the amount of bytes occupied and converting that number to bits
	 */
	shiftValues.counterValue = (ceil(bits / 8.0) * 8);

	/*
	 * Sets the wasteShift value by taking the amount of nibbles currently used in bits
	 * and subtracting the amount of useful bits it needs.
	 */
	shiftValues.wasteShift = shiftValues.counterValue - bits;

	shiftValues.overflow = bits % 8;
}

//store data with no offsetting
int DataPacking::storeData(int remainingBits, int temporaryValue, uint8_t recordArray[], int index){
	/*
	 * shift bits so they they fit into 1 byte and store then do the same with the next 8
	 */
	while(remainingBits > 4){
		recordArray[index] = temporaryValue >> (remainingBits - 8);
		index++;
		remainingBits -= 8;
	}
	/*
	 * if there are bits left over after storing, set them into an overflow byte
	 */
	if(remainingBits > 0){
		recordArray[index] = temporaryValue << remainingBits;
		index++;
	}
	return index;
}

//store remainder data after offsetting right
int DataPacking::storeRemainder(int remainingBits, uint8_t remainder, uint8_t recordArray[], int index){
	recordArray[index] = remainder;
	index++;
	return index;
}

//store data with offsetting or offsetting == waste
int DataPacking::storeOffsetData(int remainingBits, int temporaryValue, uint8_t recordArray[], int index){
	/*
	 * store offset data in overflowed bit
	 */
	index--;
	recordArray[index] = recordArray[index] | temporaryValue >> (remainingBits - 8);
	remainingBits -= 8;
	index++;

	/*
	 * shift bits so they they fit into 1 byte and store then do the same with the next 8
	 * TODO consider calling storeData after the offset has been managed
	 */
	while(remainingBits > 4){
		recordArray[index] = temporaryValue >> (remainingBits - 8);
		index++;
		remainingBits -= 8;
	}
	/*
	 * if there are bits left over after storing, set them into an overflow bit
	 */
	if(remainingBits > 0){
		recordArray[index] = temporaryValue << remainingBits;
		index++;
	}

	return index;
}

/*
 * builds and set the header representing the contents of each record
 */
int DataPacking::buildHeader(bool temperature, bool pressure, bool humidity, bool isUpdate, int &index, uint8_t recordArray[], int offset){
	uint8_t header = 0;

	//Sets a 1 in the appropriate place as outlined in the design doc
	if(temperature)
		header = header | 128;
	if(pressure)
		header = header | 64;
	if(humidity)
		header = header | 32;
	if(isUpdate)
		header = header | 2;

	//manages a header if there is an offset from a previous record
	if(offset > 0){
		uint8_t offsetHeader = header >> offset;
		index--;
		recordArray[index] = recordArray[index] | header;
		index++;
		offsetHeader = header << (8 - offset);
		recordArray[index] = offsetHeader;
		index++;
		offset = 8 - (8 - offset);
	}else{
		recordArray[index] = header;
		index++;
		offset = 0;
	}
	return offset;
}
/*
 * Function that takes the value to be stored, shift it based on the amount of offset and then calls the appropriate function
 * from above based on the offset.  It updates the index as it goes.
 */
int DataPacking::storeValue(int value, int offset, DataPacking::shiftVals shiftValues, DataPacking::shiftingInstructions instr, int arrayIndex, uint8_t recordArray[]){
	int temporaryValue;

	//If there is an offset, but the offset is complementary (i.e. the numbers fit together without having to be moved)
	if(offset > 0 && offset == shiftValues.wasteShift){
		arrayIndex = storeOffsetData(shiftValues.counterValue, value, recordArray, arrayIndex);
	}else if(offset > 0){
		//an offset is found requiring the number to be shifted left
		if(instr.shiftType == false){
			temporaryValue = value << instr.shiftAmount;
			arrayIndex = storeOffsetData(shiftValues.counterValue, temporaryValue, recordArray, arrayIndex);
		}else{
			//an offset is found requiring the number to be shifted right, has a byte for saving the bits that are lost post-shifting
			temporaryValue = value >> instr.shiftAmount;
			uint8_t remainder = value << (8 - instr.shiftAmount);
			arrayIndex = storeOffsetData(shiftValues.counterValue, temporaryValue, recordArray, arrayIndex);
			arrayIndex = storeRemainder(shiftValues.counterValue, remainder, recordArray, arrayIndex);
		}
		//no offset is found simply left side align the bits and store them
	}else{
		temporaryValue = value << shiftValues.wasteShift;
		arrayIndex = storeData(shiftValues.counterValue, temporaryValue, recordArray, arrayIndex);
	}

	return arrayIndex;
}

//Function to store a record update as opposed to a full record
DataPacking::arrayManagement DataPacking::storeUpdate(double temperature, double pressure, double humid, uint8_t recordArray[], DataPacking::arrayManagement offsetAndIndex, DataPacking::fullRecord &lastFullRecord){
	//Structs containing shifting information: direction, amount, offset, overflow etc
	DataPacking::shiftVals shiftValues;
	DataPacking::shiftingInstructions instr;

	//Differences between last full record taken and current reading, this generates the actual difference
	int tempDif = scaleNumber(temperature, DataPacking::MINTMP) - lastFullRecord.fullTmp;
	int hpaDif = scaleNumber(pressure, DataPacking::MINTMP) - lastFullRecord.fullHPA;
	int humidDif = scaleNumber(humid, DataPacking::MINTMP) - lastFullRecord.fullHmd;

	int bits;
	int offset = offsetAndIndex.offset;
	int arrayIndex = offsetAndIndex.offset;

	//build the reader returning any offset
	offset = buildHeader(tempDif != 0, hpaDif != 0, humidDif != 0, true, arrayIndex, recordArray, offset);

	//Might change this later to use negative number as error codes, some issues with this to work out
	if(tempDif != 0){
		//Set the amount of bits the value will take and the shifting details
		bits = DataPacking::TMPBITSUPD;
		setShiftValues(shiftValues, bits);
		setShiftInstructions(instr, offset, shiftValues.wasteShift);

		//If the difference is negative trim the number so that it fits into the appropriate amount of bits
		if(tempDif < 0)
			tempDif = tempDif & 0xFFF;

		//Store the difference and calculate the new offset
		arrayIndex = storeValue(tempDif, offset, shiftValues, instr, arrayIndex, recordArray);
		offset = offsetValue(offset, shiftValues.overflow);
	}

	//Same process as above but for pressure
	if(hpaDif != 0){
		bits = DataPacking::HPABITSUPD;
		setShiftValues(shiftValues, bits);
		setShiftInstructions(instr, offset, shiftValues.wasteShift);

		if(hpaDif < 0)
			hpaDif = hpaDif & 0x3FFF;

		arrayIndex = storeValue(hpaDif, offset, shiftValues, instr, arrayIndex, recordArray);
		offset = offsetValue(offset, shiftValues.overflow);

	}

	//Same process as above put for humidity
	if(humidDif != 0){
		bits = DataPacking::HMDBITSUPD;
		setShiftValues(shiftValues, bits);
		std::cout << "waste: " << shiftValues.wasteShift << std::endl;
		setShiftInstructions(instr, offset, shiftValues.wasteShift);

		if(humidDif < 0)
			humidDif = humidDif & 0x7FF;

		arrayIndex = storeValue(humidDif, offset, shiftValues, instr, arrayIndex, recordArray);
		offset = offsetValue(offset, shiftValues.overflow);
	}

	offsetAndIndex.offset = offset;
	offsetAndIndex.arrayIndex = arrayIndex;
	return offsetAndIndex;
}

DataPacking::arrayManagement DataPacking::storeRecord(double rawTempRead, double rawPresRead, double rawHumidRead, uint8_t recordArray[], DataPacking::arrayManagement offsetAndIndex, DataPacking::fullRecord &fullRecord){
	// Scale the numbers read from the sensors according to the design document
	int temperature = scaleNumber(rawTempRead, DataPacking::MINTMP);
	int pressure = scaleNumber(rawPresRead, DataPacking::MINHPA);
	int humidity = scaleNumber(rawHumidRead, DataPacking::MINHMD);

	//Structs containing shifting information: direction, amount, offset, overflow etc
	DataPacking::shiftVals shiftValues;
	DataPacking::shiftingInstructions instr;

	int bits;
	int offset = offsetAndIndex.offset;
	int arrayIndex = offsetAndIndex.offset;

	//Build header
	offset = buildHeader(temperature > -1, pressure > -1, humidity > -1, false, arrayIndex, recordArray, offset);

	//Store data
	//Might change this later to use negative number as error codes, some issues with this to work out
	if(temperature > -1){
		bits = DataPacking::TMPBITS;
		setShiftValues(shiftValues, bits);
		setShiftInstructions(instr, offset, shiftValues.wasteShift);

		arrayIndex = storeValue(temperature, offset, shiftValues, instr, arrayIndex, recordArray);
		offset = offsetValue(offset, shiftValues.overflow);
	}

	if(pressure > -1){
		bits =DataPacking:: HPABITS;
		setShiftValues(shiftValues, bits);
		setShiftInstructions(instr, offset, shiftValues.wasteShift);

		arrayIndex = storeValue(pressure, offset, shiftValues, instr, arrayIndex, recordArray);
		offset = offsetValue(offset, shiftValues.overflow);
	}

	if(humidity > -1){
		bits =DataPacking:: HMDBITS;
		setShiftValues(shiftValues, bits);
		setShiftInstructions(instr, offset, shiftValues.wasteShift);

		arrayIndex = storeValue(humidity, offset, shiftValues, instr, arrayIndex, recordArray);
		offset = offsetValue(offset, shiftValues.overflow);
	}

	offsetAndIndex.offset = offset;
	offsetAndIndex.arrayIndex = arrayIndex;
	return offsetAndIndex;
}


