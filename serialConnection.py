#!/usr/bin/env python

import serial
import json
import datetime

ser = serial.Serial("/dev/ttyUSB0", 9600)

while 1:
    try:
        file = open('data.txt', 'a')

        # read Serial interface and write in a text file
        serial_data = ser.readline()
        #serial_data = json.loads(serial_data)
        #d = serial_data['Date']
        #serial_data['Date'] = datetime.datetime.strptime(d, "%Y-%m-%dT%H:%M:%SZ")
        print serial_data
        file.write(serial_data)
    except serial.serialutil.SerialException:
        pass
    except KeyboardInterrupt:
        ser.close()
        file.close()
