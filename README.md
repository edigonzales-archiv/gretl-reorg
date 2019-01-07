# gretl

The [Gradle](http://www.gradle.org) gretl plugin extends gradle for use as a sql-centric
(geo)data etl. gretl = gradle etl.

## Licencse

_GRETL_ is licensed under the [MIT License](LICENSE).

## Status

_GRETL_ is in stable state.

## System requirements

For the current version of _GRETL_, you will need a JRE (Java Runtime Environment) installed on your system, version 1.8 or later and gradle, version 3.4 or later.
For convenience use the gradle wrapper.

## Testing

### Unit tests
Since _GRETL_ is especially about reading and writing data from and to databases there is a need of them for proper testing. For convenience it does only rely on embedded / file based databases for unit testing. Also some of the code that reads and writes to a database is not part of the plugin itself but in other Java projects. Therefore the functional tests (which run build jobs as a whole) needs Docker to start and stop other databases like PostgreSQL/PostGIS or Oracle.

It would be possible to use Junit categories (Tags in Junit5) to isolate unit tests that need e.g. PostgreSQL. But again for convencience this is not used.

### Functional tests
Functional tests are running complete build jobs with TestKit. Docker is needed for starting databases like PostgreSQL with the TestContainer framework.

## Manual

A german user manual can be found here: [docs/user/](docs/user/index.md)
