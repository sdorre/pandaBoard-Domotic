/*
   DS3231_test.pde
   Eric Ayars
   4/11

   Test/demo of read routines for a DS3231 RTC.

   Turn on the serial monitor after loading this to check if things are
   working as they should.

   Connections
   ===========
   Connect SCL to analog 5
   Connect SDA to analog 4
   Connect VDD to 3.3V DC
   Connect GROUND to common ground

 */

#include <Adafruit_BMP085_U.h>
#include <DS3231.h>
#include <Wire.h>

DS3231 Clock;
bool Century=false;
bool h12;
bool PM;
byte ADay, AHour, AMinute, ASecond, ABits;
bool ADy, A12h, Apm;

Adafruit_BMP085_Unified bmp = Adafruit_BMP085_Unified(10085);

void getBMPMeasures(void)
{
    // Get a new sensor event
    sensors_event_t event;
    bmp.getEvent(&event);

    // Display the results (barometric pressure is measure in hPa)
    if (event.pressure)
    {
        // Display atmospheric pressue in hPa
        Serial.print("\"Pressure\":");
        Serial.print(event.pressure);
        Serial.print(",");

        /* Calculating altitude with reasonable accuracy requires pressure    *
         * sea level pressure for your position at the moment the data is     *
         * converted, as well as the ambient temperature in degress           *
         * celcius.  If you don't have these values, a 'generic' value of     *
         * 1013.25 hPa can be used (defined as SENSORS_PRESSURE_SEALEVELHPA   *
         * in sensors.h), but this isn't ideal and will give variable         *
         * results from one day to the next.                                  *
         *                                                                    *
         * You can usually find the current SLP value by looking at weather   *
         * websites or from environmental information centers near any major  *
         * airport.                                                           *
         *                                                                    *
         * For example, for Paris, France you can check the current mean      *
         * pressure and sea level at: http://bit.ly/16Au8ol                   */

        // First we get the current temperature from the BMP085
        float temperature;
        bmp.getTemperature(&temperature);
        Serial.print("\"Temperature\":");
        Serial.print(temperature);
        Serial.print(",");

        /* Then convert the atmospheric pressure, and SLP to altitude         */
        /* Update this next line with the current SLP for better results      */

        float seaLevelPressure = SENSORS_PRESSURE_SEALEVELHPA;
        Serial.print("\"Altitude\":"); 
        Serial.print(bmp.pressureToAltitude(seaLevelPressure,
                    event.pressure)); 
    }
    else
    {
        Serial.println("Sensor error");
    }
}


/********************** CLOCK *******************************************/
void getClockMeasures(void)
{
    // send what's going on to the serial monitor.
    // example :
    //           "Date":"2014-12-6T12:45:00Z","Clock_temperature":22.5

    // Start with the year
    Serial.print("\"Date\":");
    Serial.print("\"20");
    Serial.print(Clock.getYear(), DEC);
    Serial.print('-');

    // then the month
    Serial.print(Clock.getMonth(Century), DEC);
    Serial.print('-');

    // then the date
    Serial.print(Clock.getDate(), DEC);
    Serial.print('T');

    // Finally the hour, minute, and second
    Serial.print(Clock.getHour(h12, PM), DEC);
    Serial.print(':');
    Serial.print(Clock.getMinute(), DEC);
    Serial.print(':');
    Serial.print(Clock.getSecond(), DEC);
    Serial.print("Z\"");

    Serial.print(",");
    Serial.print("\"Clock_temperature\":");
    Serial.print(Clock.getTemperature(), 2);
}


void setup() {

    // Start the serial interface
    Serial.begin(9600);

    // Start the I2C interface
    Wire.begin();

    // Initialise the sensor
    if(!bmp.begin())
    {
        // There was a problem detecting the BMP085 ... check your connections
        Serial.print("Ooops, no BMP085 detected ... Check your wiring or I2C ADDR!");
        while(1);
    }
}

void loop() {

    // create a Simple JSON 
    Serial.print("{");

    getClockMeasures();
    Serial.print(",");
    getBMPMeasures();

    Serial.println("}");

    // wait for 5 minutes
    delay(300000);
}
