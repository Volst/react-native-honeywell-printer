# React Native Honeywell Printer

This package works with Honeywell devices that use the Intermec PB50 printer. It may also work on other Intermec printers, but this is not guaranteed.

Note that this is an experimental package.

Tested to work with React Native 0.56 and 0.57. Only works on Android.

## Installation

```
yarn add react-native-honeywell-printer
```

To install the native dependencies:

```
react-native link react-native-honeywell-printer
```

Unless you already know the Bluetooth MAC address of the printer, you need to use `react-native-bluetooth-serial` as well. Since it has an issue with recent versions of React Native, we use [this fork](https://github.com/jhonber/react-native-bluetooth-serial).

## Usage

First you need to copy `printerprofiles.json` from this repository to `android/app/src/main/assets/printerprofiles.json` in your project. You can modify this file to change the styling of the print document.

```js
import BluetoothSerial from 'react-native-bluetooth-serial';
import honeywellPrinter from 'react-native-honeywell-printer';

async function print() {
    const devices = await BluetoothSerial.list();
    // Search for Intermec PB50 devices; this is the only tested printer at the moment.
    const device = devices.find(device => device.name.includes('PB50'));
    if (device) {
        // This is the profile name defined in android/app/src/main/assets/printerprofiles.json
        const profileName = 'PB32_Fingerprint';
        await honeywellPrinter.print(profileName, device.id, 'My variable to print');
    }
}
```


## TODO

- There is no progress indication
- There is no indication whether printing succeeded or failed

We probably won't have time to build this since we'll only have a Intermec printer for a limited time.