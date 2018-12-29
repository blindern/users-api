# users-api

[![CircleCI](https://circleci.com/gh/blindern/users-api.svg?style=svg)](https://circleci.com/gh/blindern/users-api)

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

## TODO

- replace HMAC with simpler bearer token
- simpler v2 version of the api

## Linting and testing

```bash
./gradlew ktlintCheck test
```

## Building and running

```bash
./gradlew shadowJar
java -jar build/libs/users-api-1.0-SNAPSHOT-all.jar
curl localhost:8000
```

### Running directly

```bash
./gradlew runShadow
```

## Configuration

A file named `overrides.properties` must be present in the working directory
and override needed properties from `defaults.properties`.

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

## Checking for dependency updates

```bash
./gradlew dependencyUpdates
```
