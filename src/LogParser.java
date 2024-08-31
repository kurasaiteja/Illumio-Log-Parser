import java.io.*;
import java.util.*;

public class LogParser {

  private Map<CustomPair<Integer, String>, List<String>> lookupTable = new HashMap<>();

  private Map<String, Integer> tagCounts = new HashMap<>();
  private Map<CustomPair<Integer, String>, Integer> portProtocolCounts = new HashMap<>();
  private Map<Integer, String> protocolMap = new HashMap<>();  // Dynamic protocol mapping
  private int untaggedCount = 0;

  /**
   * Loads the lookup table from a text file. Each line of the file should contain a destination port, protocol,
   * and tag, in CSV format. If first line is header, it is skipped.
   *
   * @param filePath the path to the lookup table file.
   * @throws IOException
   */
  public void loadLookupTable(String filePath) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line = br.readLine();

      if (line != null && !isNumeric(line.split(",")[0].trim())) {
        line = br.readLine();
      }

      while (line != null) {
        String[] parts = line.trim().split(",");
        if (parts.length == 3) {
          try {
            int port = Integer.parseInt(parts[0].trim());
            String protocol = parts[1].trim().toLowerCase();
            String tag = parts[2].trim();
            lookupTable.computeIfAbsent(new CustomPair<>(port, protocol), k -> new ArrayList<>()).add(tag);
          } catch (NumberFormatException e) {
            System.err.println("Invalid port number in lookup table: " + parts[0]);
          }
        }
        else{
          continue;
        }
        line = br.readLine();
      }
    }
  }

  private boolean isNumeric(String str) {
    return str.matches("\\d+");
  }

  /**
   * Loads the protocol mapping from a CSV file.
   * The CSV file should have 'Decimal' as the first column and 'Keyword' as the second column.
   *
   * @param filePath the path to the protocol mapping file.
   * @throws IOException
   */
  public void loadProtocolMap(String filePath) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filePath));
    String line;
    br.readLine();
    while ((line = br.readLine()) != null) {
      line = line.trim();
      if (line.isEmpty()) continue;

      String[] parts = line.split(",");
      if (parts.length < 2) {
        continue;
      }
      try {
        int protocolNumber = Integer.parseInt(parts[0].trim());
        String protocolName = parts[1].trim().toLowerCase();
        protocolMap.put(protocolNumber, protocolName);
      } catch (NumberFormatException e) {
        System.out.println("Corrupt protocol number skipped: " + parts[0]);
      }
    }
    br.close();
  }

  /**
   * Parses the flow logs from a file, maps each record to a tag using the lookup table,
   * and updates the counts of tags and port/protocol combination's hashmaps respectively.
   *
   * @param filePath the path to the logs file.
   * @throws IOException
   */

  public void parseFlowLogs(String filePath) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filePath));
    String line;
    while ((line = br.readLine()) != null) {
      line = line.trim();
      if (line.isEmpty()) continue;

      String[] parts = line.split("\\s+");
      if (parts.length < 14) {
        continue;
      }
      try {
        int srcPort = Integer.parseInt(parts[5].trim());
        int dstPort = Integer.parseInt(parts[6].trim());
        int protocolNumber = Integer.parseInt(parts[7].trim());
        String protocol = protocolMap.getOrDefault(protocolNumber, "uncommon").toLowerCase();

        CustomPair<Integer, String> dstPortProtocolPair = new CustomPair<>(dstPort, protocol.toLowerCase());
        CustomPair<Integer, String> srcPortProtocolPair = new CustomPair<>(srcPort, protocol.toLowerCase());
        List<String> tags = lookupTable.getOrDefault(dstPortProtocolPair, Arrays.asList("Untagged"));

        for (String tag : tags) {
          tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
        }

        portProtocolCounts.put(srcPortProtocolPair, portProtocolCounts.getOrDefault(srcPortProtocolPair, 0) + 1);
        portProtocolCounts.put(dstPortProtocolPair, portProtocolCounts.getOrDefault(dstPortProtocolPair, 0) + 1);
      } catch (NumberFormatException e) {
        System.err.println("Invalid port or protocol number in log entry: " + Arrays.toString(parts));
        continue;  // Skip this line and move to the next one
      }

    }
    br.close();
  }

  /**
   * Saves the results (tag counts and port/protocol combination counts) to an output file.
   * Checks if output file exists and creates/overrides file content.
   * @param outputFilePath the path to the output file.
   * @throws IOException
   */
  public void saveResults(String outputFilePath) throws IOException {
    File outputFile = new File(outputFilePath);
    if (!outputFile.exists()) {
      outputFile.createNewFile();
    }

    BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

    bw.write("Tag Counts:\n");
    bw.write("Tag,Count\n");
    for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
      bw.write(entry.getKey() + "," + entry.getValue() + "\n");
    }

    bw.write("\nPort/Protocol Combination Counts:\n");
    bw.write("Port,Protocol,Count\n");
    for (Map.Entry<CustomPair<Integer, String>, Integer> entry : portProtocolCounts.entrySet()) {
      CustomPair<Integer, String> key = entry.getKey();
      bw.write(key.x + "," + key.y + "," + entry.getValue() + "\n");
    }

    bw.close();
  }

  public static void main(String[] args) {
    // Default file paths
    String lookupFilePath = "lookup_table.csv";
    String logFilePath = "logs.txt";
    String protocolMapFilePath = "protocol_map.csv";
    String outputFilePath = "output_results.csv";

    // Parse named arguments
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--lookup":
          if (i + 1 < args.length) lookupFilePath = args[++i];
          break;
        case "--logs":
          if (i + 1 < args.length) logFilePath = args[++i];
          break;
        case "--protocol":
          if (i + 1 < args.length) protocolMapFilePath = args[++i];
          break;
        case "--output":
          if (i + 1 < args.length) outputFilePath = args[++i];
          break;
        default:
          System.out.println("Unknown argument: " + args[i]);
          return;
      }
    }

    LogParser parser = new LogParser();
    try {
      parser.loadLookupTable(lookupFilePath);
      parser.loadProtocolMap(protocolMapFilePath);
      parser.parseFlowLogs(logFilePath);
      parser.saveResults(outputFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}