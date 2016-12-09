import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Developed by Mantas on 09/12/2016.
 */
public class WebCrawlerTest {

    private static final String PAGE_CONTENT =
            "<html>\n" +
            "  <head>\n" +
            "    <script src=\"http://www.example.org/script.js\"></script>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <a href=\"http://www.example.org/about\">About</a>\n" +
            "    <p>Hello World!</p>\n" +
            "    <a href=\"http://www.example.org\">Reload</a>\n"+
            "    <img src=\"http://www.example.org/image.jpg\" />\n" +
            "  </body>\n" +
            "</html>";
    private static final String[] URLS = { "http://www.example.org", "http://www.example.org/about" };
    private static final String[] ASSETS = { "http://www.example.org/script.js", "http://www.example.org/image.jpg" };
    private static final String OUTPUT =
            "[\n" +
            "  {\n" +
            "    \"url\": \"http://www.example.org\"\n" +
            "    \"assets\": [\n" +
            "      \"http://www.example.org/script.js\",\n" +
            "      \"http://www.example.org/image.jpg\"\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"url\": \"http://www.example.org/about\"\n" +
            "    \"assets\": [\n" +
            "    ]\n" +
            "  }\n" +
            "]\n";

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
    }

    @SuppressWarnings("unused")
    @Test
    public void testInvalidDomain() {
        WebCrawler webCrawler = new WebCrawler("");
        assertEquals("Invalid domain: \n", errContent.toString());
    }

    @Test
    public void testGetReachablePages() {
        WebCrawler webCrawler = new WebCrawler("http://www.example.org");
        ArrayList<WebCrawler.Page> output = webCrawler.getReachablePages(PAGE_CONTENT);
        assertEquals(URLS[0], output.get(0).url);
        assertEquals(URLS[1], output.get(1).url);
    }

    @Test
    public void testGetAssets() {
        WebCrawler webCrawler = new WebCrawler("http://www.example.org");
        ArrayList<WebCrawler.Asset> output = webCrawler.getPageAssets(PAGE_CONTENT);
        assertEquals(ASSETS[0], output.get(0).url);
        assertEquals(ASSETS[1], output.get(1).url);
    }

    @Test
    public void testPrint() {
        WebCrawler webCrawler = new WebCrawler("http://www.example.org");
        WebCrawler.Page page = webCrawler.new Page("http://www.example.org");
        page.pages = webCrawler.getReachablePages(PAGE_CONTENT);
        page.assets = webCrawler.getPageAssets(PAGE_CONTENT);
        webCrawler.printPages(page);
        assertEquals(OUTPUT, outContent.toString());
    }
}
