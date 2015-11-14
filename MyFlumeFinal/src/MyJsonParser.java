import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 21.05.15.
 */
public class MyJsonParser {

    public static JSONObject obj, site, user;
    public static String user_id;
    public static  Set<String> existingSiteUrls;

    public static boolean parse(String str) {
        obj = (JSONObject) JSONValue.parse(str);
        user = (JSONObject) obj.get("user");
        if (user.containsKey("id") && obj.containsKey("site")) {
            user_id = user.get("id").toString();
            site = (JSONObject) obj.get("site");
        } else {
            return false;
        }
        String exchange = obj.get("exchange").toString();
        user_id += ":" + exchange;
        existingSiteUrls = getExistingURLs(site);
    //    existingSiteUrls = removeQueries(existingSiteUrls);
        return true;
    }
    public static Set<String> getExistingURLs(JSONObject site) {
        Set<String> existing_urls = new HashSet<String>();
        if (site.containsKey("page")) {
            if (site.get("page").equals("") && site.containsKey("name") && !site.get("name").equals("")) {
                existing_urls.add("http://" + site.get("name").toString());
            }
            else
                // if (!site.get("page").toString().contains("ads.betweendigital.com"))
                existing_urls.add(site.get("page").toString());
        }
        if (site.containsKey("ref") && (site.get("ref").toString().contains("http://") || site.get("ref").toString().contains("https://"))
            // && !site.get("ref").toString().contains("ads.betweendigital.com")
                )
            existing_urls.add(site.get("ref").toString());
        if (!site.get("domain").equals(""))
            existing_urls.add("http://" + site.get("domain").toString());
        return existing_urls;
    }

    public static List<String> removeQueries(List<String> existing_urls) {
        List<String> l = new ArrayList<String>();
        for (String url : existing_urls)
            if (url.contains("?")) {
                l.add(url.substring(0, url.indexOf("?")));
            } else {
                l.add(url);
            }
        return l;
    }

}
