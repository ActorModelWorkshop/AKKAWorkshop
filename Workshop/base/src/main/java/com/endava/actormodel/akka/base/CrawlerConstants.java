package com.endava.actormodel.akka.base;

/**
 * Crawler constants
 */
public class CrawlerConstants {

    /* Actors names / paths */
    public static final String SYSTEM_CRAWLER = "CrawlerSystem";
    public static final String SYSTEM_DOWNLOADER = "DownloaderSystem";

    public static final String MASTER_ACTOR_NAME = "masterActor";

    public static final String DOMAIN_MASTER_ACTOR_NAME = "domainMasterActor";
    public static final String PROCESSING_MASTER_ACTOR_NAME = "processingMasterActor";
    public static final String PERSISTENCE_MASTER_ACTOR_NAME = "persistenceMasterActor";

    public static final String DOMAIN_ACTOR_PART_NAME = "domainActor_";

    /* HTTP related constants */
    public static final String HTTP_CUSTOM_HEADER_RESPONSE_CODE = "CRAWL-ResponseCode";

    public static final String HTTP_RESPONSE_CODE_NONE = "0";
    public static final String HTTP_RESPONSE_CODE_OK = "200";

    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

    public static final String[] ACCEPTED_MIME_TYPES = new String[]{"text/html"}; // http://en.wikipedia.org/wiki/Internet_media_type
    public static final String[] ACCEPTED_PROTOCOLS = new String[]{"http", "https"};

    /* Other */
    public static final int DOMAINS_CRAWL_MAX_COUNT = 5; // Maximum number of domains to crawl at one time
    public static final long DOMAINS_REFRESH_PERIOD = 1 * 60 * 1000; // 1 minute(s)
    public static final long DOMAIN_DEFAULT_COOLDOWN = 30 * 1000; // 30 seconds

    public static final int CONNECTION_EXCEPTION_TRIALS = 3; // the number of trials for a domain that throws IOException and HttpHostConnectException
    public static final long EMPTY_NEXT_LINK_TRIALS = 10;
}
