#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#define SERVICE_UUID           "89409171-FE10-40B7-80A3-398A8C219855"
#define CHARACTERISTIC_UUID_TX "89409171-FE10-40AA-80A3-398A8C219855" // Notify
#define CHARACTERISTIC_UUID_RX "89409171-FE10-40BB-80A3-398A8C219855" // Receive

BLEServer *pServer = NULL;
BLECharacteristic * pTxCharacteristic;
bool oldDeviceConnected = false;
uint8_t txValue = 0;

class M5ServerCallbacks: public BLEServerCallbacks {
  
  void onConnect(BLEServer* pServer) {
    #if DEBUG
      Serial.println("BLE is connected");
    #endif
    deviceConnected = true;
  };

  void onDisconnect(BLEServer* pServer) {
    #if DEBUG
      Serial.println("BLE is disconnected");
    #endif
    deviceConnected = false;
  }
};

class M5ReceiveCallback: public BLECharacteristicCallbacks {
  
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string rxValue = pCharacteristic->getValue();
      char *cstr = new char[rxValue.length() + 1];
      strcpy(cstr, rxValue.c_str());

      #if DEBUG
        Serial.println("Received data");
        Serial.println(cstr);
      #endif
      //handleInputData(cstr);
    }
};

void initBle() {  
  BLEDevice::init("M5StickC Widget");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new M5ServerCallbacks());
  
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Notify
  pTxCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID_TX,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
                      
  pTxCharacteristic->addDescriptor(new BLE2902());

  // Write
  BLECharacteristic *pRxCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID_RX,
                                         BLECharacteristic::PROPERTY_WRITE
                                       );
  pRxCharacteristic->setCallbacks(new M5ReceiveCallback());

  pService->start();
  pServer->getAdvertising()->start(); 
}

void writeBle(String data) {
  #if DEBUG
    Serial.print("Write data: ");
    Serial.println(data);
  #endif
  pTxCharacteristic->setValue(std::string(data.c_str()));
  pTxCharacteristic->notify();
}
