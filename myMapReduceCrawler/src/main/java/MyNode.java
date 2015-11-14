import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang.ArrayUtils;

import java.util.*;

/**
 * Created by user on 22.05.15.
 */
public class MyNode //implements Iterable<MyNode<T>> {
{
   // String data;
    MyNode parent;
  //  List<MyNode> children;
    BiMap<String, MyNode> children;
    boolean isAd;
    public MyNode(boolean isAd) {
      //  this.data = data;
     //   this.children = Collections.synchronizedList(new LinkedList<MyNode>());
        this.children = Maps.synchronizedBiMap(HashBiMap.<String, MyNode>create());
        this.isAd = isAd;
    }
    public MyNode addChild(String data, boolean isAd) {
        MyNode childNode = new MyNode(isAd);
        childNode.parent = this;
        synchronized (children) {
          //  this.children.add(childNode);
            this.children.put(data, childNode);
        }
        return childNode;
    }

    public MyNode getChild_fromData(String data) {
      /*  if (children.isEmpty())
            return null;
        synchronized (children) {
            for (MyNode child : children)
                if (child.getData().equals(data))
                    return child;
        }
        return null;*/
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
                    String s = getFullStringRec2(node);
                    MyUrlCrawler2.concatenated_data.add(s);
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
           //     setTreeAds();
         //   } else {
        /*    if (!isAd || !isAd_inArr(addPart(url_part, getFullStringRec())))
                child = addChild(url_part, isAd);*/
                //  if (!isAd_inArr(addPart(url_part, getFullStringRec())))
          //      child = addChild(url_part, isAd);
         //   }
          /*  if (isAd) {
                List<String> ads = new ArrayList<String>();
                synchronized (children) {
                    for (MyNode node : children.values()) {
                        if (node.isAd) {
                            System.out.println(node.getFullStringRec());
                            String node_string = node.getFullStringRec();
                            ads.add(node_string.substring(0, node_string.length() - 1));
                        }
                    }
                }
                if (ads.size() >= 10) {
                    String ads_intersection = stringIntersection(ads);
                    synchronized (MyUrlCrawler2.concatenated_data) {
                        Iterator<String> iter = MyUrlCrawler2.concatenated_data.iterator();
                        while (iter.hasNext()) {
                            if (iter.next().contains(ads_intersection))
                                iter.remove();
                        }
                    }
                    MyUrlCrawler2.concatenated_data.add(ads_intersection);
                }

            }*/
        }
        if (!isAd)
            child.isAd = false;
        child.addNodeRecursive(data, isAd);
    }

  /*  public void addNodeRecursive(Queue<String> data, boolean isAd) {
        if (data.isEmpty() || this.isAd) {
            this.isAd = isAd;
            return;
        }
        String url_part = data.remove();
        MyNode child = getChild_fromData(url_part);
        if (child == null) {
           // System.out.println(addPart(url_part, getFullStringRec()));
            if (!isAd || !isAd_inArr(addPart(url_part, getFullStringRec())))
                child = addChild(url_part);
            if (isAd) {
                List<String> ads = new ArrayList<String>();
                synchronized (children) {
                    for (MyNode node : children.values()) {
                        if (node.isAd) {
                            System.out.println(node.getFullStringRec());
                            String node_string = node.getFullStringRec();
                            ads.add(node_string.substring(0, node_string.length() - 1));
                        }
                    }
                }
                if (ads.size() >= 10) {
                    String ads_intersection = stringIntersection(ads);
                    synchronized (MyUrlCrawler2.concatenated_data) {
                        Iterator<String> iter = MyUrlCrawler2.concatenated_data.iterator();
                        while (iter.hasNext()) {
                            if (iter.next().contains(ads_intersection))
                                iter.remove();
                        }
                    }
                        MyUrlCrawler2.concatenated_data.add(ads_intersection);
                }

            }
        }
        if (child != null)
            child.addNodeRecursive(data, isAd);
    }*/


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
        synchronized (MyUrlCrawler2.concatenated_data) {
            HashSet<String> hs = new HashSet<String>();
            for (String ad : MyUrlCrawler2.concatenated_data) {
                if (s.contains(ad))
                    return true;
            }
        }
        return false;
    }

    public String getFullString() {
        String result = getFullStringRec();
        return result;
       /* if (result.equals(""))
            return "";
        else {
            System.out.println(result.substring(0, result.length() - 1));
            return result.substring(0, result.length() - 1);
        }*/
    }

    public String getFullStringRec() {
        if (parent == null)
            return "";
        String node_url_part = parent.children.inverse().get(this);
        if (node_url_part.contains("|")) {
            return parent.getFullStringRec() + node_url_part.replace("|", ".");
        } else {
            return parent.getFullStringRec() + node_url_part + "/";
        }
    }

    public String getFullStringRec2(MyNode node) {
        if (node.parent == null)
            return "";
        String node_url_part = node.parent.children.inverse().get(node);
        if (node_url_part.contains("|")) {
            return getFullStringRec2(node.parent) + node_url_part.replace("|", ".");
        } else {
            return getFullStringRec2(node.parent) + node_url_part + "/";
        }
    }

    public String addPart(String node_url_part, String url_string) {
       /* if (url_string.equals(""))
            return node_url_part.replace("|", "");
        if (node_url_part.contains("|"))
            return node_url_part.replace("|", ".");
        else
            return node_url_part + "/";*/
        return url_string + node_url_part.replace("|", "");
    }

    /*public MyNode getParent() {
        return this.parent;
    }

    public List<MyNode> getChildren() {
        return this.children;
    }

    public String getData() {
        return this.data;
    }*/

}
