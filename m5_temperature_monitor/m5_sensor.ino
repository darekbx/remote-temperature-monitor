
SHT3x Sensor;

void initializeSensor() {
  
  Wire.begin(0, 26);
  
  SHT3x::CalibrationFactors TemperatureCalibration;
  TemperatureCalibration.Factor = 0.8540; 
  TemperatureCalibration.Shift  = 0.1500;
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
