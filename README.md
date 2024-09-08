# verveChallenge

## Run Application without extensions:
- This can be validated from branch: `withoutExtensions` in this repository.
- To run this. Run command `./mvnw clean package -DskipTests`.
- Then run `java -jar verveChallenge-0.0.1-SNAPSHOT.jar`
- Or simply run `mvn spring-boot:run`.
- Make request to application using `curl --location 'http://localhost:8080/api/verve/accept?id=123&endpoint=http%3A%2F%2Fexample.com'`


## Run Application with extensions implemented:
- This code is stored in `main` branch of the repository
- To run this you need docker. Use following commands:
  - (Optional) Run if you want to run local code `./mvnw clean package -DskipTests`
  - (Optional) If you want to run local code, replace `image: ritik0503/verveuniquecounter:latest` in `docker-compose.yml`
  with `build: .`.
  - Else image is present on docker hub at `ritik0503/verveuniquecounter:latest`. Just run below steps and ignore above 2.
  - `docker-compose build`
  - `docker-compose up -d`
  - Make request to application using `curl --location 'http://localhost:8080/api/verve/accept?id=123&endpoint=http%3A%2F%2Fexample.com'`
  - Check logs at `docker-compose logs -f `
- To stop the docker container:
  - `docker-compose down`

```
Note: 
- Above partial implementation without extension is in branch `withoutExtensions` at
https://github.com/kandoria/verveChallenge/tree/withoutExtensions
- Complete implementation with extension is in main branch at 
https://github.com/kandoria/verveChallenge
```
