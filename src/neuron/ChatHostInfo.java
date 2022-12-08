package neuron;
import facecat.topin.core.*;

public class ChatHostInfo {
    public String m_aesKey = "";
    public String m_code = "";
    public String m_icon = "";
    public String m_ip = "";
    public int m_port;
    public String m_room = "";
    public int m_serverPort;
    public String m_token = "";
    public int m_type;
    public String m_userID = "";
    public String m_exInfo = "";
    
    public String getKey(){
	return m_ip + ":" + FCTran.intToStr(m_serverPort);
    }
}
