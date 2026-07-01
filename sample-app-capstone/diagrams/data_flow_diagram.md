# Data Flow Diagram --- Sample Todo App

```mermaid
flowchart TD
    subgraph External["External Actors"]
        AnonymousUser([Anonymous User])
        AuthUser([Authenticated User])
    end

    subgraph WebLayer["Web Layer"]
        RF[Registration Form Data]
        LF[Login Credentials]
        TF[Task Form Data]
    end

    subgraph ProcessLayer["Processing"]
        BV{Bean Validation}
        BL{Business Logic Validation}
        PE[Password Encoding BCrypt]
        UN[Username Normalization]
        UUID_GEN[UUID Generation]
    end

    subgraph PersistenceLayer["Persistence Layer"]
        UJ[(users.json)]
        TJ[htasks/username.json)]
    end

    AnonymousUser --> RF
    AnonymousUser --> LF
    AuthUser --> TF

    RF --> BV
    TF --> BV
    BV -- Invalid --> ERR[Error Messages]
    BV -- Valid --> UN
    UN --> BL
    BL -- Fail --> ERR
    BL -- Pass --> PE
    PE --> UJ

    LF --> SpringSec[Spring Security Auth]
    SpringSec --> UJ
    UJ --> SpringSec
    SpringSec -- Success --> HLP[HTML Pages]
    SpringSec -- Fail --> ERR

    UJ --> HLP
    TJ --> HLP
    HLP --> AuthUser
```
