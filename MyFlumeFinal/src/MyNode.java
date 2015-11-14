import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import java.util.*;

/**
 * Created by user on 22.05.15.
 */
public class MyNode //implements Iterable<MyNode<T>> {
{
    MyNode parent;
    BiMap<String, MyNode> children;
    boolean isAd;
    public MyNode(boolean isAd) {
        this.children = Maps.synchronizedBiMap(HashBiMap.<String, MyNode>create());
        this.isAd = isAd;
    }
    public MyNode addChild(String data, boolean isAd) {
        MyNode childNode = new MyNode(isAd);
        childNode.parent = this;
        synchronized (children) {
            this.children.put(data, childNode);
        }
        return childNode;
    }

    public MyNode getChild_fromData(String data) {
        return children.get(data);
    }

    public void formAdsSet() {
        if (children.isEmpty()) {

            //All ads

          /*  List<String> l = new ArrayList<String>();
            for (MyNode node : parent.children.values())
                l.add(getFullStringRec2(node));
            MyUrlCrawler2.concatenated_data.add(stringIntersection(l));*/
            return;
        }
        synchronized (children) {
            for (MyNode node : children.values())
                if (node.isAd) {
                    String s = getFullStringRec(node);
                    MyCrawler.concatenated_data.add(s);
                } else {
                    node.formAdsSet();
                }
        }
    }

    public void setTreeAds() {
        if (parent == null || !parent.isAd)
            return;
        parent.isAd = false;
        parent.setTreeAds();
    }

    public void addNodeRecursive(Queue<String> data, boolean isAd) {
        if (data.isEmpty()) {
            return;
        }
        String url_part = data.remove();
        MyNode child = getChild_fromData(url_part);
        if (child == null) {
            //  if (!isAd) {
            child = addChild(url_part, isAd);

        }
        if (!isAd)
            child.isAd = false;
        child.addNodeRecursive(data, isAd);
    }

    public String stringIntersection(List<String> strings) {
        for (int prefixLen = 0; prefixLen < strings.get(0).length(); prefixLen++) {
            char c = strings.get(0).charAt(prefixLen);
            for (int i = 1; i < strings.size(); i++) {
                if ( prefixLen >= strings.get(i).length() ||
                        strings.get(i).charAt(prefixLen) != c ) {
                    // Mismatch found
                    return strings.get(i).substring(0, prefixLen);
                }
            }
        }
        return strings.get(0);
    }

    public boolean isAd_inArr(String s) {
        synchronized (MyCrawler.concatenated_data) {
            HashSet<String> hs = new HashSet<String>();
            for (String ad : MyCrawler.concatenated_data) {
                if (s.contains(ad))
                    return true;
            }
        }
        return false;
    }

    public String getFullStringRec(MyNode node) {
        if (node.parent == null)
            return "";
        String node_url_part = node.parent.children.inverse().get(node);
        if (node_url_part.contains("|")) {
            return getFullStringRec(node.parent) + node_url_part.replace("|", ".");
        } else {
            return getFullStringRec(node.parent) + node_url_part.replace("*", "") + "/";
        }
    }


    /*public String getFullStringRec(MyNode node) {
        if (node.parent == null)
            return "";
        String node_url_part = node.parent.children.inverse().get(node);
        if (node_url_part.contains("|")) {
            return getFullStringRec(node.parent) + node_url_part.replace("|", ".");
        } else {
            return getFullStringRec(node.parent) + node_url_part + "/";
        }
    }*/



}
