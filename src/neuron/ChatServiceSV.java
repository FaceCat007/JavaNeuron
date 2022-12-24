package neuron;
import facecat.topin.chart.BaseShapeZOrderASC;
import facecat.topin.service.*;
import facecat.topin.core.*;
import iKv.KVDataBase2;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.IOException;

/*
* 多端互动服务端
*/
public class ChatServiceSV extends FCServerService {
    /*
    * 构造函数
    */
    public ChatServiceSV(){
        m_token = FCTran.getGuid();
        setServiceID(SERVICEID_CHAT);
        setCompressType(COMPRESSTYPE_NONE);
        String path = DataCenter.getAppPath() + DataCenter.m_seperator + "cdata";
        if(!FCFile.isDirectoryExist(path)){
            FCFile.createDirectory(path);
        }
        
        String cloudDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "clouddata";
        if(!FCFile.isDirectoryExist(cloudDataDir)){
            FCFile.createDirectory(cloudDataDir);
        }
        String globalDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "globaldata";
        if(!FCFile.isDirectoryExist(globalDataDir)){
            FCFile.createDirectory(globalDataDir);
        }
    }
    
    /*
    * 区块链服务ID
    */
    public static final int SERVICEID_CHAT = 19999;

    /*
    * 主机信息
    */
    public static final int FUNCTIONID_GETHOSTS = 1;

    /*
    * 广播功能ID
    */
    public static final int FUNCTIONID_SENDALL = 3;

    /*
    * 进入
    */
    public static final int FUNCTIONID_ENTER = 6;
    
    /*
    * 聊天记录
    */
    public static final int FUNCTIONID_RECORD = 7;
    
    /*
    * 获取状态
    */
    public static final int FUNCTIONID_STATE = 8;
    
    /*
    * 操作云文件
    */
    public static final int FUNCTIONID_CLOUDFILE = 9;
    
    /*
    * 发送非加密消息
    */
    public static final int FUNCTIONID_SENDNOENCRPTY = 10;
    
    /*
    * 是否发送给临近节点
    */
    public boolean m_sendToNear;
    
    /*
    * 端口号
    */
    public int m_port = 16665;
    
    /*
    * 唯一标识
    */
    public String m_token;
    
    /*
    * 节点集合
    */
    public ArrayList<ChatHostInfo> m_serverHosts = new ArrayList<ChatHostInfo>();
    
    /*
    * 连接信息
    */
    public HashMap<Integer, ChatHostInfo> m_socketIDs = new HashMap<Integer, ChatHostInfo>();
    
    /*
    * 要发送的消息
    */
    public ArrayList<FCMessage> m_sendMessages = new ArrayList<FCMessage>();
    
    /*
    * 获取端口号
    */
    public int getPort(){
        return m_port;
    }

    /*
    * 设置端口号
    */
    public void setPort(int port){
        m_port = port;
    }

    /*
    * 获取唯一标识
    */
    public String getToken(){
	return m_token;
    }

    /*
    * 设置唯一标识
    */
    public void setToken(String token){
	m_token = token;
    }

    /*
    * 添加服务端主机
    * hostInfo 主机信息
    */
    public void addServerHosts(ChatHostInfo hostInfo){
        synchronized(m_serverHosts){
		int serverHostsSize = m_serverHosts.size();
		boolean contains = false;
		for(int i = 0; i < serverHostsSize; i++){
                    ChatHostInfo oldHostInfo = m_serverHosts.get(i);
                    if(oldHostInfo.m_ip.equals(hostInfo.m_ip) && oldHostInfo.m_serverPort == hostInfo.m_serverPort){
                        contains = true;
                        break;
                    }
		}
		if(!contains){
			m_serverHosts.add(hostInfo);
		}
        }
    }

    /*
    * 检查心跳包
    */
    public void checkAlive(){
        while(DataCenter.isAppAlive()){
            synchronized(m_socketIDs){
                ArrayList<Integer> socketIDs = new ArrayList<Integer>();
                for(Integer sIter : m_socketIDs.keySet()){
                    socketIDs.add(sIter);
                }
                int socketIDsSize = socketIDs.size();
                for(int i = 0; i < socketIDsSize; i++){
                    keepAlive(socketIDs.get(i));
                }               
            }
            try{
                Thread.sleep(6000);
            }catch(Exception ex){

            }
        }
    }
    
