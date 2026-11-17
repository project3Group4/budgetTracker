# Runbook

## How to Deploy
1. Find the most recent commit that passes the CI checks and create a Github release with the most up to date approved information.
## How to Roll Back
Find the previous tagged release in github and update it with any updated credentials if needed. Recheck health before releasing the rollback.
## How to Rotate A Secret
When a secret is leaked, you'll need to reset the password on Neon and rotate the secrets. You will also need to rotate the `APP_JWT_SECRET_BASE64` by securely generating a new key and restarting the API instance. You will also need to rotate the `GITHUB_CLIENT_SECRET` and related secrets, also restarting the API instance.
## Log Location and Health
When the API is running check `/actuator/health` if it's 200/UP it's healthy, otherwise it's not. <!-- idk what to put for log location -->
## What to do When Database is Unreachable
Check Neon's database status and check your secrets to make sure they properly meet the neon connection username, password, and link. After the secrets and database status have been checked, check the overall connection, this can also be checked in the IDE with `./gradlew dbtest`. If the database is down on Neon's side, show an error or retry on the client's side.
