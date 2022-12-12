package iKv;
import java.util.*;
import facecat.topin.core.*;

/*
* 数据库组
*/
public class KVDataBase2 {
    /*
    * 所有的库
    */
    public static HashMap<Integer, FCKVArray> m_dbs = new HashMap<Integer, FCKVArray>(); 
    
    /*
    * 数据库锁
    */
    public static Object m_dbLock = new Object();

    /*
    * 获取新的编号
    */
    public static int getNewID(){
        int newID = 0;
        for(Integer id : m_dbs.keySet()){
            if(newID <= id){
                newID = id + 1;
            }
        }
        return newID;
    }

    /*
    * 初始化数据库
    */
    public static int initDB(String path, String name){
        int newID = -1;
        synchronized(m_dbLock) {
            for(Integer id : m_dbs.keySet()){
                if (m_dbs.get(id).m_path.equals(path))
                {
                    newID = id;
                }
            }
            if (newID == -1) {
                newID = getNewID();
                FCKVArray kv = new FCKVArray();
                kv.setName(name);
                kv.setPath(path);
                kv.load();
                m_dbs.put(newID, kv);
            }
        }
        return newID;
    }

    /*
    * 初始化数据库，不占文件不起多线程
    */
    public static int initDB2(String path, String name){
        int newID = -1;
        synchronized(m_dbLock) {
            for(Integer id : m_dbs.keySet()){
                if (m_dbs.get(id).m_path.equals(path))
                {
                    newID = id;
                }
            }
            if (newID == -1) {
                newID = getNewID();
                FCKVArray kv = new FCKVArray();
                kv.setName(name);
                kv.setPath(path);
                kv.m_threadMode = false;
                kv.m_noTakeup = true;
                kv.load();
                m_dbs.put(newID, kv);
            }
        }
        return newID;
    }

    /*
    * 关闭数据库
    */
    public static int closeDB(int kID){
        int state = 0;
        synchronized (m_dbLock) {
            if(m_dbs.containsKey(kID)){
                FCKVArray kv3 = m_dbs.get(kID);
                kv3.close();
                kv3 = null;
                m_dbs.remove(kID);
                state = 1;
            }
        }
        return state;
    }

    /*
    * 关闭所有的数据库
    */
    public static int closeAllDBs(){
        ArrayList<Integer> kvIDs = new ArrayList<Integer>();
        synchronized (m_dbLock) {
            for(Integer id : m_dbs.keySet()){
                kvIDs.add(id);
            }
        }
        for(int i = 0; i < (int)kvIDs.size(); i++) {
            closeDB(kvIDs.get(i));
        }
        return 1;
    }

    /*
    * 根据索引获取值
    */
    public static int getValue(int kID, int index, RefObject<String> value){
        int state = 0;
        synchronized (m_dbLock) {
            if(m_dbs.containsKey(kID)){
                FCKVArray kv3 = m_dbs.get(kID);
                String strValue = kv3.getValue(index);
                value.argvalue = strValue;
                state = 1;
            }else{
                value.argvalue = "nil";
            }
        }
        return state;
    }

    /*
    * 根据索引获取流
    */
    public static byte[] getValue(int kID, int index){
        byte[] value = null;
        int state = 0;
        synchronized (m_dbLock) {
            if(m_dbs.containsKey(kID)){
                FCKVArray kv3 = m_dbs.get(kID);
                value = kv3.getValue2(index);
            }
        }
        return value;
    }

    /*
    * 获取数据量
    */
    public static int getValueCount(int kID){
        int count = 0;
        synchronized (m_dbLock) {
            if(m_dbs.containsKey(kID)){
                FCKVArray kv3 = m_dbs.get(kID);
                count = kv3.getKvCount(0);
            }
        }
        return count;
    }

    /*
    * 添加数据
    */
    public static int addValue(int kID, String value){
        int state = 0;
        synchronized (m_dbLock) {
            if(m_dbs.containsKey(kID)) {
                FCKVArray kv3 = m_dbs.get(kID);
                kv3.addValue(value);
                state = 1;
            }
        }
        return state;
    }

    /*
    * 添加流数据
    */
    public static int addValue(int kID, byte[] value){
        int state = 0;
        synchronized (m_dbLock) {
            if(m_dbs.containsKey(kID)) {
                FCKVArray kv3 = m_dbs.get(kID);
                kv3.addValue2(value);
                state = 1;
            }
        }
        return state;
    }
}
