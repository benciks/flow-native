# flow-native

A mobile app for time tracking and task management based on the tools Timewarrior and Taskwarrior
created as part of bachelor thesis at the Brno University of Technology. There is built apk in
releases page which is connected to server running for demonstration purposes. It is encouraged to
spin up your own server for production use, which is available
at [https://github.com/benciks/flow-backend](https://github.com/benciks/flow-backend). Afterwards,
you need to edit the ```app/src/main/java/com/example/flow/di/AppolloModule.kt``` file and add your
server url to the ```PROD_BASE_URL``` variable.

## Synchronization
In case you are using the demo application which is connected to the demonstration server and you want to set up sync with your Timewarrior and Taskwarrior instances, please visit [flow-sync](https://sync.benciks.me/) and log in with the credentials used in app. Afterwards, follow the configuration instructions provided at this page.

## Features

- [x] Time tracking
- [x] Task management
- [x] Synchronization with server
- [x] Multiple users

## Development

### Requirements

- [Android Studio](https://developer.android.com/studio/index.html)
- [flow-backend](https://github.com/benciks/flow-backend) - The server for the app needs to be
  running for development purposes, alongside the requirements of the server mentioned in it's
  repository.

### Build

To build the app, run the following command in the root of the project:

```
./gradlew assembleDebug
```

### Run

To run the app, use the following command:

```
./gradlew :app:installDebug
```
