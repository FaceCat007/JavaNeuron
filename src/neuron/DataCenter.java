package neuron;
import java.util.*;
import facecat.topin.service.*;
import facecat.topin.core.*;

/*
* 服务中心
*/
public class DataCenter { 
    private static String m_userID = "";

    /*
    * 获取用户ID
    */
    public static String getUserID() {
            return m_userID;
    }

    /*
    * 设置用户ID
    */
    public static void setUserID(String value){
            m_userID = value;
    }

    private static String m_room = "000";

    /*
    * 获取房间号
    */
    public static String getRoom() {
            return m_room;
    }

    /*
    * 设置房间号
    */
    public static void setRoom(String value){
            m_room = value;
    }

    private static String m_icon = "";
    
    /*
    * 获取头像
    */
    public static String getIcon() {
            return m_icon;
    }

    /*
    * 设置头像
    */
    public static void setIcon(String value){
            m_icon = value;
    }

    private static String m_aesKey = "";

    /*
    * 获取加密密钥
    */
    public static String getAesKey() {
            return m_aesKey;
    }

    /*
    * 设置加密密钥
    */
    public static void setAesKey(String value){
            m_aesKey = value;
    }

    private static String m_code = "";

    /*
    * 获取备注
    */
    public static String getCode() {
            return m_code;
    }

    /*
    * 设置备注
    */
    public static void setCode(String value){
            m_code = value;
    }

    /*
    * 最大服务器连接数
    */
    public static final int MAXSERVERS = 1;
    
    /*
    * 通用请求ID
    */
    public static int getChatRequestID() {
            return 9999;
    }
    
    private static HostInfo m_hostInfo = new HostInfo();

    /*
    * 获取主机信息
    */
    public static HostInfo getHostInfo(){
            return m_hostInfo;
    }
    
    /*
    * 服务是否或者
    */
    public static boolean isAppAlive(){
        return true;
    }
    
    /*
    * 文件分隔符号
    */
    //public static String m_seperator = "/"; //Linux
    public static String m_seperator = "\\"; //Windows
    
    /*
    * 数据的数量
    */
    public static HashMap<String, Integer> m_datasCount = new HashMap<String, Integer>();

    /*
    * 保存数据
    */
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
     
    /*
    * 获取服务端
    */
    public static ChatServiceSV getServerChatService(){
         return m_serverChatService;
     }
     
    /*
    * 启动服务
    */
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
     
    /*
    * 获取程序路径
    */
    public static String getAppPath(){
        //return System.getProperty("user.dir").replace("\\", "/"); //Linux
        return System.getProperty("user.dir"); //Windows
    }
}