    /*
    * 获取历史记录
    * message 消息
    */
    public int record(FCMessage message){
        try{
            FCBinary br = new FCBinary();
            br.write(message.m_body, message.m_bodyLength);
            double startDate = br.readDouble();
            double endDate = br.readDouble();
            if(startDate == 0 && endDate != 0){
                startDate = endDate - 86400 * 10;
            }
            String userID = br.readString();
            String room = br.readString();
            br.close();
            int startDay = 0, endDay = 0;
            if(true){
                Calendar calendar = FCTran.numToDate(startDate);
                startDay = calendar.get(Calendar.YEAR) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH);;
            }
            if(true){
                Calendar calendar = FCTran.numToDate(endDate);
                endDay = calendar.get(Calendar.YEAR) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH);;
            }
            ArrayList<ChatData> chatDatas = new ArrayList<ChatData>();
            for(int i = startDay; i <= endDay; i++){
                    String path = DataCenter.getAppPath() + DataCenter.m_seperator  + "cdata" + DataCenter.m_seperator  + FCTran.intToStr(i);
                    if(FCFile.isDirectoryExist(path)){
                        int kID = KVDataBase2.initDB(path, "chat");
                        int valueCount = KVDataBase2.getValueCount(kID);
                        if(valueCount > 0){
                                for(int j = 0; j < valueCount; j++){
                                        byte []str = KVDataBase2.getValue(kID, j);
                                        if(str != null){
                                                ChatData chatData = new ChatData();
                                                FCBinary br2 = new FCBinary();
                                                br2.write(str, str.length);
                                                chatData.m_time = br2.readDouble();
                                                chatData.m_key = br2.readString();
                                                chatData.m_tokens = br2.readString();
                                                chatData.m_from = br2.readString();
                                                chatData.m_to = br2.readString();
                                                chatData.m_room = br2.readString();
                                                chatData.m_content = br2.readString();
                                                if(room.equals(chatData.m_room) && chatData.m_to.indexOf(userID) != -1){
                                                        if(chatData.m_time > startDate && chatData.m_time <= endDate){
                                                                    chatDatas.add(chatData);
                                                            }
                                                }
                                                br2.close();
                                        }
                                }
                        }
                    }
            }
            FCBinary bw = new FCBinary();
            bw.writeInt(chatDatas.size());
            for(int i = 0; i < chatDatas.size(); i++){
                    ChatData chatData = chatDatas.get(i);
                    bw.writeDouble(chatData.m_time);
                    bw.writeString(chatData.m_key);
                    bw.writeString(chatData.m_tokens);
                    bw.writeString(chatData.m_from);
                    bw.writeString(chatData.m_to);
                    bw.writeString(chatData.m_room);
                    bw.writeString(chatData.m_content);
            }
            byte[] bytes = bw.getBytes();
            bw.close();
            FCMessage sendMsg = new FCMessage(getServiceID(), FUNCTIONID_RECORD, DataCenter.getChatRequestID(), 0, 0, getCompressType(), bytes.length, bytes);
            sendMsg.m_socketID = message.m_socketID;
            return super.send(sendMsg);
        }catch(Exception ex){
            return -1;
        }
    }

    /*
    * 进入
    * message 消息
    */
    public int enter(FCMessage message){
        try{
            int rtnSocketID = message.m_socketID;
            FCBinary br = new FCBinary();
            br.write(message.m_body, message.m_bodyLength);
            String ip = "";
            int port = br.readInt();
            int type = br.readInt();
            String token = br.readString();
            String userID = br.readString();
            String cloudDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "clouddata" + DataCenter.m_seperator + userID;
            if(!FCFile.isDirectoryExist(cloudDataDir)){
                FCFile.createDirectory(cloudDataDir);
            }
            String userName = br.readString();
            String icon = br.readString();
            String room = br.readString();
            String exInfo = br.readString();
            ArrayList<Integer> sendSocketIDs = new ArrayList<Integer>();
            ArrayList<ChatHostInfo> hostInfos = new ArrayList<ChatHostInfo>();
            synchronized(m_socketIDs){
                if(m_socketIDs.containsKey(rtnSocketID)){
                    ChatHostInfo chi = m_socketIDs.get(rtnSocketID);
                    ip = chi.m_ip;
                    if(port == 0){
                        port = chi.m_port;
                    }
                    chi.m_token = token;
                    chi.m_serverPort = port;
                    chi.m_type = type;
                    chi.m_userID = userID;
                    chi.m_code = userName;
                    chi.m_icon = icon;
                    chi.m_room = room;
                    chi.m_exInfo = exInfo;
                    hostInfos.add(chi);
                    if(type == 0){
                        System.out.print("客户端接入:" + ip + ":" + FCTran.intToStr(port) + "," + userID + "," + FCTran.intToStr(rtnSocketID));
                    }else{
                        System.out.print("节点服务端接入:" + ip + ":" + FCTran.intToStr(port));
                    }
                    for(Integer sIter : m_socketIDs.keySet()){
                            if (sIter != rtnSocketID) {
                                    ChatHostInfo gs = m_socketIDs.get(sIter);
                                    if(gs.m_serverPort != 0){
                                            if (gs.m_type == 0) {
                                                    if (gs.m_room.equals(room)) {
                                                            sendSocketIDs.add(sIter);
                                                    }
                                            }
                                            else if (gs.m_type == 1) {
                                                    if (type == 1) {
                                                            sendSocketIDs.add(sIter);
                                                    }
                                            }
                                    }
                            }
                    }
                }
            }
            int sendSocketIDsSize = (int)sendSocketIDs.size();
            if (sendSocketIDsSize > 0) {
                sendHostInfos(sendSocketIDs, 1, hostInfos);
            }

            HashMap<String, ChatHostInfo> allHostInfos = new HashMap<String, ChatHostInfo>();
            synchronized(m_socketIDs){
                for(Integer sIter : m_socketIDs.keySet()){
                    int sid = sIter;
                    if (sid != rtnSocketID) {
                            ChatHostInfo hostInfo = m_socketIDs.get(sid);
                            if(hostInfo.m_serverPort != 0){
                                    String key = hostInfo.getKey();
                                    if (hostInfo.m_type == 0) {
                                            if (hostInfo.m_room.equals(room)) {
                                                    allHostInfos.put(key, hostInfo);
                                            }
                                    } else {
                                            allHostInfos.put(key, hostInfo);
                                    }
                            }
                    }
                }
                if(DataCenter.getHostInfo().m_localHost.length() > 0) {
                        ChatHostInfo localHostInfo = new ChatHostInfo();
                        localHostInfo.m_ip = DataCenter.getHostInfo().m_localHost;
                        localHostInfo.m_serverPort = DataCenter.getHostInfo().m_localPort;
                        localHostInfo.m_type = 1;
                        localHostInfo.m_token = getToken();
                        String key = localHostInfo.getKey();
                        allHostInfos.put(key, localHostInfo);
                }
            }
            synchronized(m_serverHosts){
                for(ChatHostInfo  serverHost : m_serverHosts){
                    String key = serverHost.getKey();
                    allHostInfos.put(key, serverHost);
                }
            }
            ArrayList<Integer> rtnSocketIDs = new ArrayList<Integer>();
            rtnSocketIDs.add(rtnSocketID);
            ArrayList<ChatHostInfo> sendAllHosts = new ArrayList<ChatHostInfo>();
            for(ChatHostInfo hostInfo : allHostInfos.values()){
                    sendAllHosts.add(hostInfo);
            }
            sendHostInfos(rtnSocketIDs, 0, sendAllHosts);
            sendAllHosts.clear();
            rtnSocketIDs.clear();
            hostInfos.clear();
            sendSocketIDs.clear();
        }catch(Exception ex){
        }
        return 0;   
    }

    /*
    * 获取信息
    * chatData 数据
    * body 包
    * bodyLength 包长度
    */
    public static int getChatData(ChatData chatData, byte[] body, int bodyLength) {
        try {
            FCBinary br = new FCBinary();
            br.write(body, bodyLength);
            chatData.m_time = br.readDouble();
            chatData.m_key = br.readString();
            chatData.m_tokens = br.readString();
            chatData.m_from = br.readString();
            chatData.m_to = br.readString();
            chatData.m_room = br.readString();
            chatData.m_content = br.readString();
            if (DataCenter.getAesKey().length() > 0) {
                String allKey = DataCenter.getAesKey() + AESHelper.m_key.substring(DataCenter.getAesKey().length());
                chatData.m_content = AESHelper.decrypt(allKey, chatData.m_content);
            }
            chatData.m_bodyLength = br.readInt();
            if (chatData.m_bodyLength > 0) {
                chatData.m_body = new byte[chatData.m_bodyLength];
                br.readBytes(chatData.m_body);
            }
            br.close();
            return 1;
        }catch (Exception ex){
            return -1;
        }
    }

    /*
    * 客户端断开
    * socketID 套接字ID
    * localSID 服务端套接字ID
    */
    public void onClientClose(int socketID, int localSID){
        super.onClientClose(socketID, localSID);
        ArrayList<ChatHostInfo> removeHostInfos = new  ArrayList<ChatHostInfo>();
        ArrayList<Integer> sendSocketIDs = new ArrayList<Integer>();
        synchronized(m_socketIDs){
            ArrayList<Integer> removeSocketIDs = new ArrayList<Integer>();
            for(Integer sIter : m_socketIDs.keySet()){
                if (sIter == socketID) {
                    removeHostInfos.add(m_socketIDs.get(sIter));
                    removeSocketIDs.add(sIter);
                }
            }
            int removeSocketIDsSize = (int)removeSocketIDs.size();
            for (int i = 0; i < removeSocketIDsSize; i++) {
                m_socketIDs.remove(removeSocketIDs.get(i));
            }
            for(Integer sIter : m_socketIDs.keySet()){
                sendSocketIDs.add(sIter);
            }
            removeSocketIDs.clear();
        }
        int sendSocketIDsSize = sendSocketIDs.size();
        if (sendSocketIDsSize > 0) {
            sendHostInfos(sendSocketIDs, 2, removeHostInfos);
        }
        sendSocketIDs.clear();
        removeHostInfos.clear();
    }

    /*
    * 客户端连接
    * socketID 套接字ID
    * localSID 服务端套接字ID
    * ip ip地址
    */
    public void onClientConnect(int socketID, int localSID, String ip){
         super.onClientConnect(socketID, localSID, ip);
        synchronized(m_socketIDs){
            if (!m_socketIDs.containsKey(socketID)) {
                m_socketIDs.put(socketID,  new ChatHostInfo());
                String strIPPort = ip.replace("accept:", "");
                String[] strs = strIPPort.split("[:]");
                m_socketIDs.get(socketID).m_ip = strs[0].replace("/", "");
                m_socketIDs.get(socketID).m_port = FCTran.strToInt(strs[1]);
                //MainFrame::addLog(strIPPort + L"," + FCTran::intToStr(socketID));
            }
        }
    }

    /*
    * 接收消息
    * message 消息
    */
    public void onReceive(FCMessage message){
        super.onReceive(message);
        switch (message.m_functionID) {
            case FUNCTIONID_SENDALL:
                sendAll(message, false);
                break;
            case FUNCTIONID_ENTER:
                enter(message);
                break;
            case FUNCTIONID_RECORD:
                record(message);
                break;
            case FUNCTIONID_CLOUDFILE:
                cloudFile(message);
                break;
            case FUNCTIONID_SENDNOENCRPTY:
                sendAllNoEncrpty(message);
                break;
            default:
                break;
        }
    }
    
    /*
    * 接收文件信息
    */
    public HashMap<String, String> m_receiveFileInfos = new HashMap<String, String>();
    
    /*
    * 接收非加密信息
    * message 消息
    */
    public void sendAllNoEncrpty(FCMessage message)
    {
        ChatData chatData = new ChatData();
        getChatData(chatData, message.m_body, message.m_bodyLength);
        if (chatData.m_key.indexOf("sendfile:") == 0) {
                String dir = chatData.m_to;
                String userID = chatData.m_from;
                if(checkOperater(message.m_socketID, userID)){
                    String cloudDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "clouddata" + DataCenter.m_seperator + userID + DataCenter.m_seperator;
                    if(FCFile.isDirectoryExist(cloudDataDir)) {
                        cloudDataDir += dir;
                        String content = chatData.m_content;
                        String guid = chatData.m_content.substring(0, chatData.m_content.indexOf(":"));
                        String right = chatData.m_content.substring(chatData.m_content.indexOf(":") + 1);
                        String dCount = right.substring(0, right.indexOf(","));
                        String fName = right.substring(right.indexOf(",") + 1);
                        String fileName = cloudDataDir + DataCenter.m_seperator + fName;
                        if (dCount.indexOf("1/") == 0) {
                            int index = 2;
                            while(FCFile.isFileExist(fileName)){
                                if(fName.indexOf(".") != -1 && fName.indexOf(".") != fName.length() - 1){
                                    String leftText = fName.substring(0, fName.lastIndexOf("."));
                                    String rightText = fName.substring(fName.lastIndexOf(".") + 1);
                                    fileName = cloudDataDir + DataCenter.m_seperator + leftText + FCTran.intToStr(index) + "." + rightText;
                                }else{
                                    fileName = cloudDataDir + DataCenter.m_seperator + fName + FCTran.intToStr(index);
                                }
                                index++;
                            }
                            m_receiveFileInfos.put(guid, fileName);
                            if(FCFile.isFileExist(fileName)){
                                FCFile.removeFile(fileName);
                            }
                        }
                        if (m_receiveFileInfos.containsKey(guid)) {
                            if (true) {
                                try {
                                    RandomAccessFile accessFile = new RandomAccessFile(new File(m_receiveFileInfos.get(guid)), "rwd");
                                    long length = accessFile.length();
                                    accessFile.seek(length);
                                    accessFile.write(chatData.m_body);
                                    accessFile.close();
                                    String[] strs = dCount.split("[/]");
                                    if (strs[0].equals(strs[1])) {
                                        ArrayList<String> list = new ArrayList<String>();
                                        ArrayList<CloudFile> cloudFiles = new ArrayList<CloudFile>();
                                        getDirAndFiles(list, cloudDataDir, true, false);
                                        cloudFiles.clear();
                                        for (int i = 0; i < list.size(); i++)
                                        {
                                            CloudFile cloudFile = new CloudFile();
                                            cloudFile.m_path = list.get(i).replace(cloudDataDir, "");
                                            if (FCFile.isFileExist(list.get(i)))
                                            {
                                                cloudFile.m_type = 1;
                                                try {
                                                    FileInputStream in = new FileInputStream(new File(list.get(i)));
                                                    DataInputStream din = new DataInputStream(in);
                                                    long len = din.available();
                                                    cloudFile.m_size = len;
                                                    din.close();
                                                    in.close();
                                                }catch (Exception ex){

                                                }
                                            }
                                            cloudFile.m_userID = userID;
                                            cloudFiles.add(cloudFile);
                                        }
                                        Collections.sort(cloudFiles, new CloudFileCompare());
                                        sendCloudFiles(message.m_socketID, cloudFiles);
                                        cloudFiles.clear();
                                    }
                                } catch (Exception ex) {

                                }
                            }
                        }
                    }
            }
        }
    }

    /*
    * 发送数据
    * message 消息
    * chatData 信息
    */
    public int send(FCMessage message, ChatData chatData){
        try{
            String tokens = chatData.m_tokens;
            FCBinary bw = new FCBinary();
            bw.writeDouble(chatData.m_time);
            bw.writeString(chatData.m_key);
            bw.writeString(tokens);
            bw.writeString(chatData.m_from);
            bw.writeString(chatData.m_to);
            bw.writeString(chatData.m_room);
            bw.writeString(chatData.m_content);
            bw.writeInt(chatData.m_bodyLength);
            if (chatData.m_bodyLength > 0) {
                bw.writeBytes(chatData.m_body);
            }
            message.m_body = bw.getBytes();
            message.m_bodyLength = bw.getBytes().length;
            int ret = super.send(message);
            bw.close();
            return ret;
        }catch(Exception ex){
            return -1;
        }
    }
    
    /*
    * 是否记录消息
    */
    public boolean m_recordChatData = true;
    
    /*
    * 消息锁
    */
    public Object m_chatLock = new Object();

    /*
    * 发送消息
    * message 消息
    * isLocal 是否本地
    */
    public int sendAll(FCMessage message, boolean isLocal){
        if(isLocal){
            return dealWithMsg(message, isLocal);
        }else{
            FCMessage copyMessage = new FCMessage();
            copyMessage.copy(message);
            copyMessage.m_body = message.m_body;
            synchronized (m_sendMessages){
                m_sendMessages.add(copyMessage);
            }
            return 1;
        }
    }
    
    /*
    * 处理发送消息
    * message 消息
    * isLocal 是否本地
    */
    public int dealWithMsg(FCMessage message, boolean isLocal){
        FCMessage copyMessage = new FCMessage();
        copyMessage.copy(message);
        int rtnSocketID = message.m_socketID;
        ChatData chatData = new ChatData();
        getChatData(chatData, message.m_body, message.m_bodyLength);
        if(!isLocal){
            try{
                FCBinary bw = new FCBinary();
                bw.writeString(chatData.m_key);
                byte[] bytes = bw.getBytes();
                bw.close();
                FCMessage sendMsg = new FCMessage(getServiceID(), FUNCTIONID_STATE, DataCenter.getChatRequestID(), 0, 0, getCompressType(), bytes.length, bytes);
                sendMsg.m_socketID = message.m_socketID;
                super.send(sendMsg);
            }catch(Exception ex){
                
            }
       }
        if(m_recordChatData){
            if(chatData.m_key.indexOf("addtext:") == 0 || chatData.m_key.indexOf("attention:") == 0){
                synchronized(m_chatLock){
                    try{
                        Calendar calendar = FCTran.numToDate(chatData.m_time);
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                        String strDate = format.format(calendar.getTime());
                        String path = DataCenter.getAppPath() + DataCenter.m_seperator + "cdata" + DataCenter.m_seperator + strDate;
                        FCBinary bw = new FCBinary();
                        bw.writeDouble(chatData.m_time);
                        bw.writeString(chatData.m_key);
                        bw.writeString(chatData.m_tokens);
                        bw.writeString(chatData.m_from);
                        bw.writeString(chatData.m_to);
                        bw.writeString(chatData.m_room);
                        bw.writeString(chatData.m_content);
                        KVDataBase2.addValue(KVDataBase2.initDB(path, "chat"), bw.getBytes());
                        bw.close();
                    }
                    catch(Exception ex){

                    }
                }
            }
        }
        synchronized(m_socketIDs){
            String oldToken = chatData.m_tokens;
            String newToken = oldToken;
            for(Integer sIter : m_socketIDs.keySet()){
                    ChatHostInfo hostInfo = m_socketIDs.get(sIter);
                    if(hostInfo.m_serverPort!= 0 && hostInfo.m_type == 1){
                            if((int)newToken.indexOf(hostInfo.m_token) == -1){
                                    newToken += hostInfo.m_token;
                            }
                    }
            }
            chatData.m_tokens = newToken;
            ChatHostInfo thisHost = null;
            if(m_socketIDs.containsKey(rtnSocketID)){
                    thisHost = m_socketIDs.get(rtnSocketID);
            }
            for(Integer sIter : m_socketIDs.keySet()){
                ChatHostInfo hostInfo = m_socketIDs.get(sIter);
                    if (rtnSocketID != sIter && hostInfo.m_serverPort != 0) {
                            copyMessage.m_socketID = sIter;
                            boolean hasFind = false;
                            if (hostInfo.m_type == 0 && chatData.m_to.length() > 0) {
                                if (chatData.m_to.indexOf(hostInfo.m_userID) == -1) {
                                    continue;
                                }
                                hasFind = true;
                            }
                            if(hostInfo.m_type == 0 || (int)chatData.m_tokens.indexOf("determine:") == 0){
                                    if(thisHost != null && hostInfo.m_room.equals(thisHost.m_room)){
                                            //MainFrame::addLog(L"向客户端" + chatData.m_to + L"发送数据：" + chatData.m_content + L"," + FCTran::intToStr(message->m_socketID));
                                            send(copyMessage, chatData);
                                    }
                            }else{
                                    if(m_sendToNear && (int)oldToken.indexOf(hostInfo.m_token) == -1){
                                            //MainFrame::addLog(L"向临近节点" + sIter->second->m_ip + L":" + FCTran::intToStr(sIter->second->m_serverPort) + L"发送数据：" + chatData.m_content);
                                            send(copyMessage, chatData);
                                    }
                            }
                            if(hasFind){
                                    //break;
                            }
                    }
            }
        }
        return 1;
    }

    /*
    * 发送主机信息
    * socketIDs 套接字数组
    * type 类型
    * hostInfos 返回主机信息
    */
    public int sendHostInfos(ArrayList<Integer> socketIDs, int type, ArrayList<ChatHostInfo> hostInfos){
        try{
            int hostInfosSize = (int)hostInfos.size();
            FCBinary bw = new FCBinary();
            bw.writeInt(hostInfosSize);
            bw.writeInt(type);
            for (int i = 0; i < hostInfosSize; i++) {
                ChatHostInfo hostInfo = hostInfos.get(i);
                bw.writeString(hostInfo.m_ip);
                bw.writeInt(hostInfo.m_serverPort);
                bw.writeInt(hostInfo.m_type);
                bw.writeString(hostInfo.m_token);
                bw.writeString(hostInfo.m_userID);
                bw.writeString(hostInfo.m_code);
                bw.writeString(hostInfo.m_icon);
                bw.writeString(hostInfo.m_exInfo);
            }
            byte[] bytes = bw.getBytes();
            FCMessage message = new FCMessage(getServiceID(), FUNCTIONID_GETHOSTS, DataCenter.getChatRequestID(), 0, 0, getCompressType(), bytes.length, bytes);
            for(int s = 0; s < socketIDs.size(); s++){
                message.m_socketID = socketIDs.get(s);
                super.send(message);
            }
            bw.close();
        }catch(Exception ex){
            
        }
        return 1;
    }

    /*
    * 发送消息给本地客户端
    * message 消息
    */
    public int sendMsg(FCMessage message){
        sendToListener(message);
        return 1;
    }
    
    /*
    * 发送消息线程
    */
    public void startSendMessage()
    {
        while(true){
            ArrayList<FCMessage> sendMessages = new ArrayList<FCMessage>();
            synchronized (m_sendMessages){
                if(m_sendMessages.size() > 0){
                    for(int i = 0; i < m_sendMessages.size(); i++){
                        sendMessages.add(m_sendMessages.get(i));
                    }
                    m_sendMessages.clear();
                }
            }
            if(sendMessages.size() > 0){
                for(int i = 0; i < sendMessages.size(); i++){
                    FCMessage copyMessage = sendMessages.get(i);
                    dealWithMsg(copyMessage, false);
                }
                sendMessages.clear();
            }else{
                try {
                    Thread.sleep(1);
                }catch (Exception ex){

                }
            }
        }
    }
    
    /*
    * 发送文件
    */
    public ArrayList<SendFileInfo> m_sendFileInfos = new ArrayList<SendFileInfo>();
    
    /*
    * 处理发送文件
    * sendFileName 发送文件
    * chatData 信息
    * socketID 套接字ID
    */
    public void startSendFile(String sendFileName, ChatData chatData, int socketID){
        try {
            String identifier = FCTran.getGuid();
            FileInputStream in = new FileInputStream(new File(sendFileName));
            DataInputStream din = new DataInputStream(in);
            long len = din.available();
            int slice = 1024 * 1024 * 3;
            int sCount = (int) (len / slice);
            int mod = (int) (len % slice);
            if (mod > 0) {
                sCount += 1;
            }
            for (int i = 0; i < sCount; i++) {
                byte[] bytes = null;
                if (i != sCount - 1) {
                    bytes = new byte[slice];
                } else {
                    bytes = new byte[mod];
                }
                din.read(bytes);
                String sayText = identifier + ":" + FCTran.intToStr(i + 1) + "/" + FCTran.intToStr(sCount) + "," + sendFileName.substring(sendFileName.lastIndexOf(DataCenter.m_seperator) + 1);
                chatData.m_key = "sendfile:" + FCTran.getGuid();
                chatData.m_content = sayText;
                chatData.m_body = bytes;
                chatData.m_bodyLength = bytes.length;
                chatData.m_from = "区块链节点";
                FCMessage copyMessage = new FCMessage(getServiceID(), FUNCTIONID_SENDNOENCRPTY, DataCenter.getChatRequestID(), socketID, 0, getCompressType(), 0, null);
                send(copyMessage, chatData);
            }
            din.close();
            in.close();
        }catch (Exception ex){
        }
    }
     
    /*
    * 发送文件线程
    */
    public void checkSendFile(){
        while(true){
            ArrayList<SendFileInfo> sendFiles = new ArrayList<SendFileInfo>();
            synchronized (m_sendFileInfos){
                for(int i = 0; i < m_sendFileInfos.size(); i++){
                    sendFiles.add(m_sendFileInfos.get(i));
                }
                m_sendFileInfos.clear();
            }
            if(sendFiles.size() > 0){
                for(int i = 0; i < sendFiles.size(); i++){
                    SendFileInfo sendFileInfo = sendFiles.get(i);
                    startSendFile(sendFileInfo.m_sendFileName, sendFileInfo.m_sendChatData, sendFileInfo.m_socketID);
                }
            }
            try{
                Thread.sleep(10);
            }catch(Exception ex){
                
            }
        }
    }
    
    /*
    * 启动服务
    */
    public void startService(){
         new Thread(new Runnable() {
                    @Override
                    public void run() {
                        checkAlive();
                    }
                }).start();
         new Thread(new Runnable() {
            @Override
            public void run() {
                startSendMessage();
            }
        }).start();
         new Thread(new Runnable() {
            @Override
            public void run() {
                checkSendFile();
            }
        }).start();
    }
    
    /*
    * 获取云文件信息
    * body 包
    * bodyLength 包体长度
    * cloudFiles 云文件信息
    */
     public static int getCloudFiles(byte[] body, int bodyLength, ArrayList<CloudFile> cloudFiles)
    {
        try
        {
            FCBinary br = new FCBinary();
            br.write(body, bodyLength);
            int size = br.readInt();
            for (int i = 0; i < size; i++)
            {
                CloudFile cloudFile = new CloudFile();
                cloudFile.m_path = br.readString();
                cloudFile.m_type = br.readInt();
                cloudFile.m_size = br.readDouble();
                cloudFile.m_createTime = br.readDouble();
                cloudFile.m_modifyTime = br.readDouble();
                cloudFile.m_extend = br.readString();
                cloudFile.m_cmd = br.readString();
                cloudFile.m_userID = br.readString();
                cloudFiles.add(cloudFile);
            }
            br.close();
        }
        catch (Exception ex)
        {
        }
        return 1;
    }

    /*
    * 发送云文件信息
    * socketID 套接字ID
    * cloudFiles 云文件信息
    */
    public int sendCloudFiles(int socketID, ArrayList<CloudFile> cloudFiles)
    {
        try{
            FCBinary bw = new FCBinary();
            bw.writeInt(cloudFiles.size());
            for (int i = 0; i < cloudFiles.size(); i++)
            {
                CloudFile cloudFile = cloudFiles.get(i);
                bw.writeString(cloudFile.m_path);
                bw.writeInt(cloudFile.m_type);
                bw.writeDouble(cloudFile.m_size);
                bw.writeDouble(cloudFile.m_createTime);
                bw.writeDouble(cloudFile.m_modifyTime);
                bw.writeString(cloudFile.m_extend);
                bw.writeString(cloudFile.m_cmd);
                bw.writeString(cloudFile.m_userID);
            }
            byte[] bytes = bw.getBytes();
            FCMessage message = new FCMessage(getServiceID(), FUNCTIONID_CLOUDFILE, DataCenter.getChatRequestID(), socketID, 0, getCompressType(), bytes.length, bytes);
            super.send(message);
            bw.close();
        }catch(Exception ex){

        }
        return 1;
    }
    
    /*
    * 获取所有的文件和文件夹
    * list 返回文件/文件夹
    * dir 目录
    * withFile 是否包含文件
    * recursion 是否递归
    */
    public static void getDirAndFiles(ArrayList<String> list, String dir, boolean withFile, boolean recursion)
    {
            ArrayList<String> dirs = new ArrayList<String>();
            FCFile.getDirectories(dir, dirs);
            for (int i = 0; i < dirs.size(); i++)
            {
                    list.add(dirs.get(i));
                    if(recursion) {
                            getDirAndFiles(list, dirs.get(i), withFile, recursion);
                    }
            }
            if(withFile){
                ArrayList<String> files = new ArrayList<String>();
                FCFile.getFiles(dir, files);
                for (int i = 0; i < files.size(); i++)
                {
                        list.add(files.get(i));
                }
            }
    }
   
    /*
    * 删除文件夹
    * dir 目录
    */
    public static void deleteDir(String dir){
            ArrayList<String> files = new ArrayList<String>();
            FCFile.getFiles(dir, files);
            for (int i = 0; i < files.size(); i++)
            {
                    FCFile.removeFile(files.get(i));
            }
            ArrayList<String> dirs = new ArrayList<String>();
            FCFile.getDirectories(dir, dirs);
            for (int i = 0; i < dirs.size(); i++)
            {
                    deleteDir(dirs.get(i));
            }
            new File(dir).delete();
    }
     
    /*
    * 拷贝文件
    * srcPath 源文件
    * destPath 目标文件
    */
    public static void FileCopy(String srcPath, String destPath){ //文件复制
               File f = new File(srcPath); //源文件
               File F = new File(destPath);  //目的文件
               byte [] a =  new byte[1024];
               try{
                       FileInputStream f1 = new FileInputStream(f);  //对源文件输入流
                       FileOutputStream F1 = new FileOutputStream(F); //对目的文件输出流
                       while(true) {
                               int b = f1.read(a);   // 实际上他还是读了a&#xff0c;只有在最后一次他读了 存在 b 里面不满的那个
                               if (b  == -1) break;
                               F1.write(a, 0, b); // 在 这里b的作用是 从0-b的长度是为了防止最后一次可能不满 1024
                               F1.flush();             //美其名曰 &#xff1a; 收尾保险员 b
                       }
               }catch (IOException e){
                       e.printStackTrace();
               }

    }
     
   /*
    * 拷贝文件夹
    * srcPath 源文件
    * destPath 目标文件
    */
    public static void foldercopy(String srcPath,String destPath){  //参数是目录&#xff0c;而不是文件
            File srcFolder = new File(srcPath);
            File destFolder = new File(destPath);
            if(!srcFolder.isDirectory()) //源文件不是一个文件夹
                    return;
            if (!srcFolder.exists())   //源文件夹不存在
                    return;
            if (destFolder.isFile())   //目标文件夹是一个文件&#xff0c;&#xff0c;就不行
                    return;
            if(!destFolder.exists())   //目标文件夹不存在&#xff0c;就自动创建一个
                    destFolder.mkdirs();
            File [] files = srcFolder.listFiles();      //遍历源文件夹&#xff0c;files就是临时文件名
            for(File srcFile:files)
            {
                    if (srcFile.isFile()) {//如果是文件就复制过去
                            // 这里是准备创建目标文件newDestFile,根据 目标文件目录&#xff0c;和 源文件名
                            File newDestFile = new File(destFolder,srcFile.getName()); //暂时的临时的虚空的
                            //然后在 从源文件绝对路径 复制到 刚刚创建的新绝对路径文件
                            FileCopy(srcFile.getAbsolutePath(), newDestFile.getAbsolutePath());
                    }

                    //上下操作差不多&#xff0c;就是file改成了folder。。 下面递归是本体而已

                    if(srcFile.isDirectory()) { //如果是文件夹就递归遍历,注意参数是目录&#xff0c;是文件夹,
                            //在这个分支里面 srcFile是文件夹 而不是文件
                            File newDestFolder = new File(destFolder,srcFile.getName());
                            //注意递归函数名
                            foldercopy(srcFile.getAbsolutePath(),newDestFolder.getAbsolutePath());
                    }
            }
    }
     
   /*
    * 检查操作权限
    * socketID 套接字ID
    * userID 用户ID
    */
    public boolean checkOperater(int socketID, String userID)
    {
        boolean checkUser = false;
        synchronized (m_socketIDs)
        {
            for (Integer sIter : m_socketIDs.keySet())
            {
                ChatHostInfo hostInfo = m_socketIDs.get(sIter);
                if (sIter == socketID)
                {
                    if(hostInfo.m_userID.equals(userID)) {
                        checkUser = true;
                        break;
                    }
                }
            }
        }
        return checkUser;
    }
    
    /*
    * 检查浏览权限
    * socketID 套接字ID
    * userID 用户ID
    */
    public boolean checkBrowser(int socketID, String userID)
    {
        boolean checkUser = false;
        synchronized (m_socketIDs)
        {
            for (Integer sIter : m_socketIDs.keySet())
            {
                ChatHostInfo hostInfo = m_socketIDs.get(sIter);
                 if (hostInfo.m_userID.equals(userID))
                {
                    if (socketID == sIter || hostInfo.m_exInfo.equals("1"))
                    {
                        checkUser = true;
                        break;
                    }
                }
            }
        }
        return checkUser;
    }
    
    /*
    * 处理云文件消息
    * message 消息
    */
    public int cloudFile(FCMessage message)
    {
        ArrayList<CloudFile> cloudFiles = new ArrayList<CloudFile>();
        getCloudFiles(message.m_body, message.m_bodyLength, cloudFiles);
        if (cloudFiles.size() > 0)
        {
            String userID = cloudFiles.get(0).m_userID;
            String dir = cloudFiles.get(0).m_path;
            String cmd = cloudFiles.get(0).m_cmd;
            if (cmd.equals("rename"))
            {
                if(checkOperater(message.m_socketID, userID)){
                    String cloudDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "clouddata" + DataCenter.m_seperator + userID;
                    if(FCFile.isDirectoryExist(cloudDataDir)) {
                        String[] strs = dir.split("[;]");
                        String sDir = "";
                        if(strs.length > 2){
                            sDir = strs[2];
                        }
                        String oldPath = cloudDataDir + DataCenter.m_seperator + sDir + DataCenter.m_seperator + strs[0];
                        String newPath = cloudDataDir + DataCenter.m_seperator + sDir + DataCenter.m_seperator + strs[1];
                        String result = "fail";
                        if(!oldPath.equals(newPath)) {
                            if (FCFile.isFileExist(oldPath))
                            {
                                if (!FCFile.isFileExist(newPath))
                                {
                                    new File(oldPath).renameTo(new File(newPath));
                                    result = "success";
                                }
                            }
                            else if (FCFile.isDirectoryExist(oldPath))
                            {
                                if (!FCFile.isDirectoryExist(newPath))
                                {
                                    FCFile.createDirectory(newPath);
                                    foldercopy(oldPath, newPath);
                                    deleteDir(oldPath);
                                    result = "success";
                                }
                            }
                        }
                        CloudFile retCloudFile = new CloudFile();
                        retCloudFile.m_path = dir;
                        retCloudFile.m_userID = userID;
                        retCloudFile.m_cmd = cmd;
                        retCloudFile.m_extend = result;
                        cloudFiles.clear();
                        cloudFiles.add(retCloudFile);
                        sendCloudFiles(message.m_socketID, cloudFiles);
                        cloudFiles.clear();
                    }
                }
            }
            else if (cmd.equals("move"))
            {
                if(checkOperater(message.m_socketID, userID)){
                    String cloudDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "clouddata" + DataCenter.m_seperator + userID;
                    if(FCFile.isDirectoryExist(cloudDataDir)) {
                        String[] strs = dir.split("[;]");
                        String target = strs[2];
                        String fromDir = strs[0];
                        String toDir = strs[1];
                        if(fromDir.equals(".cloudroot")){
                                fromDir = "";
                        }
                        if(toDir.equals(".cloudroot")){
                                toDir = "";
                        }
                        String oldPath = cloudDataDir + DataCenter.m_seperator + fromDir + DataCenter.m_seperator + target;
                        String newPath = cloudDataDir + DataCenter.m_seperator + toDir + DataCenter.m_seperator + target;
                        if(fromDir.length() == 0){
                            oldPath = cloudDataDir + DataCenter.m_seperator + target;
                        }
                        if(toDir.length() == 0){
                            newPath = cloudDataDir + DataCenter.m_seperator + target;
                        }
                        String result = "fail";
                        if(newPath.indexOf(oldPath) == -1) {
                            if (FCFile.isFileExist(oldPath))
                            {
                                if (!FCFile.isFileExist(newPath))
                                {
                                    new File(oldPath).renameTo(new File(newPath));
                                    result = "success";
                                }
                            }
                            else if (FCFile.isDirectoryExist(oldPath))
                            {
                                if (!FCFile.isDirectoryExist(newPath))
                                {
                                    FCFile.createDirectory(newPath);
                                    foldercopy(oldPath, newPath);
                                    deleteDir(oldPath);
                                    result = "success";
                                }
                            }
                        }
                        CloudFile retCloudFile = new CloudFile();
                        retCloudFile.m_path = dir;
                        retCloudFile.m_userID = userID;
                        retCloudFile.m_cmd = cmd;
                        retCloudFile.m_extend = result;
                        cloudFiles.clear();
                        cloudFiles.add(retCloudFile);
                        sendCloudFiles(message.m_socketID, cloudFiles);
                        cloudFiles.clear();
                    }
                }
            }
            else if (cmd.equals("download"))
            {
                if(checkBrowser(message.m_socketID, userID)){
                    String cloudDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "clouddata" + DataCenter.m_seperator + userID;
                    if(FCFile.isDirectoryExist(cloudDataDir)) {
                        SendFileInfo sendFileInfo = new SendFileInfo();
                        sendFileInfo.m_socketID = message.m_socketID;
                        sendFileInfo.m_sendFileName = cloudDataDir + DataCenter.m_seperator + dir;
                        ChatData chatData = new ChatData();
                        chatData.m_from = getToken();
                        chatData.m_to = userID;
                        sendFileInfo.m_sendChatData = chatData;
                        synchronized (m_sendFileInfos)
                        {
                            m_sendFileInfos.add(sendFileInfo);
                        }
                    }
                }
            }
            else if (cmd.equals("mkdir")) {
                if(checkOperater(message.m_socketID, userID)){
                    String cloudDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "clouddata" + DataCenter.m_seperator + userID + DataCenter.m_seperator;
                   if(FCFile.isDirectoryExist(cloudDataDir)) {
                        cloudDataDir += dir;
                        CloudFile retCloudFile = new CloudFile();
                        retCloudFile.m_path = dir;
                        retCloudFile.m_userID = userID;
                        retCloudFile.m_cmd = cmd;
                        int index = 1;
                        String newDir = "新的云文件夹";
                        while (true)
                        {
                            String createDir = cloudDataDir + DataCenter.m_seperator + newDir;
                            if (index > 1)
                            {
                                createDir += FCTran.intToStr(index);
                            }
                            if (FCFile.isDirectoryExist(createDir))
                            {
                                index++;
                            }
                            else
                            {
                                FCFile.createDirectory(createDir);
                                break;
                            }
                        }
                        retCloudFile.m_extend = "success";
                        cloudFiles.clear();
                        cloudFiles.add(retCloudFile);
                        sendCloudFiles(message.m_socketID, cloudFiles);
                        cloudFiles.clear();
                   }
                }
            } else if (cmd.equals("delete")) {
                if(checkOperater(message.m_socketID, userID)){
                    String cloudDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "clouddata" + DataCenter.m_seperator + userID + DataCenter.m_seperator;
                    if(FCFile.isDirectoryExist(cloudDataDir)) {
                         cloudDataDir += dir;
                         CloudFile retCloudFile = new CloudFile();
                         retCloudFile.m_path = dir;
                         retCloudFile.m_userID = userID;
                         retCloudFile.m_cmd = cmd;
                         if (FCFile.isDirectoryExist(cloudDataDir)) {
                             ArrayList<String> list = new ArrayList<String>();
                             getDirAndFiles(list, cloudDataDir, true, true);
                             for (int i = list.size() - 1; i >= 0; i--) {
                                 if (FCFile.isDirectoryExist(list.get(i))) {
                                     new File(list.get(i)).delete();
                                 } else if (FCFile.isFileExist(list.get(i))) {
                                     FCFile.removeFile(list.get(i));
                                 }
                             }
                             new File(cloudDataDir).delete();
                             retCloudFile.m_extend = "success";
                         } else if (FCFile.isFileExist(cloudDataDir)) {
                             FCFile.removeFile(cloudDataDir);
                             retCloudFile.m_extend = "success";
                         } else {
                             retCloudFile.m_extend = "fail";
                         }
                         cloudFiles.clear();
                         cloudFiles.add(retCloudFile);
                         sendCloudFiles(message.m_socketID, cloudFiles);
                         cloudFiles.clear();
                    }
                }
            }
            else if(cmd.equals("getalldirs")){
                if(checkBrowser(message.m_socketID, userID)){
                    String cloudDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "clouddata" + DataCenter.m_seperator + userID + DataCenter.m_seperator;
                    if(FCFile.isDirectoryExist(cloudDataDir)) {
                        ArrayList<String> list = new ArrayList<String>();
                        getDirAndFiles(list, cloudDataDir, false, true);
                        cloudFiles.clear();
                        for (int i = 0; i < list.size(); i++)
                        {
                            CloudFile cloudFile = new CloudFile();
                            cloudFile.m_path = list.get(i).replace(cloudDataDir, "");
                            if (FCFile.isFileExist(list.get(i)))
                            {
                                cloudFile.m_type = 1;
                                try {
                                    FileInputStream in = new FileInputStream(new File(list.get(i)));
                                    DataInputStream din = new DataInputStream(in);
                                    long len = din.available();
                                    cloudFile.m_size = len;
                                    din.close();
                                    in.close();
                                }catch (Exception ex){

                                }
                            }
                            cloudFile.m_cmd = cmd;
                            cloudFile.m_userID = userID;
                            cloudFiles.add(cloudFile);
                        }
                        sendCloudFiles(message.m_socketID, cloudFiles);
                        cloudFiles.clear();
                    }
                }
            }
            else{
                if(checkBrowser(message.m_socketID, userID)){
                    String cloudDataDir = DataCenter.getAppPath() + DataCenter.m_seperator + "clouddata" + DataCenter.m_seperator + userID + DataCenter.m_seperator;
                    if(FCFile.isDirectoryExist(cloudDataDir)) {
                        ArrayList<String> list = new ArrayList<String>();
                        getDirAndFiles(list, cloudDataDir + dir, true, false);
                        cloudFiles.clear();
                        for (int i = 0; i < list.size(); i++)
                        {
                            CloudFile cloudFile = new CloudFile();
                            cloudFile.m_path = list.get(i).replace(cloudDataDir, "");
                            if (FCFile.isFileExist(list.get(i)))
                            {
                                cloudFile.m_type = 1;
                                try {
                                    FileInputStream in = new FileInputStream(new File(list.get(i)));
                                    DataInputStream din = new DataInputStream(in);
                                    long len = din.available();
                                    cloudFile.m_size = len;
                                    din.close();
                                    in.close();
                                }catch (Exception ex){

                                }
                            }
                            cloudFile.m_userID = userID;
                            cloudFiles.add(cloudFile);
                        }
                         Collections.sort(cloudFiles, new CloudFileCompare());
                        sendCloudFiles(message.m_socketID, cloudFiles);
                        cloudFiles.clear();
                    }
                }
            }
        }
        return 1;
    }
}
