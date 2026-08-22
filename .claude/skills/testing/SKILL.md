---
name: testing
description: Rules for writing tests - JUnit, TestFX for headless Java FX UI tests, package mirroring, coverage and the developer test vs. integration test ("IT" suffix) split. Load before creating or changing any test class.
---

# Testing

* JUnit test system MUST be used
* For Java FX UI tests use TestFX framework
  * Must run in the background without an open UI
* Every use case MUST be tested
* Code coverage should reach 100%
* The package structure of the production code is to be mirrored
* EVERY test method is to be documented with a detailed KDoc describing the use case
* Tests are to be split into two categories
    * **Developer tests** - Simple unit tests covering individual pieces of functionality
      * All Test Classes without suffix of "IT"
    * **Integration tests** - Tests covering complete features or aiming at performance
      * Identified by Class Name ending with "IT"
