
package neuron;

/*
* 云文件排序比较
*/
public class CloudFileCompare implements java.util.Comparator<CloudFile> {
    public int compare(CloudFile x, CloudFile y) {
        return y.m_path.compareTo(x.m_path);
    }
}
