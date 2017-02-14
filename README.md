# BLE Sim

## Fork of https://github.com/WebBluetoothCG/ble-test-peripheral-android

### Changes:

* Added Color picker service
* Based on Color picker fork here: https://github.com/dati91/ble-test-peripheral-android/commit/0af90c7e6512a5ed81a1e0e12a0e61f4dbfc0670

* Allows recieving colors as Hex byte array (2-digit R, 2-digit G, 2-digit B) or 6-digit RGB Hex

[![Build Status](https://travis-ci.org/WebBluetoothCG/ble-test-peripheral-android.svg)](https://travis-ci.org/WebBluetoothCG/ble-test-peripheral-android)

The BLE Peripheral Simulator is an Android app that allows developers to try
out new features of Web Bluetooth without the need for a BLE Peripheral Device.

You can build it from source or install it from the [Google Play Store](https://play.google.com/store/apps/details?id=io.github.webbluetoothcg.bletestperipheral).

A developer can use the app to simulate a BLE Peripheral with one of two services:

* Battery Service
* Heart Rate Service

The developer can use the new Web Bluetooth features to connect to the app to Read and Write Characteristics, and Subscribe to Notifications for when the Characteristics change.

From the app a developer can set the characteristics' values, send notifications and disconnect.

![Color Picker Service](color-picker.png) ![Battery Service](Battery Service.png) ![Heart Rate Service](Heart Rate Service.png)

### Caveats

BLE peripheral mode was introduced in Android 5.0 Lollipop. Due to hardware chipset dependency, some devices don't have access to this feature. Here's a non-exhaustive list of devices that support BLE peripheral mode at the time of writing: Nexus 5X, Nexus 6P, Nexus 6, Nexus 9, Moto E 4G LTE, LG G4, Galaxy S6. See https://altbeacon.github.io/android-beacon-library/beacon-transmitter-devices.html for more.

Source: http://stackoverflow.com/questions/26482611/chipsets-devices-supporting-android-5-ble-peripheral-mode

