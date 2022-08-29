
> POST endpoint: http://localhost:8080/v1/libraryEvent

Body:
```json
{
  "libraryEventId": null,
  "book": {
    "bookId": 1,
    "bookName": "Moby Dick",
    "bookAuthor": "Herman Melville"
  }
}
```

> PUT endpoint: http://localhost:8080/v1/libraryEvent

Body:
```json
{
  "libraryEventId":1,
  "book": {
    "bookId": 1,
    "bookName": "Title: Moby Dick",
    "bookAuthor": "Author: Herman Melville"
  }
}
```

### Kafka Producer Configurations

Important configurations:

- ack values = 0, 1, all
  - 1: guaranties message is written to a leader replica (Default)
  - all: guarantees message is written to a leader and all replicas
  - 0: no guarantee (Not Recommended)
  
- retries
  - Integer value = [0 - 2147483647]
  - In Spring Kafka, the default value is 2147483647

- retry.backoff.ms
  - Integer value represented in milliseconds
  - Default value is 100ms


> For Real Databases Integration Tests see: https://www.testcontainers.org/
> and 
> https://youtu.be/Wpz6b8ZEgcU