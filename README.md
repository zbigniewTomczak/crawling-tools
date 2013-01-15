Web crawling tools
==============
Response Code
----
Returns HTTP response code from HEAD request for provided address(es).

Build

    mvn package

Run

    java -cp target/*.jar ResponseCode www.google.com
    java -cp target/*.jar ResponseCode -f hosts.txt
