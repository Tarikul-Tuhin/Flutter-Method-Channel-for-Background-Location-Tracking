import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const platform = MethodChannel('com.example.apprunbgrnd');
  String _batteryLevel = 'Unknown battery level.';

  Future<void> _getBatteryLevel() async {
    String batteryLevel;
    try {
      final result = await platform.invokeMethod<int>('getBatteryLevel');
      batteryLevel = 'Battery level at $result % .';
    } on PlatformException catch (e) {
      batteryLevel = "Failed to get battery level: '${e.message}'.";
    }

    setState(() {
      _batteryLevel = batteryLevel;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Background Location Tracking'),
        ),
        body: Center(
          child: ElevatedButton(
            onPressed: _getBatteryLevel,
            child: Text(_batteryLevel),
          ),
        ),
      ),
    );
  }

  // void startLocationService() async {
  //   const platform = MethodChannel('com.example.batterylevel');

  //   try {
  //     await platform.invokeMethod('startLocationService');
  //   } on PlatformException catch (e) {
  //     if (kDebugMode) {
  //       print("Failed to start location service: '${e.message}'.");
  //     }
  //   }
  // }
}
