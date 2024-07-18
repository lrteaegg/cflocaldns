package top.badguy.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static top.badguy.utils.TimeUtil.getTimeString;

public class NetworkStatsDAO {

    // 单例模式
    private static NetworkStatsDAO instance = new NetworkStatsDAO();

    public NetworkStatsDAO() {

    }

    public static NetworkStatsDAO getInstance() {
        return instance;
    }



    public List<String> getNetworkStats(int avgLatency) {
        List<String> ipList = new ArrayList<>();
        try (Statement statement = MysqlConfig.getStatement()){
            ResultSet resultSet = statement.executeQuery("SELECT * FROM network_stats where average_latency < " + avgLatency +" order by updated_at desc limit 50");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("ip_address"));
                ipList.add(resultSet.getString("ip_address"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return ipList;
    }

    public void insertNetworkStats(String ipAddress, Float averageLatency, int isUsed) {
        try (Statement statement = MysqlConfig.getStatement()){
            String timeString = getTimeString();
            statement.executeUpdate("INSERT INTO network_stats (ip_address, average_latency, is_used, updated_at) " +
                    "VALUES ('" + ipAddress + "', " + averageLatency + ", " + isUsed + ", '" + timeString +"')");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateNetworkStats(String ipAddress, Float averageLatency, int isUsed) {
        String timeString = getTimeString();
        try (Statement statement = MysqlConfig.getStatement()){
            statement.executeUpdate("UPDATE network_stats SET average_latency = " + averageLatency + ", is_used = " + isUsed  +
                    ", updated_at = '" + timeString +
                    "' WHERE ip_address = '" + ipAddress + "'");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteNetworkStats(String ipAddress) {
        try (Statement statement = MysqlConfig.getStatement()){
            statement.executeUpdate("DELETE FROM network_stats WHERE ip_address = '" + ipAddress + "'");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveOrUpdateNetworkStats(String ipAddress, Float averageLatency, int isUsed) {
        try (Statement statement = MysqlConfig.getStatement()){
            ResultSet resultSet = statement.executeQuery("SELECT * FROM network_stats WHERE ip_address = '" + ipAddress + "'");
            if (resultSet.next()) {
                updateNetworkStats(ipAddress, averageLatency, isUsed);
            } else {
                insertNetworkStats(ipAddress, averageLatency, isUsed);
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
