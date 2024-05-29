# guess-number

This application provides the backend for a guess the number game with using WebSocket for communication.

-----

## Requirements:

- Java 17
- SpringBoot 3
- Gradle 8.4
- Docker 25
- JUnit
- Currently, only tested on Fedora 39 and IntelliJ IDEA

## How to run locally:

```bash
cd guess-number
gradle clean
gradle bootRun
```

## Run with Docker:

```bash
cd guess-number
gradle clean build
docker build --no-cache --build-arg JAR_FILE=build/libs/\*.jar -t renvl/guess-number .
docker run -p 8080:8080 -t renvl/guess-number
```

## WebSocket:

### [ws://localhost:8080](ws://localhost:8080)

* `CONNECTION`
  ```javascript
  ws = new WebSocket('ws://localhost:8080/game');
  ```

* `SEND MESSAGE`
  ```json
  {
    "player": "player",
    "number": "5",
    "amount": "12"
  }
  ```

## Guess Number Game [DEMO](http://localhost:8080/index.html)

## Parametrization:

To test RTP (Return to Player) **N ROUNDS**, set the properties file according
to [application-test.properties](src/test/resources/application-test.properties)

## License

guess-number is distributed under the terms of the
[MIT License](https://choosealicense.com/licenses/mit).
