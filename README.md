Web crawling tools
==============
Build

    mvn package
    
Response Code
----
Returns HTTP response code from HEAD request for provided address(es).

Run

    java -cp target/*.jar ResponseCode www.google.com
    java -cp target/*.jar ResponseCode -f hosts.txt

Tests

Will test all addresses in src/test/resources/code.200 for status code 200

    mvn test
    
Tidy Crawler
----
Crawls through your site and searches for tidy validation warnings.

Run

    java -cp target/*.jar -code 3 -depth 1 -timeout 300 -start www.google.com

Usage:
    
    java TidyCrawler -code tidy_error_code -depth crawling_depth -timeout timeout_in_seconds -start url

    
