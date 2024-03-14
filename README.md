# LogsFinder

LogsFinder is a versatile tool designed to facilitate searching through logs stored across different mediums, including local filesystems and AWS S3 buckets.

## Key Components:
* API Layer: RESTful endpoints allowing clients to query log data based on keywords, date ranges, and other criteria.
* Service Layer: Core logic for processing search queries, interfacing with different storage services.
* Storage Service Factory: Dynamically selects the appropriate storage service (e.g., local filesystem, AWS S3) based on configuration.
* Storage Services: Abstracted interfaces and implementations for interacting with specific storage backends.
* Executor Service: Manages concurrent log search operations, optimizing performance and resource utilization.

## Getting Started
### Prerequisites
* Java 17 or higher
* Maven 3.6.3 or higher
* Access to AWS S3 (optional, required only if using S3 as a storage backend)

## Setup and Installation
#### Clone the repository:
* git clone [https://github.com/nadeemqwerty/logsfinder_grep.git](https://github.com/nadeemqwerty/logsfinder_grep.git)

* cd LogsFinder

#### Configure application properties:

* Edit src/main/resources/application.properties to set your storage type and other relevant settings, or add in environment variable

#### Properties
* storage.type=local # or 's3' for AWS S3
* local.storage.baseDir=/path/to/your/logs
* #AWS S3 Configuration
* aws.s3.bucket.name=yourbucketname
* aws.s3.base.path=logsfolder/
* aws.region.name=us-east-1
#### Build the project:
* mvn clean package
#### Run the application:

* java -jar target/logsfinder-0.0.1-SNAPSHOT.jar
#### Usage
* Use the provided RESTful API to search through logs. Here's an example using curl:

* curl -X GET 'http://localhost:8081/api/logs/search?searchKeyword=error&from=2023-01-01&to=2023-01-02&ignoreCase=true'
Replace the parameters as needed based on your search criteria

#### Sample Response

{
"items": [
"Line 9: error: cannot find symbol"
],
"totalItems": 1,
"currentPage": 0,
"itemsPerPage": 10,
"totalPages": 1
}