# Budget Tracker
The Expo app lives in `/app` and the Springboot API is in `/api`
## Running Locally
#### Prerequisites:
- Node 22
- JDK 21
- Android Studio

#### Web
In the android studio terminal right after cloning the project, enter in the following commands in the app directory (`cd app`). You will only have to run `npm install` once.
```
npm install
npx expo start --web --clear
```
Then open the application in http://localhost:8081

#### Android
Turn on an android emulator, then run the following command in the app directory (`cd app`).
```
npm run android
```
To run on a USB connected android device, connect your USB to the device, set your USB settings to transferring files, and then run the following command in the app directory.
```
npx expo run:android --device
```
Press enter when it prompts you to select a device. It will take a few minutes for it to install and then open.

## Running API Tests
Starting from the root, you will want to run the following commands.
```
cd api
./gradlew test
```
No credentials are needed for this portion of the tests. If you want to run tests that test the database, you will need to supply a neon database URL, database user, and database password in a .env file at the api root. Then run
```
./gradlew dbtest
```
