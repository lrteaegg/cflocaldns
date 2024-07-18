package top.badguy.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class NetworkStatsDAO {

    // 单例模式
    private static NetworkStatsDAO instance = new NetworkStatsDAO();

    public NetworkStatsDAO() {

    }

    public static NetworkStatsDAO getInstance() {
        return instance;
    }



    public String getNetworkStats() {
        try (Statement statement = MysqlConfig.getStatement()){
            ResultSet resultSet = statement.executeQuery("SELECT * FROM network_stats");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("ip_address"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return "NetworkStats";
    }

    public void insertNetworkStats(String ipAddress, Float averageLatency, int isUsed) {
        try (Statement statement = MysqlConfig.getStatement()){
            statement.executeUpdate("INSERT INTO network_stats (ip_address, average_latency, is_used) VALUES ('" + ipAddress + "', " + averageLatency + ", " + isUsed + ")");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateNetworkStats(String ipAddress, Float averageLatency, int isUsed) {
        try (Statement statement = MysqlConfig.getStatement()){
            statement.executeUpdate("UPDATE network_stats SET average_latency = " + averageLatency + ", is_used = " + isUsed + " WHERE ip_address = '" + ipAddress + "'");
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
