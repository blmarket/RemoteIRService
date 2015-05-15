# RemoteIRService

Andorid InfraRed Blaster http service wrapper

## How it works

This app creates http server to receive public request and emits IR blaster. it can works as smart remote control.

## Requirements

Android 4.4.2(needs more effort to support >= 4.4.3) device with `CONSUMER_IR_SERVICE` capability. at least tested on Galaxy Note 3. but many other devices provide this.

## Example

```
curl -v -X POST -H 'Content-Type: application/json' -d '{"freq":"38400","code":["345","171","23","19","23","19","23","62","23","19","23","19","23","19","23","19","23","19","23","62","23","62","23","19","23","62","23","62","22","62","23","62","23","62","23","62","22","20","23","19","23","62","23","19","22","62","23","62","23","62","23","19","23","62","23","62","22","20","22","62","23","19","23","20","22","20","22","1543"]}' http://192.168.25.74:8080
```

this sends 'HDMI 3' signal to LG TV. codes can be retrieved from several sites.

## Caveats

Response is not that fast T.T I guess it's due to android battery management.
