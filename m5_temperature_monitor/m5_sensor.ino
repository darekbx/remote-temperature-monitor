
SHT3X sht3x;

void displaySensorReading() {
 if (sht3x.get() == 0){
    tmp = sht3x.cTemp;
    hum = sht3x.humidity;
    M5.Lcd.setCursor(0, 20, 2);
    M5.Lcd.printf("Temp: %2.1f Humi: %2.0f", tmp, hum);
  }
}
