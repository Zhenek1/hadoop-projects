import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 21.05.15.
 */
public class MyJsonParser {

    public static JSONObject obj, site, user;
    public static String user_id;
    public static  List<String> existingSiteUrls;

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
        return true;
    }
    public static List<String> getExistingURLs(JSONObject site) {
        List<String> existing_urls = new ArrayList<String>();
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

}
