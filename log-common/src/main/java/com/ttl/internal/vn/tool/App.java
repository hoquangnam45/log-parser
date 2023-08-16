package com.ttl.internal.vn.tool;

import com.zaxxer.hikari.HikariConfig;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.tools.Server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Collectors;

public class App {
    private final TransactionManager transactionManager;
    private static final Logger logger = LogManager.getLogger(LogIngestor.class);

    private static final Options options = new Options() {
        {
            addOption(Option.builder()
                    .desc("Db url. Default: jdbc:h2:mem:logger")
                    .longOpt("dbUrl")
                    .required(false)
                    .hasArg(true)
                    .build());
            addOption(Option.builder()
                    .desc("Db password. Default: \"\"")
                    .longOpt("dbPassword")
                    .required(false)
                    .hasArg(true)
                    .build());
            addOption(Option.builder()
                    .desc("Db password. Default: sa")
                    .longOpt("dbUsername")
                    .required(false)
                    .hasArg(true)
                    .build());
            addOption(Option.builder()
                    .desc("Path to log directories, comma-separated values.")
                    .longOpt("logDirectories")
                    .required(false)
                    .hasArg(true)
                    .build());
            addOption(Option.builder()
                    .desc("Pattern to find log files, comma-separated values. Using regex expression. Default: WV-ST((-(ERROR|EXCEPTION))|())-\\d{8}-\\d{4}.log")
                    .longOpt("logFilePatterns")
                    .required(false)
                    .hasArg(true)
                    .build());
            addOption(Option.builder()
                    .desc("Monitor change to log files")
                    .option("watch")
                    .hasArg(false)
                    .required(false)
                    .build());
            addOption(Option.builder()
                    .desc("Print help message and exit")
                    .option("h")
                    .longOpt("help")
                    .hasArg(false)
                    .required(false)
                    .build());
        }
    };

    public static void main(String[] args) throws ParseException, SQLException, ClassNotFoundException {
        CommandLine commandLine = parseArgs(args, options);

        boolean help = commandLine.hasOption("help");
        if (help) {
            printHelp(options);
            return;
        }
        String dbUsername = commandLine.getOptionValue("dbUsername", "sa");
        String dbPassword = commandLine.getOptionValue("dbPassword", "");
        String dbUrl = commandLine.getOptionValue("dbUrl", "jdbc:h2:mem:log");
        boolean watch = commandLine.hasOption("watch");
        Observable<File> logFileObservables = Single.just(commandLine.getOptionValue("logDirectories"))
                .filter(Objects::nonNull)
                .zipWith(
                        // Zip with file patterns
                        Maybe.just(
                                Single.just(commandLine.getOptionValue("logFilePatterns", "WV-ST((-(ERROR|EXCEPTION))|())-\\d{8}-\\d{4}.log"))
                                        .filter(Objects::nonNull)
                                        .flatMapObservable(it -> Observable.fromArray(it.split(",")))
                                        .map(String::trim)
                                        .collect(Collectors.toList())
                                        .map(filterPatterns -> WildcardFileFilter.builder().setWildcards(filterPatterns).get())
                                        .cache()
                        ),
                        Pair::of)
                .flatMapObservable(pair -> Observable.fromArray(pair.getLeft().split(","))
                        .map(String::trim)
                        .map(File::new)
                        .filter(File::isDirectory)
                        .flatMap(file ->
                                pair.getRight().toObservable()
                                        .map(filter -> file.listFiles((FilenameFilter) filter))
                                        .filter(Objects::nonNull)
                                        .flatMap(Observable::fromArray)
                        )
                        .filter(File::isFile)
                )
                .cache();

        if (useH2Server(dbUrl)) {
            startH2Database(dbUsername, dbPassword, dbUrl);
        }

        Single.just(createHikariConfig(dbUsername, dbPassword, dbUrl))
                .map(Datasource::new)
                .map(TransactionManager::new)
                .zipWith(Single.just(logFileObservables), Pair::of)
                .subscribeWith(new DisposableSingleObserver<Pair<TransactionManager, Observable<File>>>() {
                    @Override
                    public void onSuccess(@NonNull Pair<TransactionManager, Observable<File>> pair) {
                        pair.getValue()
                                .sorted((f1, f2) -> {
                                    // Sort the files so log ingestor will only watch the latest files
                                    try {
                                        BasicFileAttributes attrs1 = Files.readAttributes(f1.toPath(), BasicFileAttributes.class);
                                        BasicFileAttributes attrs2 = Files.readAttributes(f2.toPath(), BasicFileAttributes.class);
                                        return attrs1.creationTime().compareTo(attrs2.creationTime());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .map(file -> new LogIngestor(file))
                                .switchMap(ingestor -> ingestor.loadLogFile(watch, Duration.ofSeconds(1))
                                        .flatMap(ingestor::processLine)
                                        .doOnNext(ingestor::storeDeltaToDB)
                                        .observeOn(Schedulers.io()))
                                .subscribe();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        logger.error(e);
                    }
                });
    }

    public static CommandLine parseArgs(String[] args, Options options) {
        try {
            CommandLineParser parser = new DefaultParser();
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelp(options);
            System.exit(1);
            return null;
        }
    }

    public static void printHelp(Options options) {
        new HelpFormatter().printHelp("java -jar ${artifact}.jar <options> | java -cp <classpaths> "
                + LogIngestor.class.getName() + " <options>", options);
    }

    public App(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    private static boolean useH2Server(String dbUrl) {
        return dbUrl.toLowerCase().startsWith("jdbc:h2:");
    }

    private static HikariConfig createHikariConfig(String username, String password, String url) {
        HikariConfig config = new HikariConfig();
        config.setUsername(username);
        config.setPassword(password);
        config.setJdbcUrl(url);
        return config;
    }

    public static void startH2Database(String username, String password, String url) throws ClassNotFoundException, SQLException {
        Server.createTcpServer("-tcpAllowOthers").start();
        Class.forName("org.h2.Driver");
        DriverManager.getConnection(url, username, password);
    }
}
