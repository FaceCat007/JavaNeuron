package iKv;
import java.util.*;
import java.io.File;
import java.io.RandomAccessFile;
import facecat.topin.core.*;

/*
* 内存数据库切片
*/
public class KVData2 {
    /*
    * 构造函数
    */
    public KVData2(){

    }

    /*
    * 析构函数
    */
    public void finalize() throws Throwable {
    }

    public Object m_lock = new Object(); //数据锁1
    public Object m_lock2 = new Object(); //数据锁2
    public int m_count; //数据量
    public long m_lastTime; //上次操作时间
    public boolean m_loaded; //是否已加载
    public FCKVArray m_kv; //所在数据库
    public ArrayList<FCValue0> m_list = new ArrayList<FCValue0>(); //包含数据
    public int m_pos; //写入位置
    public int m_totalSize; //总大小
    public ArrayList<KVWriteInfo0> m_writeInfos = new ArrayList<KVWriteInfo0>(); //写入缓存
    public String m_dbPath; //保存位置

    /*
    * 检查线程
    */
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while(true){
                int state = checkLoop();
                if(state == -1){
                    break;
                }else{
                    try {
                        Thread.sleep(1000);
                    }catch (Exception ex){

                    }
                }
            }
        }
    };

    /*
    * 检查循环
    */
    public int checkLoop(){
        int state = fallFile();
        if(m_kv.m_autoClose || m_kv.m_forceClose){
            long nowTime = getTickCount();
            long sub = nowTime - m_lastTime;
            if(sub > m_kv.getCloseTime() || m_kv.m_forceClose){
                closeCache();
                state = -1;
            }
        }
        return state;
    }

    /*
    * 关闭缓存
    */
    public void closeCache(){
        synchronized (m_lock) {
            fallFile();
            int mapSize = m_list.size();
            m_list.clear();
            if (m_kv.isFall()) {
                if(m_accessFile != null) {
                    try {
                        m_accessFile.close();
                    } catch (Exception ex) {

                    }
                    m_accessFile = null;
                }
            }
            m_loaded = false;
        }
        m_lastTime = getTickCount();
    }

    /*
    * 数据落地
    */
    public int fallFile(){
        int state = -1;
        if(m_kv.isFall()){
            ArrayList<KVWriteInfo0> writeInfos = new ArrayList<KVWriteInfo0>();
            synchronized (m_lock2) {
                for(int i = 0; i < m_writeInfos.size(); i++){
                    writeInfos.add(m_writeInfos.get(i));
                }
                m_writeInfos.clear();
            }
            if(writeInfos.size() > 0){
                try {
                    if(m_kv.m_noTakeup){
                        m_accessFile = new RandomAccessFile(new File(m_dbPath), "rwd");
                    }
                    for (KVWriteInfo0 writeInfo : writeInfos) {
                            int kvSizeT = writeInfo.m_end - writeInfo.m_start + 1;
                            int kv2 = writeInfo.m_start;
                            m_accessFile.seek(kv2);
                            byte[] buffer = new byte[4];
                            buffer[0] = (byte) (kvSizeT & 0xff);
                            buffer[1] = (byte) ((kvSizeT & 0xff00) >> 8);
                            buffer[2] = (byte) ((kvSizeT & 0xff0000) >> 16);
                            buffer[3] = (byte) ((kvSizeT & 0xff000000) >> 24);
                            m_accessFile.write(buffer);
                            if(writeInfo.m_value.length > 0) {
                                m_accessFile.write(writeInfo.m_value);
                            }
                    }
                    if(m_kv.m_noTakeup){
                        m_accessFile.close();
                        m_accessFile = null;
                    }
                    state = 1;
                    m_lastTime = getTickCount();
                }catch (Exception ex){
                }
            }
        }
        return state;
    }

    /*
    * 获取索引获取数据流
    */
    public byte[] getValue(int index){
        byte[] value = null;
        synchronized (m_lock) {
            if (!m_loaded) {
                load(m_kv.m_number);
                if(m_kv.m_threadMode){
                    new Thread(runnable).start();
                }
            }
             if (index < m_list.size())
            {
                    FCValue0 fcValue = m_list.get(index);
                    value = fcValue.m_text;
            }
        }
        m_lastTime = getTickCount();
        return value;
    }
    
    /*
    * 根据索引获取数据文本
    */
    public int getKv(int index, RefObject<String> value){
        int state = 0;
        synchronized(m_lock) {
            if (!m_loaded) {
                load(m_kv.m_number);
                if(m_kv.m_threadMode){
                    new Thread(runnable).start();
                }
            }
            if (index < m_list.size())
            {
                    FCValue0 fcValue = m_list.get(index);
                    value.argvalue = bytesToStr(fcValue.m_text);
                    state = 1;
            }
        }
        m_lastTime = getTickCount();
        return state;
    }
    
    /*
    * 获取数据量
    */
    public int getKvCount(){
        int count = 0;
        synchronized (m_lock) {
            if (!m_loaded) {
                load(m_kv.m_number);
                if(m_kv.m_threadMode){
                    new Thread(runnable).start();
                }
            }
            count = m_list.size();
        }
        m_lastTime = getTickCount();
        return count;
    }

    /*
    * 加载数据
    */
    public void load(int number) {
        try {
            if (m_kv.isFall()) {
                String sStrI = FCTran.intToStr(m_pos);
                String dir = m_kv.m_path;
                String strNumber = FCTran.intToStr(number);
                m_dbPath = dir + "/" + m_kv.m_name + "_" + strNumber + "_" + sStrI + ".db";
                if (FCFile.isFileExist(m_dbPath)) {
                    m_accessFile = new RandomAccessFile(new File(m_dbPath), "rwd");
                    int pos = 0;
                    long length = m_accessFile.length();
                    m_totalSize = (int) length;
                    while (pos < length) {
                        int start = pos;
                        m_accessFile.seek(start);
                        int totalLength = 0;
                        byte[] buffer = new byte[4];
                        m_accessFile.read(buffer);
                        totalLength = (int) ((buffer[0] & 0xFF)
                                | ((buffer[1] & 0xFF) << 8)
                                | ((buffer[2] & 0xFF) << 16)
                                | ((buffer[3] & 0xFF) << 24));
                        pos += 4;
                        if (totalLength > 1000000) {
                            break;
                        }
                        byte[] value = new byte[totalLength - 4];
                        if (value.length > 0) {
                               m_accessFile.read(value);
                        }
                        FCValue0 fcValue = new FCValue0();
                        fcValue.m_text = value;
                        m_list.add(fcValue);
                        fcValue.m_start = start;
                        fcValue.m_end = start + totalLength;
                        pos = start + totalLength;
                    }
                    if (m_kv.m_noTakeup) {
                        m_accessFile.close();
                    }
                } else {
                    if (!m_kv.m_noTakeup) {
                        m_accessFile = new RandomAccessFile(new File(m_dbPath), "rwd");
                    }
                }
            }
            m_lastTime = getTickCount();
            m_loaded = true;
        } catch (Exception ex) {
            m_loaded = false;
        }
    }

    private RandomAccessFile m_accessFile; //文件流

    /*
    * 添加数据
    */
    public void addValue(byte[] value){
        synchronized (m_lock) {
            try {
                if (!m_loaded) {
                    load(m_kv.m_number);
                    if(m_kv.m_threadMode){
                        new Thread(runnable).start();
                    }
                }
                 FCValue0 fcValue = new FCValue0();
                    fcValue.m_text = value;
                    m_list.add(fcValue);
                    if (m_kv.isFall()) {
                        int newLength = 4 + value.length;
                        int inputStart = -1, inputEnd = -1;
                        if (inputStart != -1) {
                            fcValue.m_start = inputStart;
                            fcValue.m_end = inputEnd;
                        } else {
                            fcValue.m_start = m_totalSize;
                            m_totalSize += newLength;
                            fcValue.m_end = m_totalSize - 1;
                        }
                        KVWriteInfo0 writeInfo = new KVWriteInfo0();
                        writeInfo.m_value = value;
                        writeInfo.m_start = fcValue.m_start;
                        writeInfo.m_end = fcValue.m_end;
                        writeInfo.m_oldStart = -1;
                        writeInfo.m_oldEnd = -1;
                        synchronized (m_lock2) {
                            m_writeInfos.add(writeInfo);
                        }
                    }
            }catch (Exception ex){

            }
        }
        if(!m_kv.m_threadMode) {
            fallFile();
        }
        m_lastTime = getTickCount();
    }

    /*
    * 获取当前毫秒数
    */
    public long getTickCount(){
        return System.currentTimeMillis();
    }

    /*
    * 流转字符串
    */
    public static String bytesToStr(byte[] bytes){
        int bytesSize = bytes.length;
        int pos = 0;
        for(int i = 0; i < bytesSize; i++){
            if(bytes[i] == 0){
                break;
            }
            pos++;
        }
        if(pos > 0){
            byte[] newBytes = new byte[pos];
            for(int i = 0; i < pos; i++){
                newBytes[i] = bytes[i];
            }
            try {
                return new String(newBytes, "GB2312");
            }catch (Exception ex){

            }
        }
        return "";
    }
}
