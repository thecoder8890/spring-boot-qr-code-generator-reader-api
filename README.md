# QR-code Generator and Reader

[![Java CI with Maven](https://github.com/thecoder8890/spring-boot-qr-code-generator-reader-api/actions/workflows/maven-ci.yml/badge.svg)](https://github.com/thecoder8890/spring-boot-qr-code-generator-reader-api/actions/workflows/maven-ci.yml)

## Application used [Java 17](https://onurdesk.com/what-are-preview-features-in-java-17/) and Spring Boot 3.5.0 | [Onurdesk](https://onurdesk.com/)

###### Spring boot application exposing REST API endpoint to genrate QR-code representing custom message and another endpoint to read the decoded message, built using Java, [Spring Boot 3.5.0](https://spring.io/projects/spring-boot/) and [google's zxing library](https://opensource.google/projects/zxing).

<center>
	<a target='_blank' href='https://spring-boot-qr-code-generator.herokuapp.com/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config'>Running Application</a>
</center>

## Local Setup

* Install [Java 17](https://onurdesk.com/what-are-preview-features-in-java-17/)
* Install [Maven](https://onurdesk.com/what-is-maven-plugin/)

Recommended way is to use [sdkman](https://sdkman.io/) for installing both maven and java

Run the below commands in the core

```
mvn clean install
```

```
mvn spring-boot:run

```

server port is configured to 9090 which can be changed in application.properties file

Go to the below url to view swagger-ui (API docs)

```
http://localhost:9090/swagger-ui.html
```

## Testing

The core QR code generation and reading functionalities are tested in `src/test/java/com/onurdesk/iris/service/QrCodeServiceTests.java`. These tests cover:

*   **Positive Scenarios:**
    *   Successful QR code generation with valid text input.
    *   Successful reading and decoding of a valid QR code image.
*   **Negative Scenarios:**
    *   Attempting QR code generation with null or invalid DTO.
    *   Handling of empty title during QR code generation.
    *   Attempting to read invalid image files (not images or not QR codes).
    *   Attempting to read QR codes with unexpected content (not deserializable to the expected DTO).
    *   Handling I/O exceptions during file reading.

### Running Tests

You can run the tests using Maven:

```bash
mvn test
```

Alternatively, running `mvn clean install` will also execute the tests as part of the build lifecycle.
