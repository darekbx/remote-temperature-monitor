
#include <M5StickC.h>
#include <Wire.h>
#include "SHT3X.h"

#define DEBUG true
#define SCREEN_BREATH 8
#define NOTIFY_INTERVAL 1 // Minutes
#define MAIN_LOOP_DELAY 50

#define BATTERY_LEVEL_HEIGHT 14
#define BATTERY_LEVEL_PADDING 2

float tmp = 0.0;
float hum = 0.0;

float batteryVoltage();
void writeData();
void displaySensorReading();
void setConnected();
void setDisconnected();

void floatToByte(byte* bytes, float f){
  int length = sizeof(float);
  for(int i = 0; i < length; i++){
    bytes[i] = ((byte*)&f)[i];
  }
}
