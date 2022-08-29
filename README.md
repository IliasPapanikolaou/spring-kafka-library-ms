
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

### Recovery in Kafka Consumer

When number of retries are exhausted due to an error, we can configure the consumer to Recover 
and retry to succeed later.

Recovery Type:
1. Reprocess the failed record (message) again
    - Example: Service that the Consumer interacts is temporarily down
2. Discard the Message and move on
    - Invalid message: Parsing error, Invalid Event

Reprocess the failed message options:
1. Publish the failed message to a Retry Topic
2. Save the failed message in a DB and retry with a Scheduler

Discard the message options:
1. Publish the failed record in DeadLetter Topic for tracking purposes
2. Save the failed record into a Database for tracking purposes
