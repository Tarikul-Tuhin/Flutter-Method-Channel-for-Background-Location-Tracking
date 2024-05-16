import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class StreamLiveLocation extends StatefulWidget {
  const StreamLiveLocation({super.key});

  @override
  State<StreamLiveLocation> createState() => _StreamLiveLocationState();
}

class _StreamLiveLocationState extends State<StreamLiveLocation> {
  static const locationChannel = MethodChannel('locationPlatform');

  final _eventChannel = const EventChannel('com.example.apprunbgrnd');

  StreamSubscription? subscription;

  String data = '';

  @override
  void initState() {
    super.initState();

    subscription = _eventChannel.receiveBroadcastStream().listen((event) {
      data = event.toString();
      setState(() {});
      print('FlutterRecevied: $event');
    }, onError: (Object obj, StackTrace stackTrace) {
      print('FlutterRecevied: $obj');
      print('FlutterRecevied: $stackTrace');
    });
  }

  @override
  void dispose() {
    subscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          ElevatedButton(
            onPressed: () async {
              await locationChannel.invokeListMethod('getLocation');
            },
            child: const Text('Get Locaiton'),
          ),
          ElevatedButton(
            onPressed: () async {
              await locationChannel.invokeListMethod('stopLocation');
            },
            child: Text('Stop Locaiton $data'),
          )
        ],
      ),
    );
  }
}
