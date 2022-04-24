# recommendation-system

This is an experimental system, whose aim is to make recommendations for stock decisions. 
It performs calculations basing on investore data stored in graph database and current stock movement predictions. Both data are provided exernally:

- graph database Neo4j
- data-server (available here: https://github.com/R-tooR/data-server)

In order to run you need to have installed:
- Neo4j
- Java 15 (openjdk)
- Scala 2.12 (IntelliJ plugin should also work)
- sbt plugin manager

## To run:
1) Clone this repository
2) Setup Neo4j (if you don't have) and put some data there
3) Initialize Neo4j
4) Clone, build and run *data-server*: https://github.com/R-tooR/data-server
5) Build project
6) Specify path to files of historical data (refer to *Important* section)
7) Run `java -cp recommendation-system Client -Djava.util.logging.config.file=path/to/logging.properties
-appconfig=path/to/appConfiguration.properties`

If everything is fine, recommendation-system should connect to database and to *data-server*, and start working.

The file `appConfiguration.properties` contains configuration of parameters related to connection to database and logic of application.
Some of those paramters are:
- `application.inverstors_top_n=<Int>` number of investors, on which calculation will be performed
- `application.target.investor=<Int>` ID of investor, for which recommendations will be calculated
- `application.embeddings_top_n_ratio=(0,1]` Which part of embedding vector will be taken to calculate distance between investors (it takes most significant values)
- `application.updater.enabled=<Boolean>` If database structure will be changed during every iteration. WARNING: Unstable
- `application.updater.change_ratio=(0,1]` How many connections will be changed after iteration. WARNING: Unstable

### Important

To initialize dataset properly, import file to neo4j database: https://drive.google.com/file/d/1L6_ceVu5DN4AkvNpAixLYM9uiQxqX75O/view?usp=sharing

How to perform import: https://neo4j.com/developer/kb/export-sub-graph-to-cypher-and-import/


