# Log Parser

This project reads log data and analyses each entry with the help of a lookup table. It generates an output file(csv) with two main sections:
1. **Tag Counts** - Contains counts of each tag.
2. **Port/Protocol Combination Counts** - Contains counts of port/protocol combinations.

## Files

1. **logs.txt**: The input file containing flow log data. **(Supports only version 2 and skips corrupt/other version entries).**
2. **lookup_table.csv**: The text file with columns `dstport`, `protocol`, `tag`.
3. **protocol_map.csv**: The CSV file mapping protocol numbers to protocol names.
4. **output_results.csv**: The output file containing tag counts and port/protocol combination counts.

## Usage

### Compilation and Execution

Navigate to the `/src` directory containing Java Files.
1. **Compile the Java Files**:
   - Compile all the `.java` files in the directory using:
     ```bash
     javac *.java
     ```

2. **Run the Program**:
    - Run the program with default filenames:
      ```bash
      java LogParser
      ```

    - Or, run the program with custom filenames by specifying command-line arguments:
      ```bash
      java LogParser --lookup custom_lookup.txt --logs custom_logs.txt --protocol custom_protocol_map.csv --output custom_results.csv
      ```

### Command-Line Arguments

The program accepts up to four optional command-line arguments, allowing for flexibility:

1. **--lookup**: Path to the lookup table file (default: `lookup_table.txt`).
2. **--logs**: Path to the flow logs text file (default: `logs.txt`).
3. **--protocol**: Path to the protocol map CSV file (default: `protocol_map.csv`).
4. **--output**: Path to the output CSV file (default: `output_results.csv`).

## Assumptions
I am proficient in Java, and hence decided to implement this project in Java. This project aimed at utilizing no third party libraries and hence I came up with below assumptions.
### port/protocol combination maps to multiple tags

If a port/protocol combination maps to multiple tags, all matching tags are stored and counted.
### Downloading the Protocol Map File

Instead of hard coding the mapping for protocol numbers to protocol names, this project uses up-to-date version of this file from the IANA website:

- [Download Protocol Numbers from IANA](https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml)

The csv file consists of 2 columns Protocol Number and Name. This ensures that any new protocols can be easily updated in the system, providing flexibility.

### Handling Both Source and Destination Ports

In the problem description, the expected output for Port/Protocol counts was ambiguous about whether to consider only the destination port or both source and destination ports as the output did not satisy any of them. To address this issue, the program considers both source and destination ports.

### CustomPair Class

The `CustomPair` class was written to efficiently handle pairs of values, specifically `(port, protocol)` pairs. This class is crucial for correctly mapping and counting unique port/protocol combinations.

### Time Complexity Analysis

- **loadLookupTable**: O(N), where N is the number of lines in the lookup table file.
- **loadProtocolMap**: O(M), where M is the number of lines in the protocol map file.
- **parseFlowLogs**: O(L), where L is the number of lines in the logs file. Each line involves a constant-time operation to check and update the relevant maps, so the overall time complexity is linear with respect to the number of log entries.
- **saveResults**: O(T + P), where T is the number of unique tags and P is the number of unique port/protocol combinations.

### Advantages of Named Arguments

By implementing named arguments, the assumption is that program becomes more user-friendly and adaptable to different use cases, making it suitable for both quick tests and more detailed analyses.

### Testing 
1. Tested the scenario where data is correct in 
2. Tested the scenario where there is no matching dstport and protocol combination in the lookup table with the log data. The output shows Untagged Count and Respective entries in port/protocol counts.
3. Tested case sensitivity in the protocol field of the lookup table. The tag count and port/protocol count are displayed correctly.
4. Tested empty log file as input. The output file has no tag and port/protocol counts.
5. Tested with corrupt entries in log file. The output showed tag counts and port/protocol counts only for valid entries pertaining to version 2.
6. Tested with empty look up data. The output showed Untagged tag counts only and corresponding port/protocol entries accordingly.