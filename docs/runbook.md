# Runbook

## How to Deploy
1. Find the most recent commit that passes the CI checks on `main`
2. The current sprint's merge captain creates a new tagged Github release with release notes 
3. (TBD) Log into Render and deploy the API using the same commit that was used to make the Github release
4. Deploy the web application with that release
5. Verify that all of the environment variables are configured correctly
6. Check the health of the application using `/actuator/health` and verify that users can sign in
## How to Roll Back
Find the previous tagged release in Github. Find the deployment on Render (TBD) associated with that release and roll back the API or redeploy the chosen previous release. Roll back the web app if necessary and verify that the environment variables have the correct information if needed. Check the `/actuator/health` and ensure that users can still log into their accounts.
## How to Rotate A Secret
When a secret is leaked, you'll need to:
- Database: Reset the password on Neon and rotate the secrets in the API .env file.
- JWT Secret: Generate a new key using `openssl rand -base64 32` and update `APP_JWT_SECRET_BASE64`. Users will need to sign in again after the secret is rotated.
- Generate a new client secret in the GitHub OAuth application settings, revoke the compromised secret, and update `GITHUB_CLIENT_SECRET`
<br>After updating the affected credentials, restart the API and verify that the application functions correctly.
<br>(TBD) Once the application is deployed to Render, update the corresponding environment variables in the Render dashboard and redeploy the API.
## Health and Log Location
When the API is running check `<API URL>/actuator/health`, a healthy response should be a HTTP status 200 with the response body of `{"status":"UP"}`, and a not healthy response should be investigated in the logs.
<br>When ran in production (TBD) you'll want to log into the Render dashboard, find the logs and find the correct request IDs.
<br>When running locally through Gradle, logs can be found in the terminal running `./gradlew bootRun`
<br>When running through Docker Compose, logs can be viewed using `docker compose logs -f` from the root directory.
## What to do When Database is Unreachable
Check Neon's database status and check your secrets to make sure they properly meet the neon connection username, password, and link. After the secrets and database status have been checked, check the overall connection, this can also be checked in the IDE with `./gradlew dbtest`. If the database is down on Neon's side, show an error or retry on the client's side and wait for the database to come back online.
