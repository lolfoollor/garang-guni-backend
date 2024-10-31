#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <input_file> <failure_percentage>"
    exit 1
fi

input_file=$1
failure_percentage=$2

fail_count=$(grep -c "false" "$input_file")
pass_count=$(grep -c "true" "$input_file")
generate_item_count=$(grep -c "generateRandomItem" "$input_file")
randomize_variables_count=$(grep -c "Randomize Variables" "$input_file")
pass_count_without_misc=$((pass_count - generate_item_count - randomize_variables_count ))

total_count=$((fail_count + pass_count_without_misc))

failure_rate=$(echo "scale=2; $fail_count / $total_count * 100" | bc)

echo "Failure Rate: $failure_rate%"

if (( $(echo "$failure_rate > $failure_percentage" | bc -l) )); then
    echo "Failure rate exceeds $failure_percentage%! Failing the job."
    exit 1
fi