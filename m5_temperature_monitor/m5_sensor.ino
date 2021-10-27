#include <OneWire.h> 
#include <DallasTemperature.h>

#define ONE_WIRE_BUS 26 

OneWire oneWire(ONE_WIRE_BUS); 
DallasTemperature sensors(&oneWire);

void initializeSensor() {
  sensors.begin(); 
}

void displaySensorReading() {
  sensors.requestTemperatures();
  tmp = sensors.getTempCByIndex(0);
  
  M5.Lcd.setTextSize(2);
  
  M5.Lcd.setCursor(4, 20, 2);
  M5.Lcd.printf("%2.1f", tmp);
  M5.Lcd.drawRoundRect(64, 24, 8, 8, 4, WHITE);
  
  //M5.Lcd.setCursor(94, 20, 2);
  //M5.Lcd.printf("%2.0f%%", hum);
  
  M5.Lcd.setTextSize(1);
}

/*
SHT3x Sensor;

void initializeSensor() {
  
  Wire.begin(0, 26);
  
  SHT3x::CalibrationFactors TemperatureCalibration;
  TemperatureCalibration.Factor = 0.9200; 
  TemperatureCalibration.Shift  = 0.0500;
  Sensor.SetTemperatureCalibrationFactors(TemperatureCalibration);
}

void displaySensorReading() {
  Sensor.UpdateData();
  tmp = Sensor.GetTemperature();
  hum = Sensor.GetRelHumidity();

  M5.Lcd.setTextSize(2);
  
  M5.Lcd.setCursor(4, 20, 2);
  M5.Lcd.printf("%2.1f", tmp);
  M5.Lcd.drawRoundRect(64, 24, 8, 8, 4, WHITE);
  
  M5.Lcd.setCursor(94, 20, 2);
  M5.Lcd.printf("%2.0f%%", hum);
  
  M5.Lcd.setTextSize(1);
}
*/
