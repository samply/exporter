# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.0.0 - 2023-08-04]
### Added
- Search Controller
- Blaze Store Client
- LDM Client
- Containers and Containers Templates
- FHIR-Bundle to Containers Converter
- FHIR Rev Includes in Containers Templates
- Executor in Fhir Store Client
- Converter Pattern
- Converter Manager
- Converter Template and Conversion Template
- Input and Output Format in Converter
- Load converter from application context
- Converter Selector
- Get Source Ids and Converter Template Ids
- Project Version
- REST API: retrieve-query
- Docker
- Timestamp variable for templates
- Environment variables for templates
- Parent-fhir-path in attribute template
- MDR URN in attribute template
- Child fhir path
- Spring Boot 3.0.1
- Anonym Attribute Template
- Converter Session
- Apache POI
- Excel Converter
- Anonym Attributes
- Operation in Attribute
- EXTRACT_RELATIVE_ID attribute operation
- Api Key
- Java 19
- Template in request body
- Attribute condition fhir path
- Exporter DB
- Attribute join fhir path
- Attribute condition id and value fhir path
- Excel Format
- Flyway config
- Query entity
- JPA config (Hibernate)
- Get all queries (paging)
- Query Execution and Query Execution File
- Download files (also as zip)
- Clean Temp and Write Files Jobs
- GET active, error and archived inquiries
- Format inquiry timestamp
- Allow Cors
- Default value for inquiry label and description
- Archive Query
- Send execution file url in inquiry
- Get Query Execution Status
- Opal Importer
- Http relative path
- Http servlet request scheme
- Export in JSON format
- Export in XML format
- File Filter in Response
- Validation to bundles
- FHIR Profile URL
- Opal permission
- Validation Utils
- FHIR Package Loader
- Converter Template attribute: FHIR Package
- Explorer, CSV Explorer
- Deactivate hapi fhir client and hibernate logs (configurable)
- FHIR Terminology Server
- Location String and Message for validation
- Parameter LOG_FHIR_VALIDATION
- Java 20
- Buffered Logger
- Request Logs
- Csv Explorer
- Json Explorer
- XML Exporter
- Request Page Size
- Http response header number-of-pages

### Changed
- Rename Conversion Template as Converter Template
- First anonym: 1
- Cors config
- Bugfix: Allow join-fhir-path parent with many children
- Rename teiler to exporter
- Bugfix: Filter paths
- Bugfix: Change default template
- Bugfix: Generate filenames only once in session for csv and excel
- Bugfix: Recognize internal network requests
- Bugfix: Avoid null resources
