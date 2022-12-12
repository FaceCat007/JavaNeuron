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

/*
* 多端互动客户端
*/
public class ChatServiceEx extends FCClientService
{
    /*
    * 构造函数
    */
    public ChatServiceEx()
    {
        setServiceID(SERVICEID_CHAT);
        setCompressType(COMPRESSTYPE_NONE);
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
    * 进入ID
    */
    public static final int FUNCTIONID_ENTER = 6;

    /*
    * 获取聊天记录的ID
    */
    public static final int FUNCTIONID_RECORD = 7;

    /*
    * 获取状态的ID
    */
    public static final int FUNCTIONID_STATE = 8;

    /*
    * 套接字ID
    */
    public int m_socketID;

    /*
    * 唯一标识
    */
    public String m_token = "";

    /*
    * 加密密钥
    */
    public String m_aesKey;

    /*
    * 通用请求ID
    */
    public int getChatRequestID()
    {
        return 9999;
    }

    /*
    * 进入服务
    */
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

    /*
    * 获取通讯信息
    */
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

    /*
    * 发送消息
    */
    public int send(int functionID, ChatData chatData)
    {
        try {
            DataCenter.saveDataCount(chatData.m_key);
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
        
    /*
    * 获取主机信息
    */
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

    /*
    * 接收消息
    */
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
                if (!DataCenter.saveDataCount(chatData.m_key))
                {
                    if (chatData.m_key.indexOf("addtext:") != -1)
                    {
                    }
                }
            }
        }
    }
    
    /*
    * 启动连接
    */
    public void startConnect(String ip, int port, String userID, String code, String icon, String room, String aesKey){
        m_socketID = FCClientService.connectToServer(0, ip, port, "", 0, "", "", "", 0, new byte[]{99,104,97,116});
        m_aesKey = aesKey;
        enter(userID, code, icon, room);
    }
}
