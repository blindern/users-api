# users-api

[![CircleCI](https://circleci.com/gh/blindern/users-api.svg?style=svg)](https://circleci.com/gh/blindern/users-api)

API between user database and other services. The objective is to have an API
so that the underlying user database can be swapped from LDAP to other types,
if necessary.

All services having access to this resource have full access to the user
database.

## Testing the endpoint

```bash
curl \
  -H "Authorization: Bearer $(cat /fbs/users-api-key)" \
  https://foreningenbs.no/users-api/groups \
  | jq .
```

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

## Setup

Pushes to `master` is auto-deployed to production through CircleCI.
See https://github.com/blindern/drift/tree/master/services/users-api
for details.

### Requirements

Ensure you have JDK 11 or newer on your system. If not install it through SDKMAN!

https://sdkman.io

```bash
sdk install java
```

We use Gradle as build system. You can test it by

```bash
./gradlew -v
```

The LDAP server is located at `ldapmaster.zt.foreningenbs.no`. To be able
to connect to it you must be on the ZeroTier VPN network.
See https://github.com/blindern/drift/tree/master/zerotier

### Linting and testing

```bash
./gradlew ktlintCheck test
```

### Building and running

```bash
./gradlew shadowJar
java -jar build/libs/users-api-1.0-SNAPSHOT-all.jar
curl localhost:8000
```

### Running directly

```bash
./gradlew runShadow
```

### Checking for dependency updates

```bash
./gradlew dependencyUpdates
```

## Configuration

A file named `overrides.properties` must be present in the working directory
and override needed properties from `defaults.properties`.

The LDAP admin password can be located at
https://foreningenbs.no/confluence/x/PgYf

The HMAC key credential can be located at `/fbs/users-api-key` on athene.

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
