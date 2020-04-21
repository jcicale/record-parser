# record-parser

## Usage

Prerequisite: You will need to have [leiningen](https://leiningen.org/) installed to build the jars.

First, clone the repository.

#### CLI Usage

From the project root, build the jar:

```lein with-profile cli uberjar```

To run the CLI jar, the basic command is:

```java -jar target/record-parser-cli.jar <options> <argument>```

For example,

```java -jar target/record-parser-cli.jar -f resources/records.csv,resources/records.psv,resources/records.ssv parse-gender```

Will return the view of the records in the resources folder, sorted by gender. The available arguments are `parse-gender`, `parse-birthdate`, and `parse-last-name`
This information can also be viewed by invoking the jar with just the `-h` flag.

#### Rest API 

From the project root, build the jar:

```lein with-profile api uberjar```

To start the server, run:

```java -jar target/record-parser-api.jar```

You will see some logging, and then the server will be running at port 3000.

To POST some data, use the following cURL command (any of the delimiter formats may be used to send data):

```curl -d 'Cicale | Julia | F | Green | 1990-05-24' -H "Content-Type: text/plain" -X POST  http://localhost:3000/records```

The GET endpoints are:
 - /records/gender
 - /records/birthdate
 - /records/name
 
 To GET the data, you can use:

```curl http://localhost:3000/records/gender```

Feel free to pipe the result through `jq` or visit the endpoints in your browser if you have a json formatter extension.

#### Test Coverage

To run tests, run:

```lein test```

To view a report of the test coverage, run:

```lein cloverage```

#### Assumptions and Notes
 - The CLI tool is not interactive (e.g. multiple outputs will require multiple runs of the tool with the same data)
 - The input files do not contain header rows
 - Missing/incorrect data will result in the CLI tool exiting with an error message and a 400 response from the API
 - The input format of the date is required to be a date ISO string (different than the output format)
 - Data does not persist across different runs of either program, nor can data parsed by the CLI be read by running the API
 - The total size of the input files will not exceed the maximum heap of the program 