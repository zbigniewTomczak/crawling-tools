

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;

import com.googlecode.flaxcrawler.CrawlerConfiguration;
import com.googlecode.flaxcrawler.CrawlerController;
import com.googlecode.flaxcrawler.CrawlerException;
import com.googlecode.flaxcrawler.DefaultCrawler;
import com.googlecode.flaxcrawler.download.DefaultDownloaderController;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import com.googlecode.flaxcrawler.model.Page;
import com.googlecode.flaxcrawler.parse.DefaultParserController;

public class TidyCrawler {

	static private int counter = 0;
	
    public static void main(String[] args) throws MalformedURLException, CrawlerException {
    	Integer tidyCode = null;
    	String startAddress = null;
    	Integer seconds = null;
    	Integer depth = null;
    	try {
	    	for (int i = 0; i <args.length ; i++) {
				if (args[i].equals("-code")) {
					tidyCode = Integer.parseInt(args[i+1]);
				}
				if (args[i].equals("-depth")) {
					depth = Integer.parseInt(args[i+1]);
				}
				if (args[i].equals("-timeout")) {
					seconds = Integer.parseInt(args[i+1]);
				}
				if (args[i].equals("-start")) {
					startAddress = args[i+1];
					startAddress = Util.correctAddress(startAddress);
				}
			}
	    	if (tidyCode == null || startAddress == null || seconds == null || depth ==null) {
	    		throw new RuntimeException();
	    	}
    	} catch (Exception e) {
			printUsage();
			return;
		}
    	try {
    		crawl(startAddress, seconds, depth, tidyCode);
    	} catch (MalformedURLException e) {
    		System.out.println("Malformed URL");
    	}
    }

    private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("\tjava TidyCrawler -code tidy_error_code -depth crawling_depth -timeout timeout_in_seconds -start url");
		
	}

	public static void crawl(String startAddress, int seconds, int depth, int tidyCode) throws MalformedURLException, CrawlerException {
    	// Setting up downloader controller
        DefaultDownloaderController downloaderController = new DefaultDownloaderController();
        // Setting up parser controller
        DefaultParserController parserController = new DefaultParserController();

        // Creating crawler configuration object
        CrawlerConfiguration configuration = new CrawlerConfiguration();

        // Creating five crawlers (to work with 5 threads)
        for (int i = 0; i < 5; i++) {
            // Creating crawler and setting downloader and parser controllers
            DefaultCrawler crawler = new ExampleCrawler(tidyCode);
            crawler.setDownloaderController(downloaderController);
            crawler.setParserController(parserController);
            // Adding crawler to the configuration object
            configuration.addCrawler(crawler);
        }

        // Setting maximum parallel requests to a single site limit
        configuration.setMaxParallelRequests(1);
        // Setting http errors limits. If this limit violated for any
        // site - crawler will stop this site processing
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_CLIENT_TIMEOUT, 10);
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_BAD_GATEWAY, 10);
        // Setting period between two requests to a single site (in milliseconds)
        configuration.setPolitenessPeriod(50);
        configuration.setMaxLevel(depth);

        // Initializing crawler controller
        CrawlerController crawlerController = new CrawlerController(configuration);
        // Adding crawler seed
        crawlerController.addSeed(new URL(startAddress));
        // Starting and joining our crawler
        System.out.format("Starting crawling from %s%n", startAddress);
        crawlerController.start();
        System.out.format("Current timeout is %d seconds%n", seconds);
        crawlerController.join(100* seconds);
        //System.out.println(new Date() + "Stopping crawler");
        crawlerController.stop();
        
        System.out.println();
        System.out.println("Summary:");
        System.out.println("\tTotal Warnings code "+tidyCode+": " + counter);
    }
    /**
     * Custom crawler. Extends {@link DefaultCrawler}.
     */
    private static class ExampleCrawler extends DefaultCrawler {

    	private final int tidyCode;
    	
        public ExampleCrawler(int tidyCode) {
			this.tidyCode = tidyCode;
		}

		/**
         * This method is called after each crawl attempt.
         * Warning - it does not matter if it was unsuccessfull attempt or response was redirected.
         * So you should check response code before handling it.
         * @param crawlerTask
         * @param page
         */
        @Override
        protected void afterCrawl(CrawlerTask crawlerTask, final Page page) {
            super.afterCrawl(crawlerTask, page);

            if (page == null) {
                System.out.println(crawlerTask.getUrl() + " violates crawler constraints (content-type or content-length or other)");
            } else if (page.getResponseCode() >= 300 && page.getResponseCode() < 400) {
                // If response is redirected - crawler schedulles new task with new url
                System.out.println("Response was redirected from " + crawlerTask.getUrl());
            } else if (page.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Printing url crawled
                //System.out.println(crawlerTask.getUrl() + ". Found " + (page.getLinks() != null ? page.getLinks().size() : 0) + " links.");
                Tidy tidy = new Tidy();
                InputStream is = new ByteArrayInputStream(page.getContent());
                //tidy.setShowWarnings(false);
                OutputStream nullStream = new OutputStream() {
					
					@Override
					public void write(int b) throws IOException {
						//null stream
					}
				};
				PrintWriter pw = new PrintWriter(nullStream);
				tidy.setErrout(pw);
                tidy.setMessageListener(new TidyMessageListener() {
					
					public void messageReceived(TidyMessage m) {
						if (m.getErrorCode()==tidyCode) {
							System.out.println(page.getUrl()+"(line:"+ m.getLine() + ") " + m.getMessage());
							counter++;
						}
					}
				});
                tidy.parse(is, nullStream);
               // System.out.println();
                //System.out.println("Summary:");
                //System.out.println("\tParse Errors: "+tidy.getParseErrors());
                //System.out.println("Warnings: "+tidy.getParseWarnings());
                //System.out.println("Warnings code 3 count: " + counter);
                
            }
        }

        /**
         * You may check if you want to crawl next task
         * @param crawlerTask Task that is going to be crawled if you return {@code true}
         * @param parent parent.getUrl() page contain link to a crawlerTask.getUrl() or redirects to it
         * @return
         */
        @Override
        public boolean shouldCrawl(CrawlerTask crawlerTask, CrawlerTask parent) {
            // Default implementation returns true if crawlerTask.getDomainName() == parent.getDomainName()
            return super.shouldCrawl(crawlerTask, parent);
        }
    }
}
