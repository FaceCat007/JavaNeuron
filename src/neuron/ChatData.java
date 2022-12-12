package neuron;

import facecat.topin.core.*;

/*
* 通讯数据
*/
public class ChatData {
    public int m_bodyLength; //包体长度
    public byte[] m_body; //包体
    public String m_content = ""; //内容
    public String m_from = ""; //发送者
    public String m_key = FCTran.getGuid(); //唯一标识
    public String m_to = ""; //接收用户
    public String m_tokens = ""; //标识
    public double m_time; //时间戳
    public String m_room = ""; //房间
}

