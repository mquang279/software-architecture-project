#!/bin/bash

# K6 Benchmark Test Runner for Movie Reservation System
# This script runs all K6 tests for each microservice and saves results

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
RESULTS_DIR="./test-results"
SCRIPTS_DIR="."
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Service configurations (name:port)
declare -A SERVICES=(
  # ["user-service"]="8080"
  # ["movie-service"]="8080"
  # ["notification-service"]="8080"
  # ["reservation-service"]="8080"
  # ["seat-service"]="8080"
  ["show-service"]="8080"
  # ["theater-service"]="8080"
)

# Create results directory if it doesn't exist
mkdir -p "$RESULTS_DIR"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}K6 Benchmark Test Suite${NC}"
echo -e "${GREEN}Movie Reservation System${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "Timestamp: $TIMESTAMP"
echo -e "Results directory: $RESULTS_DIR"
echo ""

# Function to check if k6 is installed
check_k6() {
  if ! command -v k6 &> /dev/null; then
    echo -e "${RED}Error: k6 is not installed${NC}"
    echo "Please install k6 from https://k6.io/docs/getting-started/installation/"
    exit 1
  fi
  echo -e "${GREEN}✓ k6 is installed ($(k6 version))${NC}"
}

# Function to check if jq is installed
check_jq() {
  if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}⚠ Warning: jq is not installed${NC}"
    echo -e "${YELLOW}  Error code extraction will be skipped${NC}"
    echo -e "${YELLOW}  Install jq for detailed error reporting: sudo apt-get install jq${NC}"
    return 1
  fi
  echo -e "${GREEN}✓ jq is installed${NC}"
  return 0
}

# Function to check if a service is running
check_service() {
  local service=$1
  local port=$2
  
  if curl -s -o /dev/null -w "%{http_code}" "http://localhost:$port/actuator/health" 2>/dev/null | grep -q "200\|404"; then
    echo -e "${GREEN}✓ $service is running on port $port${NC}"
    return 0
  else
    echo -e "${YELLOW}⚠ Warning: $service may not be running on port $port${NC}"
    return 1
  fi
}

# Function to run a k6 test
run_test() {
  local service=$1
  local port=$2
  local script="${SCRIPTS_DIR}/${service}-test.js"
  local result_file="${RESULTS_DIR}/${service}_${TIMESTAMP}.json"
  local summary_file="${RESULTS_DIR}/${service}_${TIMESTAMP}_summary.txt"
  
  echo ""
  echo -e "${YELLOW}----------------------------------------${NC}"
  echo -e "${YELLOW}Testing: $service${NC}"
  echo -e "${YELLOW}----------------------------------------${NC}"
  
  if [ ! -f "$script" ]; then
    echo -e "${RED}Error: Script not found: $script${NC}"
    return 1
  fi
  
  echo "Script: $script"
  echo "Base URL: http://localhost:$port"
  echo "Results: $result_file"
  echo ""
  
  # Run k6 test
  BASE_URL="http://localhost:$port" K6_WEB_DASHBOARD=true k6 run \
    --out json="$result_file" \
    --summary-export="$summary_file" \
    "$script"
  
  local exit_code=$?
  
  if [ $exit_code -eq 0 ]; then
    echo -e "${GREEN}✓ Test completed successfully${NC}"
  else
    echo -e "${RED}✗ Test failed with exit code: $exit_code${NC}"
  fi
  
  # Display error codes immediately after test
  echo ""
  echo "Analyzing results..."
  extract_error_codes "$result_file"
  
  return $exit_code
}

# Function to parse JSON summary and extract key metrics
extract_metrics() {
  local summary_file=$1
  
  if [ -f "$summary_file" ]; then
    echo "  Request Rate: $(grep -o '"rate":[0-9.]*' "$summary_file" | head -1 | cut -d':' -f2) req/s"
    echo "  P95 Latency: $(grep -o '"p(95)":[0-9.]*' "$summary_file" | head -1 | cut -d':' -f2) ms"
    echo "  Error Rate: $(grep -o '"rate":[0-9.]*' "$summary_file" | tail -1 | cut -d':' -f2)%"
  fi
}

# Function to extract HTTP error codes from JSON result file
extract_error_codes() {
  local result_file=$1
  
  if [ ! -f "$result_file" ]; then
    echo "  HTTP Error Codes: Result file not found"
    return
  fi
  
  # Check if jq is available
  if ! command -v jq &> /dev/null; then
    echo "  HTTP Error Codes: Skipped (jq not installed)"
    return
  fi
  
  # Extract all HTTP status codes with their counts
  local all_codes=$(jq -r 'select(.type=="Point" and .metric=="http_reqs" and .data.tags.status != null) | .data.tags.status' "$result_file" 2>/dev/null | sort | uniq -c | sort -rn)
  
  if [ -z "$all_codes" ]; then
    echo "  HTTP Status Codes: No data available"
    return
  fi
  
  # Filter for error codes (4xx, 5xx)
  local error_codes=$(echo "$all_codes" | grep -E "(4[0-9]{2}|5[0-9]{2})")
  
  # Display all status codes
  echo "  HTTP Status Codes:"
  while IFS= read -r line; do
    if [ -n "$line" ]; then
      local count=$(echo "$line" | awk '{print $1}')
      local code=$(echo "$line" | awk '{print $2}')
      # Highlight error codes in red
      if echo "$code" | grep -qE "(4[0-9]{2}|5[0-9]{2})"; then
        echo -e "    - HTTP $code: $count occurrences ${RED}(ERROR)${NC}"
      else
        echo "    - HTTP $code: $count occurrences"
      fi
    fi
  done <<< "$all_codes"
  
  if [ -z "$error_codes" ]; then
    echo -e "  ${GREEN}✓ No HTTP errors (4xx/5xx) detected${NC}"
  fi
}

