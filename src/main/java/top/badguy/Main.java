package top.badguy;

import top.badguy.aliyun.AliyunDDNS;
import top.badguy.dao.NetworkStatsDAO;
import top.badguy.parser.CFCSVParser;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

//    public static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void main(String[] args) throws IOException {
        try {
            YamlConfigLoader.init();
            CFCSVParser cfcsvParser = new CFCSVParser();
            AliyunDDNS aliyunDDNS = new AliyunDDNS();
//        cfcsvParser.runCloudflareST();
            checkIp(aliyunDDNS, cfcsvParser);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            // 延迟0秒后开始执行，然后每隔1小时执行一次
            scheduler.scheduleAtFixedRate(() -> {
                // 这里放需要定时执行的任务代码
                checkIp(aliyunDDNS, cfcsvParser);
            }, 0, 6, TimeUnit.HOURS);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in main");
        }
    }

    private static void checkIp(AliyunDDNS aliyunDDNS, CFCSVParser cfcsvParser) {
        System.out.println("Executing task at " + System.currentTimeMillis());
        try {
            String orgIp = aliyunDDNS.getSubDomainIp(YamlConfigLoader.RR+"."+YamlConfigLoader.DOMAIN);
            double ping = aliyunDDNS.ping(orgIp, 20);
            if (ping < 200) {
                System.out.println("ping值小于200，不需要更换ip");
                return;
            }
            System.out.println("ping值大于200，更换ip");
            cfcsvParser.runCloudflareST();
            // TODO: 2024/7/18 改为从数据库中取 
            String ip = cfcsvParser.readCSVIP();
            System.out.println("改变 ip 为：" + ip);
            aliyunDDNS.changeIp(YamlConfigLoader.RR, YamlConfigLoader.DOMAIN, ip);
        } catch (ExecutionException | InterruptedException | IOException e) {
            System.out.println("Error in checkIp");
            throw new RuntimeException(e);
        } 
    }


}