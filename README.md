# Budget Tracker

Budget Tracker is a web and Android application that helps users
manage their finances by tracking their paycheck, savings, budget
allocations, and expenses.

The Expo application lives in `/app`, and the Spring Boot API
lives in `/api`.

## Running Locally

### Prerequisites

- Node.js 22 (22.13.0 or later)
- JDK 21
- Docker and Docker Compose
- Git
- Android Studio (for Android development)

### Clone the Repository

```bash
git clone https://github.com/project3Group4/budgetTracker.git
cd budgetTracker
```

### Environment Configuration

Create a `.env` file in the `api` directory containing the
required environment variables:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`
- `APP_JWT_SECRET_BASE64`

Create a `.env` file in the `app` directory containing:

- `EXPO_PUBLIC_GITHUB_CLIENT_ID`

Configure the root `.env` file with the environment variables
required by Docker Compose.

Developers should configure their own development credentials
or obtain the necessary credentials securely from a team member.

All `.env` files containing credentials must remain local and
should never be committed to GitHub.

### Running the Backend

Make sure Docker Desktop is running.

From the root directory, run:

```bash
docker compose up --build
```

This starts the Spring Boot API and a local PostgreSQL database.

The backend API runs at http://localhost:8080.

To verify that the backend is running, visit:

http://localhost:8080/actuator/health

A healthy response should return:

```json
{"status":"UP"}
```

To stop the containers, run:

```bash
docker compose down
```

### Web

Make sure the backend is running before starting the frontend.

From the root directory, run:

```bash
cd app
npm install
npx expo start --web --clear
```

Open the application at http://localhost:8081.

### Android

Make sure the backend is running and start an Android emulator
through Android Studio.

From the root directory, run:

```bash
cd app
npm install
npm run android
```

The Android emulator connects to the local backend using
http://10.0.2.2:8080.

An Expo development build is required for OAuth authentication.

## Running Tests

### API Tests

From the root directory, run:

```bash
cd api
./gradlew test
```

No production database credentials are required for these tests.

To run the backend tests and verify the JaCoCo coverage
threshold, run:

```bash
./gradlew clean check
```

### Frontend Tests

Instructions will be added once the Jest test suite is configured.

### End-to-End Tests

Instructions will be added once the Playwright test suite is
configured.

## Production URLs

- Web Application: TBD
- Backend API: TBD
- Health Endpoint: TBD
- Swagger UI: TBD
