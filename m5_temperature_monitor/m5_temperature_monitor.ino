/**
 * TODO:
 *  - button debounce: https://www.arduino.cc/en/Tutorial/BuiltInExamples/Debounce
 *  - add headers for ble and battery
 *  - change names for ble an battert
 *  - add to battery function to read voltage, which will be used in BLE transfer
 *  - move temp/hum sensor to external file with header
 *  - decrease refresh rate?
 *  - change battery display ratio
 *  - remove from ble time set (android phone will store datetimes)
 *  - prettier format of temp/hum display
 */
#include "m5_temperature_monitor.h"
 
RTC_TimeTypeDef TimeStruct;

bool deviceConnected = false;
bool isScreenOn = true;

byte topMargin = BATTERY_LEVEL_HEIGHT + BATTERY_LEVEL_PADDING;

void setup() {
  #if DEBUG
    Serial.begin(115200);
    Serial.flush();
    delay(50);
    Serial.println("M5Widget ready");
  #endif
  
  M5.begin();
  Wire.begin(0, 26);
  
  M5.Axp.ScreenBreath(SCREEN_BREATH);
  M5.Lcd.setRotation(3);
  
  initBle();
  resetTime();
}

void loop() {
  M5.Rtc.GetTime(&TimeStruct);

  if (deviceConnected && TimeStruct.Minutes >= NOTIFY_INTERVAL) {
    writeData();
    resetTime();
  }
  
  handleTurnOffButton();
  handleScreenOnOffButton();
  
  if (isScreenOn) {
    updateDisplay();
  }

  delay(MAIN_LOOP_DELAY);
}

void writeData() {
  float vbat = batteryVoltage();
  writeBle(String(vbat) + ";" + String(tmp) + ";" + String(hum));
}

void handleScreenOnOffButton() {
  if (digitalRead(M5_BUTTON_HOME) == LOW) {
    isScreenOn = !isScreenOn;
    if (isScreenOn) {
      M5.Axp.ScreenBreath(0);
    } else {
      M5.Axp.ScreenBreath(SCREEN_BREATH);
    }
  }
}

void handleTurnOffButton() {
  if (digitalRead(BUTTON_B_PIN) == LOW) {
    M5.Axp.PowerOff();
  }
}

void updateDisplay() {
  displayBatteryLevel();
  indicateBleState();
  displaySensorReading();
}

void indicateBleState() {
  if (deviceConnected) {
    setConnected();
  } else {
    setDisconnected(); 
  }
}

void setConnected() {
  M5.Lcd.fillCircle(150, 7, 4, GREEN);
  M5.Lcd.drawCircle(150, 7, 5, WHITE);
}

void setDisconnected() {
  M5.Lcd.fillCircle(150, 7, 4, RED);
  M5.Lcd.drawCircle(150, 7, 5, WHITE);
}

void resetTime() {
  TimeStruct.Hours   = 0;
  TimeStruct.Minutes = 0;
  TimeStruct.Seconds = 0;
  M5.Rtc.SetTime(&TimeStruct);
}
