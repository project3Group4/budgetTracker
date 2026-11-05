# Budget Tracker Architecture

## System context

The user accesses Budget Tracker through its web or Android client and can sign in with GitHub or Google.

```mermaid
flowchart LR
    user[Budget Tracker user]
    app["Budget Tracker<br/>Web and Android"]
    github[GitHub]
    google[Google]

    user -->|uses| app
    app -->|OAuth sign-in| github
    app -->|OAuth sign-in| google
```

## Containers

The web and Android clients use the same React Native with Expo codebase. The Spring Boot API runs on Render and stores application data in Neon PostgreSQL.

```mermaid
flowchart LR
    user[User]
    client["React Native with Expo<br/>Web and Android clients"]
    api["Spring Boot API<br/>Render"]
    db[(Neon PostgreSQL)]
    github[GitHub OAuth]
    google[Google OAuth]

    user -->|uses| client
    client -->|API requests with app JWT| api
    api -->|reads and writes| db

    client -->|Authorization Code plus PKCE| github
    client -->|Authorization Code plus PKCE| google
    client -->|sends authorization code| api
    api -->|exchanges code and verifies identity| github
    api -->|exchanges code and verifies identity| google
    api -->|returns app JWT| client
```

## Authentication flow

The client uses OAuth Authorization Code with PKCE to sign in with a provider. It sends the authorization code to the API. The API verifies the user with the provider, creates or finds the user account, and returns an app JWT. The client sends that JWT with later API requests.

The project guide plans GitHub sign-in on web first, then Google sign-in on web and Android. Update this document if the team’s final implementation plan changes.
