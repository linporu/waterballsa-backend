#!/bin/bash

# Script to run BDD tests
# Usage: ./run-bdd-tests.sh

echo "=== Running BDD Tests ==="
echo ""

# Navigate to backend directory
cd "$(dirname "$0")" || exit 1

# Run tests using Maven
./mvnw clean test -Dcucumber.filter.tags="@isa"

echo ""
echo "=== Test Results ==="
echo "HTML Report: target/cucumber-reports/cucumber.html"
echo "JSON Report: target/cucumber-reports/cucumber.json"