# Function to generate summary report
generate_summary() {
  local summary_file="${RESULTS_DIR}/test_summary_${TIMESTAMP}.txt"
  
  echo ""
  echo -e "${GREEN}========================================${NC}"
  echo -e "${GREEN}Generating Summary Report${NC}"
  echo -e "${GREEN}========================================${NC}"
  
  {
    echo "K6 Benchmark Test Summary"
    echo "Movie Reservation System"
    echo "========================="
    echo "Timestamp: $TIMESTAMP"
    echo ""
    echo "Test Results:"
    echo "-------------"
    
    for service in "${!SERVICES[@]}"; do
      local result_file="${RESULTS_DIR}/${service}_${TIMESTAMP}.json"
      local summary_json="${RESULTS_DIR}/${service}_${TIMESTAMP}_summary.txt"
      
      echo ""
      echo "Service: $service"
      if [ -f "$result_file" ]; then
        echo "Status: ✓ Completed"
        echo "Results file: $result_file"
        if [ -f "$summary_json" ]; then
          extract_metrics "$summary_json"
        fi
        extract_error_codes "$result_file"
      else
        echo "Status: ✗ Failed or Not Run"
      fi
    done
    
    echo ""
    echo "========================="
    echo "All results saved to: $RESULTS_DIR"
    echo ""
    echo "To view detailed results, check the JSON files in $RESULTS_DIR"
    echo "Or use k6 cloud or grafana for visualization"
  } | tee "$summary_file"
  
  echo ""
  echo -e "${GREEN}Summary saved to: $summary_file${NC}"
}

# Function to run specific service test
run_specific_test() {
  local service=$1
  
  if [ -z "${SERVICES[$service]}" ]; then
    echo -e "${RED}Error: Unknown service '$service'${NC}"
    echo "Available services:"
    for s in "${!SERVICES[@]}"; do
      echo "  - $s"
    done
    exit 1
  fi
  
  check_k6
  echo ""
  echo "Checking service..."
  check_service "$service" "${SERVICES[$service]}"
  
  run_test "$service" "${SERVICES[$service]}"
  
  echo ""
  echo -e "${GREEN}Test completed for $service${NC}"
}

# Function to display usage
usage() {
  echo "Usage: $0 [OPTIONS]"
  echo ""
  echo "Options:"
  echo "  -s, --service SERVICE    Run test for specific service only"
  echo "  -h, --help              Show this help message"
  echo "  --skip-check            Skip service availability check"
  echo ""
  echo "Available services:"
  for service in "${!SERVICES[@]}"; do
    echo "  - $service (port ${SERVICES[$service]})"
  done
  echo ""
  echo "Examples:"
  echo "  $0                           # Run all tests"
  echo "  $0 -s user-service          # Run test for user-service only"
  echo "  $0 --skip-check             # Run all tests without checking services"
}

# Main execution
main() {
  local skip_check=false
  local specific_service=""
  
  # Parse command line arguments
  while [[ $# -gt 0 ]]; do
    case $1 in
      -s|--service)
        specific_service="$2"
        shift 2
        ;;
      --skip-check)
        skip_check=true
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        echo -e "${RED}Unknown option: $1${NC}"
        usage
        exit 1
        ;;
    esac
  done
  
  # If specific service requested, run only that test
  if [ -n "$specific_service" ]; then
    run_specific_test "$specific_service"
    exit $?
  fi
  
  # Check prerequisites
  echo "Checking prerequisites..."
  check_k6
  check_jq
  
  # Check services unless skip flag is set
  if [ "$skip_check" = false ]; then
    echo ""
    echo "Checking services..."
    local all_running=true
    for service in "${!SERVICES[@]}"; do
      if ! check_service "$service" "${SERVICES[$service]}"; then
        all_running=false
      fi
    done
    
    if [ "$all_running" = false ]; then
      echo ""
      echo -e "${YELLOW}Warning: Some services may not be running${NC}"
      echo "Do you want to continue? (y/n)"
      read -r response
      if [[ ! "$response" =~ ^[Yy]$ ]]; then
        echo "Test cancelled"
        exit 0
      fi
    fi
  fi
  
  echo ""
  echo -e "${BLUE}Starting tests...${NC}"
  
  local failed_tests=0
  local total_tests=${#SERVICES[@]}
  local passed_tests=0
  
  # Run tests in order
  for service in "user-service" "movie-service" "theater-service" "seat-service" "show-service" "notification-service" "reservation-service"; do
    if [ -n "${SERVICES[$service]}" ]; then
      if run_test "$service" "${SERVICES[$service]}"; then
        ((passed_tests++))
      else
        ((failed_tests++))
      fi
    fi
  done
  
  generate_summary
  
  echo ""
  echo -e "${GREEN}========================================${NC}"
  echo -e "${GREEN}Test Suite Completed${NC}"
  echo -e "${GREEN}========================================${NC}"
  echo "Total tests: $total_tests"
  echo -e "${GREEN}Passed: $passed_tests${NC}"
  if [ $failed_tests -gt 0 ]; then
    echo -e "${RED}Failed: $failed_tests${NC}"
  else
    echo -e "${GREEN}Failed: $failed_tests${NC}"
  fi
  
  if [ $failed_tests -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓ All tests completed successfully!${NC}"
    exit 0
  else
    echo ""
    echo -e "${RED}✗ Some tests failed${NC}"
    exit 1
  fi
}

# Run main function
main "$@"
