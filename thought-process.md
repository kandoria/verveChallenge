# Thought Process: REST Application for Unique Request Counter

## Original Problem Thought Process

1. **Request Handling**
    - Create a Spring Boot application to handle GET requests at `/api/verve/accept`.
    - Extract `id` and optional `endpoint` parameters from the request.

2. **ID Deduplication**
    - Implemented a system to track unique IDs within each minute.
    - Use `ConcurrentHashMap` for thread-safe operations in a multi-threaded environment.
    - Key: current minute timestamp, Value: Set of unique IDs.

3. **Endpoint Notification**
    - If an endpoint is provided, send a GET request with the count of unique IDs.
    - Use `RestTemplate` for making HTTP requests.

4. **Logging**
    - Implemented a scheduled task to log the count of unique requests every minute.
    - Use SLF4J for logging to a file by creating FileLogger and consoleLogger separately.
    - Create a logback-spring.xml file to configure a separate file appender for our FileLogger. 
    This will write logs from FileLogger to a file named unique_requests.log.

5. **Error Handling**
    - Implemented proper error handling and logging for all operations.
    - Create a custom `HttpRequestException` for handling HTTP request errors.


## Extension 1 Thought process: POST Request Instead of GET

1. **Modification of Endpoint Notification**
    - Change the `sendHttpGetRequest` method to `sendHttpPostRequest`.
    - Define a data structure for the POST request body (e.g., JSON with count and timestamp).
    - Update the `RestTemplate` call to use `postForEntity` instead of `getForEntity`.

2. **Content Type Handling**
    - Set the appropriate content type for the POST request (application/json).
    - Create a `HttpHeaders` object and set it in the request entity.

3. **Error Handling Update**
    - Adjust error handling to account for potential POST-specific errors.

## Extension 2 Thought process: Distributed Deduplication

1. **Distributed Storage**
    - Replace `ConcurrentHashMap` with a distributed solution (Redis).
    - Decided to go with Redis for its atomic operations and high performance.

2. **Redis Integration**
    - Add Redis and related dependencies to the project.
    - Configure Redis connection in the `application.properties`.
    - Create Redis related Beans in `RedisConfig.java`.
    - Inject `RedisTemplate` for Redis operations.

3. **ID Storage and Retrieval**
    - Using Redis Sets for storing unique IDs for each minute.
    - Implement `add` operation to Redis Set, which automatically handles uniqueness.
    - Update `getUniqueCount` method to use Redis `size` operation.

4. **Cleanup Process**
    - Modify the scheduled task to clean up expired Redis keys.

5. **Concurrency Handling**
    - Leverage Redis's built-in atomic operations to handle concurrent requests across multiple instances.

## Extension 3 Thought Process: Distributed Streaming Service

1. **Streaming Service Selection**
    - Decided to go with Apache Kafka as the distributed streaming platform for its scalability and durability.

2. **Kafka Integration**
    - Add Kafka and related dependencies to the project.
    - Configure Kafka connection in the `application.properties`.
    - Create Beans required for kafka in `KafkaConfig.java`.
    - Inject `KafkaTemplate` for Kafka operations.

3. **Message Production**
    - Replace file logging with Kafka message production.
    - Define a Kafka topic for unique request counts (e.g., "unique-request-counts").
    - Implement `sendToKafka` method to send count data to Kafka.

4. **Scheduled Task Update**
    - Modify the scheduled task to send data to Kafka instead of logging to a file.

5. **Error Handling**
    - Implement error handling for Kafka operations.
    - Log any issues with sending messages to Kafka.

## Docker Compose Setup

1. **Containerization**
    - Create a Dockerfile for the Spring Boot application.
    - Define services in docker-compose.yml: app, Redis, Zookeeper, and Kafka.

2. **Network Configuration**
    - Set up appropriate network links between containers.
    - Configure environment variables for the app to connect to Redis and Kafka and
   update `application.properties` too.

3. **Persistence**
    - Consider volume mounts for Redis and Kafka for data persistence (if needed).
    - For now have not created any volume mounts.

4. **Scaling Considerations**
    - Design the docker-compose file to allow easy scaling of the app service.

```
Note: 
- Above partial implementation without extension is in branch `withoutExtensions` at
https://github.com/kandoria/verveChallenge/tree/withoutExtensions
- Complete implementation with extension is in main branch at 
https://github.com/kandoria/verveChallenge
- To Run these branches check steps at README.md file.
```
