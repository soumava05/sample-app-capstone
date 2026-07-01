# Deployment Diagram - Sample Todo App

```mermaid
graph TB
    subgraph DevMachine["Developer Machine / Server"]
        subgraph JVM["JVM Process (Java 21)"]
            TomcatEmbed["Embedded Tomcat - Port 8090"]
            AppCode["todo-app-1.0.0.jar"]
        end
        subgraph LocalFS["Local Filesystem"]
            StorageDir["storage/\nusers.json\ntasks/alice.json\ntasks/bob.json"]
        end
        Maven["Apache Maven - pom.xml"]
    end

    subgraph ClientSide["Client Browser"]
        Browser["Web Browser - HTTP to 8090"]
    end

    Browser -->|HTTP Requests| TomcatEmbed
    TomcatEmbed --> AppCode
    AppCode -->|File I/O| StorageDir
    Maven -->|mvn spring-boot:run| JVM
```
