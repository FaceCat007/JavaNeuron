package iKv;
import facecat.topin.core.*;

/*
* 本地顺序写入数据库
*/
public class FCKVArray {
    public boolean m_autoClose; //自动关闭
    public int m_closeTime = 10000; //闲置超时
    public KVData2 []m_datas; //分片存储的数据
    public boolean m_fall = true; //是否落地
    public boolean m_forceClose; //是否强制关闭
    public String m_name; //名称
    public int m_number = 1; //切割文件数
    public String m_path; //路径
    public int m_hashMode; //哈希模式
    
    /*
    * 构造函数
    */
    public FCKVArray(){

    }
    
    /*
    * 析构函数
    */
    public void finalize() throws Throwable {
    }
    
    /*
    * 是否自动关闭资源
    */
    public boolean autoClose(){
        return m_autoClose;
    }
    
    /*
    * 设置是否自动关闭资源
    */
    public void setAutoClose(boolean autoClose){
        m_autoClose = autoClose;
    }
    
    /*
    * 获取闲置关闭倒计时
    */
    public int getCloseTime(){
        return m_closeTime;
    }
    
    /*
    * 设置闲置关闭倒计时
    */
    public void setCloseTime(int closeTime){
        m_closeTime = closeTime;
    }
    
    /*
    * 是否落地
    */
    public boolean isFall(){
        return m_fall;
    }
    
    /*
    * 设置是否落地
    */
    public void setFall(boolean fall){
        m_fall = fall;
    }
    
    /*
    * 获取名称
    */
    public String getName(){
        return m_name;
    }
    
    /*
    * 设置名称
    */
    public void setName(String name){
        m_name = name;
    }
    
    /*
    * 获取切割文件数
    */
    public int getNumber(){
        return m_number;
    }
    
    /*
    * 设置切割文件数
    */
    public void setNumber(int number){
        m_number = number;
    }
    
    /*
    * 获取路径
    */
    public String getPath(){
        return m_path;
    }
    
    /*
    * 设置路径
    */
    public void setPath(String path){
        m_path = path;
    }

    /*
    * 是否使用多线程
    */
    public boolean m_threadMode = true;

    /*
    * 是否占用文件
    */
    public boolean m_noTakeup = false;

    /*
    * 关闭
    */
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

    /*
    * 根据索引获取数据
    */
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

    /*
    * 根据索引获取数据流
    */
    public byte[] getValue2(int index){
        return m_datas[0].getValue(index);
    }

    /*
    * 根据索引和切片号获取数据
    */
    public int getKv(int index, int number, RefObject<String> value){
        return m_datas[number].getKv(index, value);
    }

    /*
    * 根据切片号获取数据量
    */
    public int getKvCount(int number){
        return m_datas[number].getKvCount();
    }

    /*
    * 加载数据
    */
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

    /*
    * 添加数据
    */
    public void addValue( String value){
        try {
            m_datas[0].addValue(value.getBytes("GB2312"));
        }catch (Exception ex){

        }
    }

    /*
    * 添加流数据
    */
    public void addValue2(byte[] value){
        m_datas[0].addValue(value);
    }
}
