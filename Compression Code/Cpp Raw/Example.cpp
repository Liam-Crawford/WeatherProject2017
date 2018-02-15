//============================================================================
// Name        : Example.cpp
// Author      : Jesse Bricknell
// Version     :
// Copyright   : Your copyright notice
// Description : Example program that displays the useage of the updating record function.
//============================================================================

#include <iostream>
#include "DataPacking.h"
#include <stdint.h>
using namespace std;

int main() {
	DataPacking data;

	DataPacking::arrayManagement manager;
	manager.arrayIndex = 0;
	manager.offset = 0;

	DataPacking::fullRecord r;
	uint8_t array[50];
	manager = data.storeRecord(125.0, 1110.0, 100.0, array, manager, r);

	for(int i = 0; i < manager.arrayIndex; i++){
		std::cout << (int) array[i] << " at " << i << std::endl;
	}
	return 0;
}



