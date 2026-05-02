# User API WebFlux + JWT

Author: omartapia <omar.tapia.h@gmail.com>

- WebFlux
- CRUD H2
- @Valid
- JWT
- OpenAPI en resources

Run:
./gradlew bootRun


Testing
-------

Unit and integration tests are configured with Gradle. To run the full test suite locally:

1. From the project root run:

   ./gradlew test

2. After the run, a test report is available at:

   build/reports/tests/test/index.html

3. To run a single test class or method (useful during development):

   ./gradlew test --tests "com.nisum.userapi.package.ClassNameTest"
   # or a specific method
   ./gradlew test --tests "com.nisum.userapi.package.ClassNameTest.methodName"

Notes
-----
- Tests use JUnit 5, Mockito and Reactor Test. Some tests are integration-style (spring context) and may take longer.
- If you add or rename packages, update test package declarations accordingly (the project keeps tests mirroring src/main structure).
- If you want to run tests and see logs in the console use --info or --debug with Gradle, e.g.:

  ./gradlew test --info

If you want, I can add CI configuration to run tests automatically on push.

Postman collection: src/main/resources/User_API_Enterprise.postman_collection.json