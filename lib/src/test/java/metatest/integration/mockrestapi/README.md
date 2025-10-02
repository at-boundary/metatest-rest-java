# Integration tests

Integration tests with a mock REST api.

`mappings` folder contains mock api resources that are loaded by Wiremock service

1. Run `docker-compose up`
2. Run API tests: `./gradlew clean test --tests "metatest.integration.*" --info -DrunWithMetatest=true`