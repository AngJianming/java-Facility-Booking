# Facility Bookings using Java Programming
In this Java Programming project I used gradle insted of maven as I soly wanted to explore the tasks carry out the "workhorses" in Gradle, which is to built on a graph of relationships between tasks.

## Prerequisites
- gradle

## Build project using gradle
1. `gradle wrapper`\
2. `./gradlew build -x test`

## To start a server
`./gradlew run --args='[PORT_NUMBER] [AT_MOST_ONCE] [PACKET_DROP_RATE]'`\
- `PORT_NUMBER`: Server port number
- `AT_MOST_ONCE`: Boolean, true to start server using at-most-once semantics and false to start using at-least-once semantics
- `PACKET_DROP_RATE`: Probability that a packet will be dropped by the server

## To start a client
`cd src/main/java` \
`javac client/Main.java` \
`java client.Main [HOST_NAME] [SERVER_PORT] [CLIENT_PORT] [PACKET_DROP_RATE] [TIMEOUT] [MAX_TRIES]`

- `HOST_NAME`: IP address of server
- `SERVER_PORT`: Server port number
- `CLIENT_PORT`: Client port number
- `PACKET_DROP_RATE`: Probability that a packet will be dropped by the client
- `TIMEOUT`: Client socket timeout in milliseconds
- `MAX_TRIES`: Maximum number of retries

