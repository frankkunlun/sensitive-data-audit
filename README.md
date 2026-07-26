# Sensitive Data Audit JVM Agent

This repository implements the core of the attached *应用系统敏感数据操作审计技术实施方案* as a Java 8-compatible, fail-open JVM agent.

## Implemented capabilities

- `premain` and `agentmain` loading with YAML configuration.
- Byte Buddy probes for Servlet (`javax`/`jakarta`) request boundaries and JDBC `Connection`, `Statement`, `PreparedStatement`, and `ResultSet` operations.
- Trace/request context, Spring Security identity resolution, safe parameter metadata (values are never retained), SQL normalization/fingerprints, conservative multi-dialect parsing, sensitive catalog matching, row/affected-row counts, and request/data audit events.
- Bounded asynchronous JSONL file reporting with hash-chain integrity fields.
- Optional bounded asynchronous HTTP batch reporting (`reporter.type: http`) for a centralized audit platform.
- Fail-open instrumentation and reporter behavior; agent errors never escape into business methods.

## Build and run

```bash
mvn test
mvn -pl audit-agent-bootstrap -am package -DskipTests
java -javaagent:audit-agent-bootstrap/target/audit-agent-bootstrap-1.0.0-SNAPSHOT.jar=config/agent.yaml -jar your-application.jar
```

The supplied `config/agent.yaml` writes JSON Lines to `logs/sensitive-audit.jsonl` relative to the config directory. For centralized HTTP delivery:

```yaml
reporter:
  type: http
  url: https://audit.example.internal/v1/events
  token: ${AUDIT_TOKEN} # resolve through deployment templating; do not commit secrets
```

The agent intentionally records only parameter type/null/length metadata. Add or change catalog entries in `config/sensitive-catalog.yaml`; matching is case-insensitive and supports `*`/`?` patterns. SQL parse failures are still emitted with a fingerprint and `parseSuccess=false`.

## Scope notes

The implementation is the deployable foundation for the方案本期范围. Database-session context injection, full function-directory hot reload, Kafka transport, and framework-specific export probes are extension points rather than silently simulated behavior.
