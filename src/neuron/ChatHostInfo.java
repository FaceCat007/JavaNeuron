package neuron;
import facecat.topin.core.*;

/*
* 用户信息
*/
public class ChatHostInfo {
    public String m_aesKey = ""; //密钥
    public String m_code = ""; //昵称
    public String m_icon = ""; //头像
    public String m_ip = ""; //IP地址
    public int m_port; //端口号
    public String m_room = ""; //房间号
    public int m_serverPort; //服务端端口
    public String m_token = ""; //唯一标识
    public int m_type; //类型
    public String m_userID = ""; //用户ID
    public String m_exInfo = ""; //扩展信息
    /*
    * 获取主键
    */
    public String getKey(){
	return m_ip + ":" + FCTran.intToStr(m_serverPort);
    }
}
