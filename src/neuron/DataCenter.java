package neuron;
import java.util.*;
import facecat.topin.service.*;
import facecat.topin.core.*;

public class DataCenter { 
    private static String m_userID = "";

    /// <summary>
    /// 获取或设置用户ID
    /// </summary>
    public static String getUserID() {
            return m_userID;
    }

    public static void setUserID(String value){
            m_userID = value;
    }

    private static String m_room = "000";

    /// <summary>
    /// 房间号
    /// </summary>
    public static String getRoom() {
            return m_room;
    }

    public static void setRoom(String value){
            m_room = value;
    }

    private static String m_icon = "";

    /// <summary>
    /// 获取或设置图标
    /// </summary>
    public static String getIcon() {
            return m_icon;
    }

    public static void setIcon(String value){
            m_icon = value;
    }

    private static String m_aesKey = "";

    /// <summary>
    /// 加密密钥
    /// </summary>
    public static String getAesKey() {
            return m_aesKey;
    }

    public static void setAesKey(String value){
            m_aesKey = value;
    }

    private static String m_code = "";

    /// <summary>
    /// 获取或设置备注
    /// </summary>
    public static String getCode() {
            return m_code;
    }

    public static void setCode(String value){
            m_code = value;
    }

    /// <summary>
    /// 最大服务器连接数
    /// </summary>
    public static final int MAXSERVERS = 1;

    /// <summary>
    /// 区块链通用请求ID
    /// </summary>
    public static int getChatRequestID() {
            return 9999;
    }
    
    private static HostInfo m_hostInfo = new HostInfo();

    public static HostInfo getHostInfo(){
            return m_hostInfo;
    }
    
    public static boolean isAppAlive(){
        return true;
    }
    
    //public static String m_seperator = "/"; //Linux
    public static String m_seperator = "\\"; //Windows
    
    /// <summary>
    /// 数据的数量
    /// </summary>
    public static HashMap<String, Integer> m_datasCount = new HashMap<String, Integer>();

    /// <summary>
    /// 保存数据数量
    /// </summary>
    /// <param name="key"></param>
    /// <returns></returns>
    public static boolean saveDataCount(String key) {
            boolean exist = false;
            synchronized (m_datasCount) {
                    if (m_datasCount.containsKey(key)) {
                            m_datasCount.put(key, m_datasCount.get(key) + 1);
                            exist = true;
                    } else {
                            m_datasCount.put(key, 1);
                    }
            }
            return exist;
    }
     
     private static ChatServiceSV m_serverChatService;
     
     public static ChatServiceSV getServerChatService(){
         return m_serverChatService;
     }
     
     public static void startService(){
         m_hostInfo.m_localHost = "127.0.0.1";
         m_hostInfo.m_localPort = 16665;
         m_serverChatService = new ChatServiceSV();
        FCServerService.addService(m_serverChatService);
	m_serverChatService.setPort(m_hostInfo.m_localPort);
	int socketID = FCServerService.startServer(m_serverChatService.getPort(), new byte[]{99,104,97,116});
        m_serverChatService.setSocketID(socketID);
        m_serverChatService.startService();
     }
     
     public static String getAppPath(){
         //return System.getProperty("user.dir").replace("\\", "/"); //Linux
         return System.getProperty("user.dir"); //Windows
     }
}
