# QR-code Generator and Reader

## Application used [Java 15](https://onurdesk.com/what-are-preview-features-in-java-15/) | [Onurdesk](https://onurdesk.com/)

###### Spring boot application exposing REST API endpoint to genrate QR-code representing custom message and another endpoint to read the decoded message, built using Java [Spring boot](https://onurdesk.com/category/spring/spring-boot/) and [google's zxing library](https://opensource.google/projects/zxing).

<center>
	<a target='_blank' href='https://spring-boot-qr-code-generator.herokuapp.com/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config'>Running Application</a>
</center>

## Local Setup

* Install [Java 15](https://onurdesk.com/what-are-preview-features-in-java-15/)
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
