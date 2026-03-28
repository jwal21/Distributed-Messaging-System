# Java Distributed-Messaging System

Java CLI group communication system built with a client-server architecture.

## Features

- automatic coordinator election for the first member
- member list retrieval with IDs, IPs, ports, and coordinator details
- private and broadcast messaging
- timestamped inbox-based message storage
- coordinator re-election on leave
- heartbeat-based timeout detection for abnormal failure
- JUnit tests for core coordinator and messaging logic

## Quick Start

### Compile

```bash
mkdir -p bin
find src -name "*.java" -print0 | xargs -0 javac -d bin
```

### Run server

```bash
java -cp bin Main
```

### Run client

```bash
java -cp bin client.ClientMain <serverPort> <messageDelayMs> <leaveDelayMs>
```

Client arguments:

```text
ClientMain <serverPort> <messageDelayMs> <leaveDelayMs>
```

## Fault Tolerance Demo

1. Start the server
2. Start two or more clients
3. Stop one client with `Ctrl+C`
4. Wait for the timeout threshold
5. Observe server-side removal of the failed member
6. If the failed member was coordinator, observe automatic re-election

## Project Structure

```text
src/
  task1/
    Main.java
    client/
    coordinator/
    model/
    server/
    util/

test/
  task1/coordinator/
    CoordinatorServiceTest.java
```

## Testing

Current JUnit coverage includes:

- first member becomes coordinator
- second member does not replace the coordinator
- coordinator leave causes re-election
- private and broadcast messages appear in inboxes

## Notes

- CLI-based automatic simulation
- centralised server-managed group state
- in-memory state only
