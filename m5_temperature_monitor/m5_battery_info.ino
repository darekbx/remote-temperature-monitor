
int batteryLevelBoxWidth = 38;
int batteryLevelWidth = batteryLevelBoxWidth - 4;

const float batteryLevel100 = 4.2;
const float batteryLevel0 = 3.6;

float batteryVoltage() {
  float vbat = M5.Axp.GetVbatData() * 1.1 / 1000;
  return vbat;
}

void displayBatteryLevel() {
  /*
  4.2 volts 100%
  4.1 about 90%
  4.0 about 80%
  3.9 about 60%
  3.8 about 40%
  3.7 about 20%
  3.6 empty
  */
  float vbat = batteryVoltage();
  float diff = vbat - batteryLevel0;
  float diffMax = batteryLevel100 - batteryLevel0;
  float percent = ceil(diff / diffMax * 100);
  int level = min(max(int(batteryLevelWidth * (percent / 100)), 4), batteryLevelWidth);

  M5.Lcd.drawRect(1, 1, batteryLevelBoxWidth, BATTERY_LEVEL_HEIGHT, WHITE);

  if (percent <= 25) {
    M5.Lcd.fillRect(3, 3, batteryLevelWidth, BATTERY_LEVEL_HEIGHT - 4, RED);
  } else {
    M5.Lcd.fillRect(3, 3, batteryLevelWidth, BATTERY_LEVEL_HEIGHT - 4, GREEN);
  }
  M5.Lcd.fillRect(3 + level, 3, batteryLevelWidth - level, BATTERY_LEVEL_HEIGHT - 4, BLACK);

  M5.Lcd.fillRect(batteryLevelBoxWidth + 4, 0, 80, 12, BLACK);
  M5.Lcd.setCursor(batteryLevelBoxWidth + 4, 4, 1);
  M5.Lcd.setTextSize(1);
  M5.Lcd.printf("%.3fV", vbat);
}
