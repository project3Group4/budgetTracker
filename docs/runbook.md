# Runbook

## How to Deploy
1. Find the most recent commit that passes the CI checks and create a Github release
2. 
## How to Roll Back
Find the previous tagged release in github and update it with any new credentials if needed.
## How to Rotate A Secret
When a secret is leaked, you'll need to reset the password on Neon and rotate the secrets. You will also need to rotate the `APP_JWT_SECRET_BASE64` and `GITHUB_CLIENT_SECRET`
## Log Location and Health

## What to do When Database is Unreachable
Check Neon's database status and check your secrets to make sure they properly meet the neon connection username, password, and link. After the secrets and database status have been checked, check the overall connection, this can also be checked in the IDE with `./gradlew dbtest`. If the database is down on Neon's side, show an error or retry on the client's side.
