package org.spacelab.housingutilitiessystemadmin.config;

//@Configuration
//@EnableMongoRepositories(basePackages = "org.spacelab.housingutilitiessystemadmin.repository")
public class MongoConfig {

//    @Value("${spring.data.mongodb.host}")
//    private String host;
//
//    @Value("${spring.data.mongodb.port}")
//    private int port;
//
//    @Value("${spring.data.mongodb.database}")
//    private String database;
//
//    @Value("${spring.data.mongodb.username}")
//    private String username;
//
//    @Value("${spring.data.mongodb.password}")
//    private String password;
//
//    @Value("${spring.data.mongodb.authentication-database}")
//    private String authDatabase;
//
//    @Bean
//    public MongoClient mongoClient() {
//        String connectionString = String.format(
//                "mongodb://%s:%s@%s:%d/%s?authSource=%s",
//                username, password, host, port, database, authDatabase
//        );
//
//        MongoClientSettings settings = MongoClientSettings.builder()
//                .applyConnectionString(new ConnectionString(connectionString))
//                .applyToConnectionPoolSettings(poolBuilder ->
//                    poolBuilder
//                        .maxSize(50)
//                        .minSize(5)
//                        .maxWaitTime(10, java.util.concurrent.TimeUnit.SECONDS)
//                        .maxConnectionLifeTime(30, java.util.concurrent.TimeUnit.MINUTES)
//                        .maxConnectionIdleTime(5, java.util.concurrent.TimeUnit.MINUTES)
//                )
//                .writeConcern(WriteConcern.UNACKNOWLEDGED)
//
//                .applyToSocketSettings(socketBuilder ->
//                    socketBuilder
//                        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
//                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)  // Увеличен для aggregation pipeline
//                )
//                .build();
//
//        return MongoClients.create(settings);
//    }
//
//    @Bean
//    public MongoTemplate mongoTemplate() {
//        MongoTemplate template = new MongoTemplate(mongoClient(), database);
//        template.setWriteResultChecking(WriteResultChecking.NONE);
//        return template;
//    }
}
