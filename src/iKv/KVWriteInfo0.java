package iKv;

/*
* 临时写入缓存
*/
public class KVWriteInfo0 {
    public byte[] m_key; //键的流
    public byte[] m_value; //值的流
    public int m_end; //结束位置
    public int m_start; //开始位置
    public int m_oldStart; //上次开始的位置
    public int m_oldEnd; //上次结束的位置
}

