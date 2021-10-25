
#include <M5StickC.h>
#include <Wire.h>
#include "SHT3x.h"

#define DEBUG true

#define BLE_NAME "M5StickC"
#define SCREEN_BREATH 8
#define NOTIFY_INTERVAL 1 // Minutes
#define MAIN_LOOP_DELAY 10
#define ENABLE_SENSOR_HEATER false

#define BATTERY_LEVEL_HEIGHT 14
#define BATTERY_LEVEL_PADDING 2

float tmp = 0.0;
float hum = 0.0;

float batteryVoltage();
void writeData();
void initializeSensor();
void displaySensorReading();
void setConnected();
void setDisconnected();
