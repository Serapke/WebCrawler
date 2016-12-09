import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Developed by Mantas on 08/12/2016.
 */

public class WebCrawler {

    private static final String DOMAIN_REGEX = "http(s)?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}";
    private static final String ASSET_REGEX = "(http(s)?://)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\" +
            "b([-a-zA-Z0-9@:%_+.~#?&/=]*)/[a-z0-9]+\\.(jpg|png|css|js|ico|gif|flv)";


    private Page startingPage;
    private String domain;
    private Integer breadthLimit;

    private WebCrawler(String websiteUrl, Integer breadthLimit) {
        Pattern pattern = Pattern.compile(DOMAIN_REGEX);
        Matcher matcher = pattern.matcher(websiteUrl);
        if (matcher.find()) this.domain = matcher.group(0);
        else {
            System.err.println("Invalid domain: " + websiteUrl);
            return;
        }
        this.startingPage = new Page(domain);
        this.breadthLimit = breadthLimit;
    }

    WebCrawler(String websiteUrl) {
        Pattern pattern = Pattern.compile(DOMAIN_REGEX);
        Matcher matcher = pattern.matcher(websiteUrl);
        if (matcher.find()) this.domain = matcher.group(0);
        else {
            System.err.println("Invalid domain: " + websiteUrl);
            return;
        }
        this.startingPage = new Page(domain);
        this.breadthLimit = -1;
    }

    private void crawl() {
        HashSet<String> inQueue = new HashSet<String>();
        Queue<Page> toVisit = new LinkedList<Page>();

        toVisit.add(startingPage);
        inQueue.add(startingPage.url);

        while (!toVisit.isEmpty()) {
            Page visitingPage = toVisit.poll();

            getReachablePagesAndAssets(visitingPage);

            inQueue.add(visitingPage.url);

            for (Page p : visitingPage.pages) {
                if (!inQueue.contains(p.url)) {
                    inQueue.add(p.url);
                    toVisit.add(p);
                }
            }
        }
        printPages(startingPage);
    }

    private void getReachablePagesAndAssets(Page page)  {
        String content = "";

        try {
            content = getContent(page.url);
        } catch (IOException e) {
            System.err.println("Failed to get the page: " + page.url);
        }

        page.assets = getPageAssets(content);
        page.pages = getReachablePages(content);

    }

    void printPages(Page page) {
        HashSet<String> inQueue = new HashSet<String>();
        Queue<Page> toVisit = new LinkedList<Page>();

        toVisit.add(page);
        inQueue.add(page.url);

        System.out.println("[");

        while (!toVisit.isEmpty()) {
            Page visitingPage = toVisit.poll();

            inQueue.add(visitingPage.url);

            for (Page p : visitingPage.pages) {
                if (!inQueue.contains(p.url)) {
                    inQueue.add(p.url);
                    toVisit.add(p);
                }
            }
            printPageInfo(visitingPage, toVisit);
        }
        System.out.println("]");
    }

    private void printPageInfo(Page page, Queue q) {
        int n = page.assets.size();
        System.out.println("  {");
        System.out.println("    \"url\": \"" + page.url + "\"");
        System.out.println("    \"assets\": [");
        for (int i = 0; i < n-1; i++) {
            System.out.println("      \"" + page.assets.get(i).url + "\",");
        }
        if (n > 0)
            System.out.println("      \"" + page.assets.get(n-1).url + "\"");
        System.out.println("    ]");
        System.out.print("  }");
        if (!q.isEmpty()) {
            System.out.println(",");
        } else {
            System.out.println("");
        }
    }

    private String getContent(String pageUrl) throws IOException {
        URL url = new URL(pageUrl);
        URLConnection connection = url.openConnection();
        Scanner scanner = new Scanner(connection.getInputStream());
        scanner.useDelimiter("\\Z");

        if (!scanner.hasNext())              // if page is empty
            return "";

        return scanner.next();
    }

    ArrayList<Asset> getPageAssets(String pageContent) {
        ArrayList<Asset> assets = new ArrayList<Asset>();
        HashSet<String> assetsUrl = new HashSet<String>();

        Pattern assetPattern = Pattern.compile(ASSET_REGEX);
        Matcher assetMatcher = assetPattern.matcher(pageContent);

        while (assetMatcher.find()) {
            String temp = assetMatcher.group(0);

            if (!assetsUrl.contains(temp)) {
                assetsUrl.add(temp);
            }
        }

        for (String assetUrl : assetsUrl) {
            assets.add(new Asset(assetUrl));
        }
        return assets;
    }

    ArrayList<Page> getReachablePages(String pageContent) {
        ArrayList<Page> pages = new ArrayList<Page>();
        HashSet<String> reachablePagesUrl = new HashSet<String>();
        Integer limit = this.breadthLimit;

        Pattern reachablePagePattern = Pattern.compile(this.domain + "([-a-zA-Z0-9:;_&?/=]*)(?!.*\\.)");
        Matcher reachablePageMatcher = reachablePagePattern.matcher(pageContent);

        while (reachablePageMatcher.find() && limit != 0) {
            String temp = reachablePageMatcher.group(0);
            // Links 'http://www.example.org' and 'http://www.example.org/' are identical
            // thus store links without '/' at the end
            if (temp.endsWith("/"))
                temp = temp.substring(0, temp.length() - 1);
            Page reachablePage = new Page(temp);

            if (!reachablePagesUrl.contains(reachablePage.url)) {
                reachablePagesUrl.add(reachablePage.url);
                limit--;
            }
        }

        for (String pageUrl : reachablePagesUrl) {
            pages.add(new Page(pageUrl));
        }
        return pages;
    }

     class Page {
        String url;
        ArrayList<Page> pages;
        ArrayList<Asset> assets;

        Page(String url) {
            this.url = url;
            this.pages = new ArrayList<Page>();
            this.assets = new ArrayList<Asset>();
        }
    }

    class Asset {
        String url;

        Asset(String url) {
            this.url = url;
        }
    }

    public static void main(String[] args) {
        // Crawling through a forum
        WebCrawler w = new WebCrawler("https://uzdarbis.lt/", 8);
        w.crawl();
    }
}
