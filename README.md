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
Turn on an android emulator or connect an android device with USB, then run the following command in the app director (`cd app`).
```
npm run android
```
It will take a few minutes for it to install and then open.

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
