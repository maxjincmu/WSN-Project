/*
File: MqttPub.ino

This example simply publishes analog data into a MQTT topic. Before use it,
please configure the MQTT server address via Uno WiFi Web Panel. Topics are
automatically created (or subscribed) via api calls into the sketch. You can use
a maximum of 3 topics.

Note: works only with Arduino Uno WiFi Developer Edition.
*/

#include <Wire.h>
#include <UnoWiFiDevEd.h>
#include <SparkFun_GridEYE_Arduino_Library.h>
#include <ClosedCube_BME680.h>

#define CONNECTOR "mqtt"
#define TOPICWEIGHT "weight"
#define TOPICGRIDEYE "grideye"
#define TOPICBME "bme"
// Use these values (in degrees C) to adjust the contrast
#define HOT 40
#define COLD 20

GridEYE grideye;
//ClosedCube_BME680 bme680;

void setup() {
	Ciao.begin();
  // Start your preferred I2C object 
  Wire.begin();
  // Library assumes "Wire" for I2C but you can pass something else with begin() if you like
  grideye.begin();
  // Pour a bowl of serial
  Serial.begin(115200);
  
}


void loop(){
  delay(5000);
  char buf[10];
  sprintf(buf, "WSR:%d", analogRead(A0));
  Ciao.write(CONNECTOR, TOPICWEIGHT, buf); 

  int val, count;
  count = 0;
  // loop through all 64 pixels on the device and map each float value to a number
  // between 0 and 3 using the HOT and COLD values we set at the top of the sketch
  for(unsigned char i = 0; i < 64; i++){
    val = map(grideye.getPixelTemperature(i), COLD, HOT, 0, 3);
    if(val==0){Serial.print(F("."));}
    else if(val==1){Serial.print(F("o")); count++;}
    else if(val==2){Serial.print(F("0")); count++;}
    else if(val==3){Serial.print(F("O")); count++;}
    Serial.print(F(" "));
    if((i+1)%8==0){
      Serial.println();
    }
  }
  // in between updates, throw a few linefeeds to visually separate the grids. If you're using
  // a serial terminal outside the Arduino IDE, you can replace these linefeeds with a clearscreen
  // command
  Serial.println();
  Serial.println();

  if(count > 5)
    Ciao.write(CONNECTOR, TOPICGRIDEYE, F("Blob :)"));
  else
    Ciao.write(CONNECTOR, TOPICGRIDEYE, F("No blob :(")); 
}
