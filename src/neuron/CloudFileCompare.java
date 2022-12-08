/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuron;

/**
 *
 * @author taode
 */
public class CloudFileCompare implements java.util.Comparator<CloudFile> {
    public int compare(CloudFile x, CloudFile y) {
        return y.m_path.compareTo(x.m_path);
    }
}
