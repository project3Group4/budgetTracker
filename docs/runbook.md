# Runbook

## How to Deploy
1. Find the most recent commit that passes the CI checks
2. The current sprint's merge captain creates a new tagged Github release with release notes 
3. (TBD) Log into Render and deploy the API using the same commit that was used to make the Github release
4. Deploy the web application with that release
5. Verify that all of the environment variables are configured correctly
6. Check the health of the application using `/actuator/health` and verify that users can sign in
## How to Roll Back
Find the previous tagged release in Github. Find the deployment on Render (TBD) associated with that release and roll back the API or redeploy the chosen previous release. Roll back the web app if necessary and verify that the environment variables have the correct information if needed. Check the `/actuator/health` and ensure that users can still log into their accounts.
## How to Rotate A Secret
When a secret is leaked, you'll need to reset the password on Neon and rotate the secrets in the API .env file. You will also need to rotate the `APP_JWT_SECRET_BASE64` by securely generating a new key using `openssl rand -base64 32` and restarting the API instance and then verifying login still functions as intended. You will also need to rotate the `GITHUB_CLIENT_SECRET`, also restarting the API instance and verifying the login still functions correctly.
## Health and Log Location
When the API is running check `<API URL>/actuator/health`, a healthy response should be a HTTP status 200 with the response body of `{"status":"UP"}`, and a not healthy response should be investigated in the logs.
<br>When ran in production (TBD) you'll want to log into the Render dashboard, find the logs and find the correct request IDs.
<br>When ran locally, logs can be found through `./gradlew bootrun`
## What to do When Database is Unreachable
Check Neon's database status and check your secrets to make sure they properly meet the neon connection username, password, and link. After the secrets and database status have been checked, check the overall connection, this can also be checked in the IDE with `./gradlew dbtest`. If the database is down on Neon's side, show an error or retry on the client's side and wait for the database to come back online.
