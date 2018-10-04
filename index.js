const ReactNative = require('react-native');
const { NativeModules } = ReactNative;
const HoneywellPrinter = NativeModules.HoneywellPrinter || {}; // Hacky fallback for iOS

module.exports = HoneywellPrinter;
