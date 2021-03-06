import 'dart:async';

import 'package:flutter/services.dart';

class Fzxing {
  static const MethodChannel _channel = const MethodChannel('fzxing');
  static Future<List<String>> scan({
    bool isBeep = true,
    bool isContinuous = false,
    bool isBlowhorn = true,
    bool isShipment = false,
    int continuousInterval = 1000,
    List<String> refNumber,
    List<String> orderNumber,
    List<String> scannedRefNumber,
  }) async {
    final List barcodes = await _channel.invokeMethod(
        'scan',
        Map()
          ..['isBeep'] = isBeep
          ..['isContinuous'] = isContinuous
          ..['isBlowhorn'] = isBlowhorn
          ..['isShipment'] = isShipment
          ..['continuousInterval'] = continuousInterval
          ..['refNumber'] = refNumber
          ..['orderNumber'] = orderNumber
          ..['scannedRefNumber'] = scannedRefNumber);
    return barcodes.map((it) => it as String).toList();
  }
}
