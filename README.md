# Dirigible OData (extracted engine)

The OData v2 engine, extracted from the Eclipse Dirigible platform
([eclipse-dirigible/dirigible#6567](https://github.com/eclipse-dirigible/dirigible/issues/6567)) into
its own project, consumed as an **optional** dependency.

The Java packages are **unchanged** (`org.eclipse.dirigible.components.odata`,
`org.eclipse.dirigible.engine.odata2`), so existing `.odata` artefacts, event handlers and generated
projects keep working with no code changes.

## Modules

| Module | Content |
|---|---|
| `odata-core` | The parser/model library (`.odata` definition → OData model), no Spring. |
| `odata-core-test` | Its SQL/processor unit-test suite. |
| `odata-samples-northwind` | The Northwind sample. |
| `engine-odata` | The Spring surface: synchronizer, JPA domain, services, transformers, handlers, the Olingo servlet registration (`/odata/v2/*`), the `ODataHostedEngineUris` platform contribution, and the Operations-perspective artefact views (`META-INF/dirigible/perspective-operations/api/artefacts/odata*`) that list the OData artefact status from the five DB tables. |
| `template-application-odata` | The "Generate OData from EDM" template (emits `application.odata`). |

## How it plugs into the platform

Since Dirigible 15, the platform no longer names `/odata` in its security chain or request filters.
Instead it collects `org.eclipse.dirigible.components.base.http.uri.HostedEngineUris` beans and merges
their declared patterns. This project ships one such bean (`ODataHostedEngineUris`) declaring
`/odata/**` (authenticated) and `/odata/v2/*` (request/security filters). Drop the engine JAR on the
classpath and the platform secures and filters the OData surface automatically; leave it off and the
platform is unaware of `/odata`.

## Build

This project builds against a **released** platform version that already contains the
`HostedEngineUris` SPI (Dirigible **15.0.0** or later). Set the target platform version via the
`dirigible.version` property in the root `pom.xml` (defaults to the version this project is released
against).

```
mvn clean install
```

## Migration guide (for an existing deployment)

OData was bundled in the platform through the last 14.x release and is **removed from the platform in
15.0.0**. To keep using it:

1. Add the engine (and, if you use the modeler, the template) to your application build:

   ```xml
   <dependency>
     <groupId>org.eclipse.dirigible</groupId>
     <artifactId>dirigible-components-engine-odata</artifactId>
     <version><!-- extracted version, see mapping below --></version>
   </dependency>
   <dependency>
     <groupId>org.eclipse.dirigible</groupId>
     <artifactId>dirigible-components-template-application-odata</artifactId>
     <version><!-- extracted version --></version>
   </dependency>
   ```

   (Or use the pre-built image variant that includes the engine.)

2. Redeploy and confirm `GET /odata/v2/$metadata` answers with `200` and `application/xml`.

**Nothing else changes.** Your `.odata` files, OData event handlers, generated projects and the five
database tables (`DIRIGIBLE_ODATA`, `_CONTAINER`, `_HANDLER`, `_MAPPING`, `_SCHEMA`) are untouched — the
JPA entities keep their `org.eclipse.dirigible.components.odata.domain` package, which the platform's
JPA scan of `org.eclipse.dirigible.components` still covers. (If your application narrows
`dirigible.scan.packages`, make sure it still includes `org.eclipse.dirigible.components`.)

- **"Generate OData from EDM"** appears in the templates list only when
  `dirigible-components-template-application-odata` is on the classpath.
- **Rollback:** pin the previous (OData-bundling) platform version.

### Version mapping

| Platform version | OData engine | Notes |
|---|---|---|
| ≤ 14.x | bundled | OData ships in the platform fat jar. |
| 15.0.0+ | this project, `15.x` | OData removed from the platform; add this dependency. |

The last OData-bundling platform release (14.x) is supported per the platform's normal support window.

## Integration test (carried over)

`integration-tests/` holds the migration-proof assets carried over from the platform **verbatim**:
`ODataAPIIT.java.carried` (asserts `/odata/v2/$metadata` = `200` XML) and `.odata` fixtures
(`Employees.odata`, `readers.odata`) that were authored for the bundled engine. Wiring these to run
requires an integration-test harness that boots a Dirigible application with this engine on the
classpath (the equivalent of the platform's `tests-framework` `IntegrationTest` base + an app
assembly); this is the remaining step to satisfy the "passes `ODataAPIIT` against a released platform
artifact" acceptance criterion.

## #5599

The `odata-core-test` (and `odata-samples-northwind`) unit tests did not execute in the platform
because JUnit 4 was declared at `compile` scope while `junit-vintage-engine` was excluded from
`spring-boot-starter-test`. Fixed here: `junit` is `test`-scoped and the vintage-engine exclusion is
removed, so the JUnit 4 SQL/processor suites run under the JUnit Platform.