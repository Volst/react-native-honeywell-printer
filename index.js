const ReactNative = require('react-native');
const { NativeModules } = ReactNative;
const HoneywellPrinter = NativeModules.HoneywellPrinter || {}; // Hacky fallback for iOS

console.log('PRINTER v2');

module.exports = HoneywellPrinter;
