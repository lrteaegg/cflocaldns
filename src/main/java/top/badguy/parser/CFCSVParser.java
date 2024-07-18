package top.badguy.parser;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import top.badguy.dao.NetworkStatsDAO;
import top.badguy.enums.BooleanEnum;
import top.badguy.exception.BizException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class CFCSVParser {

    public String readCSVIP() {
        File configFile = new File(System.getProperty("user.dir") + File.separator + "result.csv");
        try (
                InputStream resourceAsStream = new FileInputStream(configFile);
                Reader reader = new InputStreamReader(resourceAsStream);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);) {
            List<CSVRecord> records = csvParser.getRecords();
            NetworkStatsDAO networkStatsDAO = NetworkStatsDAO.getInstance();
            for (int i = 1; i < records.size(); i++) {
                if (StringUtils.isBlank(records.get(i).get(0))) {
                    break;
                }
                //  2024/7/18 存储库
                String ip = records.get(i).get(0);
                Float averageLatency = Float.valueOf(records.get(i).get(4));
                networkStatsDAO.saveOrUpdateNetworkStats(ip, averageLatency, BooleanEnum.FALSE);
            }
            CSVRecord firstRow = records.get(1);
            String ip = firstRow.get(0);
            return ip;
        } catch (IOException e) {
            System.out.println("Error in reading CSV file");
            throw new BizException(e);
        }
    }

    /**
     * 执行 CloudflareST
     */
    public void runCloudflareST() throws IOException {
        BufferedReader reader = null;
        Process process = null;
        try {
            System.out.println(new File(System.getProperty("user.dir")));
            String cloudflareST = System.getProperty("os.name").toLowerCase().contains("windows") ? "CloudflareST.exe" : "CloudflareST";
//            String charset = System.getProperty("os.name").toLowerCase().contains("windows") ? "GBK" : StandardCharsets.UTF_8.name();
            ProcessBuilder processBuilder = new ProcessBuilder("./" + cloudflareST);
            processBuilder.redirectErrorStream(true);
//            Process process = Runtime.getRuntime().exec("./" + cloudflareST);
            process = processBuilder.start();


            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8.name()));
            String line;

            // 读取ping命令的输出，找到响应时间
            while ((line = reader.readLine()) != null) {
                if (line.contains("可用") || line.contains("0 / 10")) {
                    continue;
                }
                System.out.println(line);
            }
            process.waitFor();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            reader.close();
            process.destroy();
        }

    }

}
