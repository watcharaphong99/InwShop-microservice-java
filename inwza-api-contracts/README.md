# inwza-api-contracts

Protobuf + gRPC stubs for all Inwza Spring services.

## Build & install locally

```bash
mvn clean install
```

Installs `com.inwza:inwza-api-contracts:0.1.0-SNAPSHOT` into `~/.m2/repository`.

## Use in a service

```xml
<dependency>
  <groupId>com.inwza</groupId>
  <artifactId>inwza-api-contracts</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Java packages

| Proto | `java_package` |
|-------|----------------|
| auth | `com.inwza.contracts.auth` |
| item | `com.inwza.contracts.item` |
| player | `com.inwza.contracts.player` |
| inventory | `com.inwza.contracts.inventory` |

Player proto uses `package playerpb` (Go used `authpb` by mistake).

## Publish (later)

Add `distributionManagement` for GitHub Packages or Nexus and run `mvn deploy` on tag.
