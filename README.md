# users-api

https://foreningenbs.no/users-api/

API between user database and other services. The objective is to have an API
so that the underlying user database can be swapped from LDAP to other types,
if necessary.

All services having access to this resource have full access to the user
database.

## Tech used

- JVM as runtime
- Kotlin as language: https://kotlinlang.org/docs/reference/
- Gradle with Kotlin DSL for building: https://docs.gradle.org/5.0/userguide/kotlin_dsl.html
- Http4k as HTTP toolkit: https://www.http4k.org/
- Caffeine as cache library: https://github.com/ben-manes/caffeine
- Moshi for Json serialization: https://github.com/square/moshi
- Spek 2 for testing: https://spekframework.org/
- Kluent for assertions: https://github.com/MarkusAmshove/Kluent
- MockK for mocking: https://mockk.io/

## Development

Ensure you have JDK 11 or newer on your system. If not install it through SDKMAN!
See https://sdkman.io

The LDAP servers are located at `ldap.zt.foreningenbs.no`. To be able
to connect to it, you must be on the ZeroTier VPN network.
See https://github.com/blindern/drift/

Running locally:

```bash
./gradlew runShadow
```

Lint and test:

```bash
./gradlew check
```

### Checking for dependency updates

```bash
./gradlew dependencyUpdates
```

## Production

See https://github.com/blindern/drift/tree/master/services/users-api

## Configuration

A file named `overrides.properties` must be present in the working directory
and override needed properties from `defaults.properties`.

The LDAP admin password can be located at
https://foreningenbs.no/confluence/x/PgYf

The HMAC key credential can be located at `/fbs/users-api-key` on athene.

## Testing the endpoint

```bash
curl \
  -H "Authorization: Bearer $(cat /fbs/users-api-key)" \
  https://foreningenbs.no/users-api/groups \
  | jq .
```

## TODO

- replace HMAC with simpler bearer token (initial bearer-token version done)
- simpler v2 version of the api

## Endpoints actually in use

As of Dec 2018.

- `/users-api/simpleauth` (intern + simplesaml)
- `/users-api/group/XX` (intern)
- `/users-api/groups` (intern)
- `/users-api/user/XX` (intern + simplesaml)
- `/users-api/user/XX?grouplevel=2` (intern)
- `/users-api/users` (intern)
- `/users-api/users?emails=XX` (intern + simplesaml)
- `/users-api/users?grouplevel=1` (intern)
