package neuron;

import facecat.topin.core.*;

public class ChatData {
    /// <summary>
    /// 包体长度
    /// </summary>
    public int m_bodyLength;

    /// <summary>
    /// 包体
    /// </summary>
    public byte[] m_body;

    /// <summary>
    /// 内容
    /// </summary>
    public String m_content = "";

    /// <summary>
    /// 发送者
    /// </summary>
    public String m_from = "";

    /// <summary>
    /// 唯一标识
    /// </summary>
    public String m_key = FCTran.getGuid();

    /// <summary>
    /// 接收用户
    /// </summary>
    public String m_to = "";

    /// <summary>
    /// 标识
    /// </summary>
    public String m_tokens = "";

    /// <summary>
    /// 时间戳
    /// </summary>
    public double m_time;
    
    /// <summary>
    /// 房间
    /// </summary>
    public String m_room = "";
}

