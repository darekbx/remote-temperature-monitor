#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#define SERVICE_UUID           "89409171-FE10-40B7-80A3-398A8C219855"
#define CHARACTERISTIC_UUID_TX "89409171-FE10-40AA-80A3-398A8C219855" // Notify

BLEServer *pServer = NULL;
BLECharacteristic * pTxCharacteristic;

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

void initBle() {  
  BLEDevice::init(BLE_NAME);
  
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new M5ServerCallbacks());
  
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Notify
  pTxCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID_TX,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
                      
  pTxCharacteristic->addDescriptor(new BLE2902());

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
