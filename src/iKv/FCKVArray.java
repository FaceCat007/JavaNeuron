package iKv;
import facecat.topin.core.*;

public class FCKVArray {
    public boolean m_autoClose;
    public int m_closeTime = 10000;
    public KVData2 []m_datas;
    public boolean m_fall = true;
    public boolean m_forceClose;
    public String m_name;
    public int m_number = 1;
    public String m_path;
    public int m_hashMode;
    public FCKVArray(){

    }
    public void finalize() throws Throwable {
    }
    public boolean autoClose(){
        return m_autoClose;
    }
    public void setAutoClose(boolean autoClose){
        m_autoClose = autoClose;
    }
    public int getCloseTime(){
        return m_closeTime;
    }
    public void setCloseTime(int closeTime){
        m_closeTime = closeTime;
    }
    public boolean isFall(){
        return m_fall;
    }
    public void setFall(boolean fall){
        m_fall = fall;
    }
    public String getName(){
        return m_name;
    }
    public void setName(String name){
        m_name = name;
    }
    public int getNumber(){
        return m_number;
    }
    public void setNumber(int number){
        m_number = number;
    }
    public String getPath(){
        return m_path;
    }
    public void setPath(String path){
        m_path = path;
    }

    public boolean m_threadMode = true;

    public boolean m_noTakeup = false;

    public int close(){
        try {
            m_forceClose = true;
            if(m_threadMode) {
                while (true) {
                    boolean outLoop = true;
                    for (int i = 0; i < m_number; i++) {
                        if (m_datas[i].m_loaded) {
                            outLoop = false;
                        }
                    }
                    if (outLoop) {
                        break;
                    }
                    Thread.sleep(10);
                }
                Thread.sleep(1000);
            }
            for (int i = 0; i < m_number; i++) {
                m_datas[i] = null;
            }
            m_datas = null;
        }catch (Exception ex){

        }
        return 0;
    }

    public String getValue(int index){
        try {
            byte[] bytes = m_datas[0].getValue(index);
            if (bytes != null) {
                return KVData2.bytesToStr(bytes);
            } else {
                return "nil";
            }
        }catch (Exception ex){
            return "nil";
        }
    }

    public byte[] getValue2(int index){
        return m_datas[0].getValue(index);
    }

    public int getKv(int index, int number, RefObject<String> value){
        return m_datas[number].getKv(index, value);
    }

    public int getKvCount(int number){
        return m_datas[number].getKvCount();
    }

    public void load(){
        try {
            String dir = m_path;
            boolean hasFile = false;
            String vFile = dir + "/info.txt";
            int oldNumber = m_number;
            if (FCFile.isFileExist(vFile)) {
                String content = "";
                RefObject<String> refContent = new RefObject<String>(content);
                FCFile.read(vFile, refContent);
                content = refContent.argvalue;
                oldNumber = FCTran.strToInt(content);
                hasFile = true;
            }else{
                if (!FCFile.isDirectoryExist(dir)) {
                    FCFile.createDirectory(dir);
                }
            }
            m_datas = new KVData2[m_number];
            for (int i = 0; i < m_number; i++) {
                KVData2 kvData = new KVData2();
                m_datas[i] = kvData;
                kvData.m_pos = i;
                kvData.m_kv = this;
            }
            if (m_fall) {
                if (!hasFile || oldNumber != m_number) {
                    FCFile.write(vFile, FCTran.intToStr(m_number));
                }
            }
        }catch (Exception ex){

        }
    }

    public void addValue( String value){
        try {
            m_datas[0].addValue(value.getBytes("GB2312"));
        }catch (Exception ex){

        }
    }

    public void addValue2(byte[] value){
        m_datas[0].addValue(value);
    }
}
