/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuron;
import facecat.topin.service.*;
import facecat.topin.core.*;
import static facecat.topin.service.FCClientService.COMPRESSTYPE_NONE;
import iKv.KVDataBase2;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServiceEx extends FCClientService
{
    public ChatServiceEx()
    {
        setServiceID(SERVICEID_CHAT);
        setCompressType(COMPRESSTYPE_NONE);
    }
        
        /// <summary>
    /// 区块链服务ID
    /// </summary>
    public static final int SERVICEID_CHAT = 19999;

    /// <summary>
    /// 主机信息
    /// </summary>
    public static final int FUNCTIONID_GETHOSTS = 1;

    /// <summary>
    /// 广播区块链功能ID
    /// </summary>
    public static final int FUNCTIONID_SENDALL = 3;

    /// <summary>
    /// 进入
    /// </summary>
    public static final int FUNCTIONID_ENTER = 6;

    /// <summary>
    /// 聊天记录
    /// </summary>
    public static final int FUNCTIONID_RECORD = 7;

    public static final int FUNCTIONID_STATE = 8;

    public int m_socketID;

    public String m_token = "";

    public String m_aesKey;

    /// <summary>
    /// 区块链通用请求ID
    /// </summary>
    public int getChatRequestID()
    {
        return 9999;
    }

    /// <summary>
    /// 进入区块链
    /// </summary>
    /// <returns>状态</returns>
    public int enter(String userID, String code, String icon, String room)
    {
        try{
            FCBinary bw = new FCBinary();
            bw.writeInt(0);
            bw.writeInt(0);
            bw.writeString(m_token);
            bw.writeString(userID);
            bw.writeString(code);
            bw.writeString(icon);
            bw.writeString(room);
            bw.writeString("");
            byte[] bytes = bw.getBytes();
            int ret = send(new FCMessage(getServiceID(), FUNCTIONID_ENTER, getChatRequestID(), m_socketID, 0, getCompressType(), bytes.length, bytes));
            bw.close();
        return ret;
        }catch(Exception ex){
            return -1;
        }
    }

    /// <summary>
    /// 获取弹幕信息
    /// </summary>
    /// <param name="chatData">聊天信息</param>
    /// <param name="body">包体</param>
    /// <param name="bodyLength">包体长度</param>
    /// <returns></returns>
    public int getChatData(ChatData chatData, byte[] body, int bodyLength)
    {
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
            if (m_aesKey.length() > 0) {
                String allKey = m_aesKey + AESHelper.m_key.substring(m_aesKey.length());
                try {
                    chatData.m_content = AESHelper.decrypt(allKey, chatData.m_content);
                }catch (Exception ex){
                    chatData.m_content = "";
                }
            }
            chatData.m_bodyLength = br.readInt();
            if (chatData.m_bodyLength > 0) {
                chatData.m_body = new byte[chatData.m_bodyLength];
                br.readBytes(chatData.m_body);
            }
            br.close();
            if(chatData.m_content.length() > 0) {
                return 1;
            }else{
                return 0;
            }
        }catch (Exception ex){
            return 0;
        }
    }

    /// <summary>
    /// 发送消息
    /// </summary>
    /// <param name="userID">方法ID</param>
    /// <param name="tokens">请求ID</param>
    /// <param name="chatData">发送字符</param>
    public int send(int functionID, ChatData chatData)
    {
        try {
            saveDataCount(chatData.m_key);
            FCBinary bw = new FCBinary();
            bw.writeDouble(chatData.m_time);
            bw.writeString(chatData.m_key);
            bw.writeString(chatData.m_tokens);
            bw.writeString(chatData.m_from);
            bw.writeString(chatData.m_to);
            bw.writeString(chatData.m_room);
            if (m_aesKey.length() > 0) {
                String allKey = m_aesKey + AESHelper.m_key.substring(m_aesKey.length());
                String newStr = AESHelper.encrypt(allKey, chatData.m_content);
                bw.writeString(newStr);
            } else {
                bw.writeString(chatData.m_content);
            }
            bw.writeInt(chatData.m_bodyLength);
            if (chatData.m_bodyLength > 0) {
                bw.writeBytes(chatData.m_body);
            }
            byte[] bytes = bw.getBytes();
            FCMessage message = new FCMessage(getServiceID(), functionID, getChatRequestID(), m_socketID, 0, getCompressType(), bytes.length, bytes);
            int ret = send(message);
            return ret;
        } catch (Exception ex) {
            return -1;
        }
    }
        
    /// <summary>
    /// 获取主机信息
    /// </summary>
    /// <param name="body">包体</param>
    /// <param name="bodyLength">包体长度</param>
    /// <returns></returns>
    public static int getHostInfos(ArrayList<ChatHostInfo> datas, RefObject<Integer> type, byte[] body, int bodyLength) {
        try {
            FCBinary br = new FCBinary();
            br.write(body, bodyLength);
            int size = br.readInt();
            if(size > 0) {
                type.argvalue = br.readInt();
                for (int i = 0; i < size; i++) {
                    ChatHostInfo data = new ChatHostInfo();
                    data.m_ip = br.readString();
                    data.m_serverPort = br.readInt();
                    data.m_type = br.readInt();
                    data.m_token = br.readString();
                    data.m_userID = br.readString();
                    data.m_code = br.readString();
                    data.m_icon = br.readString();
                    datas.add(data);
                }
            }
            br.close();
            return 1;
        }catch (Exception ex){
            return -1;
        }
    }

    public void onReceive(FCMessage message)
    {
        //base.onReceive(message);
        if (message.m_socketID != m_socketID)
        {
            return;
        }
        if (message.m_functionID == FUNCTIONID_GETHOSTS)
        {
            ArrayList<ChatHostInfo> datas = new ArrayList<ChatHostInfo>();
            int type = 0;
            RefObject<Integer> refType = new RefObject<Integer>(type);
            getHostInfos(datas, refType, message.m_body, message.m_bodyLength);
            type = refType.argvalue;
            for (int i = 0; i < datas.size(); i++)
            {
                ChatHostInfo hostInfo = datas.get(i);
                if (hostInfo.m_type == 1)
                {
                }
                else
                {
                }
            }
        }
        else if (message.m_functionID == FUNCTIONID_SENDALL)
        {
            ChatData chatData = new ChatData();
            if (getChatData(chatData, message.m_body, message.m_bodyLength) == 1)
            {
                if (!saveDataCount(chatData.m_key))
                {
                    if (chatData.m_key.indexOf("addtext:") != -1)
                    {
                    }
                }
            }
        }
    }

    /// <summary>
    /// 数据的数量
    /// </summary>
    public HashMap<String, Integer> m_datasCount = new HashMap<String, Integer>();

    /// <summary>
    /// 保存数据数量
    /// </summary>
    /// <param name="key"></param>
    /// <returns></returns>
    public boolean saveDataCount(String key)
    {
        boolean exist = false;
        synchronized (m_datasCount)
        {
            if (m_datasCount.containsKey(key))
            {
                m_datasCount.put(key, m_datasCount.get(key) + 1);
                exist = true;
            }
            else
            {
                m_datasCount.put(key, 1);
            }
        }
        return exist;
    }
    
    public void startConnect(String ip, int port, String userID, String code, String icon, String room, String aesKey){
        m_socketID = FCClientService.connectToServer(0, ip, port, "", 0, "", "", "", 0, new byte[]{99,104,97,116});
        m_aesKey = aesKey;
        enter(userID, code, icon, room);
    }
}
