package top.badguy.aliyun;


import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.alidns20150109.AsyncClient;
import com.aliyun.sdk.service.alidns20150109.models.*;
import com.google.gson.Gson;
import darabonba.core.client.ClientOverrideConfiguration;
import org.apache.commons.math3.distribution.NormalDistribution;
import top.badguy.YamlConfigLoader;
import top.badguy.enums.TelecomEnum;
import top.badguy.exception.BizException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AliyunDDNS {

    public static final StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
            .accessKeyId(YamlConfigLoader.ACCESS_KEY_ID)
            .accessKeySecret(YamlConfigLoader.ACCESS_KEY_SECRET)
            .build());

    public static AsyncClient getClient() {
        return AsyncClient.builder()
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("alidns.cn-hangzhou.aliyuncs.com")
                )
                .build();
    }

    public void getDNSRecords() throws ExecutionException, InterruptedException {
        AsyncClient client = getClient();
        // Parameter settings for API request
        DescribeDomainsRequest describeDomainsRequest = DescribeDomainsRequest.builder()
                .build();

        CompletableFuture<DescribeDomainsResponse> response = client.describeDomains(describeDomainsRequest);
        // Synchronously get the return value of the API request
        DescribeDomainsResponse resp = response.get();
        DescribeDomainsResponseBody.Domains domains = resp.getBody().getDomains();
        for (DescribeDomainsResponseBody.Domain domain : domains.getDomain()) {
            System.out.println(domain.getDomainName());
        }

    }

    /**
     * 获取指定域名的 ip
     */
    public String getSubDomainIp(String domain) throws ExecutionException, InterruptedException {
        System.out.println(String.format("获取[%s]的ip", domain));
        AsyncClient client = getClient();
        // Parameter settings for API request
        DescribeSubDomainRecordsRequest describeSubDomainRecordsRequest = DescribeSubDomainRecordsRequest.builder()
                .subDomain(domain)
                // 电信 telecom，移动 mobile，联通 unicom
                .line(TelecomEnum.TELECOM.getValue())
                .build();

        CompletableFuture<DescribeSubDomainRecordsResponse> response = client.describeSubDomainRecords(describeSubDomainRecordsRequest);
        // Synchronously get the return value of the API request
        DescribeSubDomainRecordsResponse resp = response.get();
//        DescribeDomainRecordsResponseBody.DomainRecords domainRecords = resp.getBody().getDomainRecords();
        System.out.println(new Gson().toJson(resp));
        return resp.getBody().getDomainRecords().getRecord().get(0).getValue();
    }

    /**
     * ping 值，取正态分布的值
     *
     * @param host
     * @return
     */
    public double ping(String host) {
        int numPings = 20;

        List<Double> pingTimes = new ArrayList<>();

        // 执行ping命令并解析输出
        try {
            for (int i = 0; i < numPings; i++) {
                ProcessBuilder processBuilder;
                String charset = System.getProperty("os.name").toLowerCase().contains("windows") ? "GBK" : StandardCharsets.UTF_8.name();
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    // 在Windows系统上，通过cmd执行ping命令
                    processBuilder = new ProcessBuilder("cmd.exe", "/c", "ping", "-n", "1", host);
                } else {
                    // 在其他系统上，通过bash或者sh执行ping命令
                    processBuilder = new ProcessBuilder("ping", "-c", "1", host);
                }
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
                String line;
                double time = -1.0;

                // 读取ping命令的输出，找到响应时间
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains("请求超时") || line.contains("Request timed out") || line.contains("time 0ms")){
                        time = 400;
                        break;
                    }

                    if (line.contains("time=") || line.contains("时间=")) {
                        int startIndex = line.indexOf("time=") != -1 ? line.indexOf("time=") + 5 : line.indexOf("时间=") + 3;
                        String timeStr = line.substring(startIndex, startIndex + 3);
                        time = Double.parseDouble(timeStr);
                        break;
                    }
                }

                reader.close();

                if (time != -1.0) {
                    pingTimes.add(time);
                }
            }

            // 计算最大值和最小值
            double maxPing = Collections.max(pingTimes);
            double minPing = Collections.min(pingTimes);

            System.out.println("Maximum Ping Time: " + maxPing + " ms");
            System.out.println("Minimum Ping Time: " + minPing + " ms");

            // 计算正态分布的值
            double mean = calculateMean(pingTimes);
            double variance = calculateVariance(pingTimes);
            NormalDistribution normalDistribution = new NormalDistribution(mean, Math.sqrt(variance));
            double normalValue = normalDistribution.sample();

            System.out.println("Value from Normal Distribution: " + normalValue);
            return normalValue;
        } catch (IOException e) {
            throw new BizException(e);
        }
    }


    // 计算平均值
    private static double calculateMean(List<Double> data) {
        double sum = 0.0;
        for (Double num : data) {
            sum += num;
        }
        return sum / data.size();
    }

    // 计算方差
    private static double calculateVariance(List<Double> data) {
        double mean = calculateMean(data);
        double temp = 0.0;
        for (Double num : data) {
            temp += (num - mean) * (num - mean);
        }
        return temp / (data.size() - 1);
    }


    /**
     * 改变 ip
     */
    public void changeIp(String rr, String domain, String value) throws ExecutionException, InterruptedException {
        AsyncClient client = getClient();
        // 获取记录 id
        String recordId = this.getRecordId(rr + "." + domain);
        // Parameter settings for API request
        UpdateDomainRecordRequest updateDomainRecordRequest = UpdateDomainRecordRequest.builder()
                .recordId(recordId)
                .rr(rr)
                .type("A")
                .value(value)
                // 只处理电信线路
                .line(TelecomEnum.TELECOM.getValue())
                .build();

        CompletableFuture<UpdateDomainRecordResponse> response = client.updateDomainRecord(updateDomainRecordRequest);
        // Synchronously get the return value of the API request
        UpdateDomainRecordResponse resp = response.get();
        System.out.println(new Gson().toJson(resp));
    }

    /**
     * 获取记录 id
     */
    public String getRecordId(String subDomain) throws ExecutionException, InterruptedException {
        AsyncClient client = getClient();
        // Parameter settings for API request
        DescribeSubDomainRecordsRequest describeSubDomainRecordsRequest = DescribeSubDomainRecordsRequest.builder()
                .subDomain(subDomain)
                .build();

        CompletableFuture<DescribeSubDomainRecordsResponse> response = client.describeSubDomainRecords(describeSubDomainRecordsRequest);
        // Synchronously get the return value of the API request
        DescribeSubDomainRecordsResponse resp = response.get();
        return resp.getBody().getDomainRecords().getRecord().get(0).getRecordId();
    }
}
