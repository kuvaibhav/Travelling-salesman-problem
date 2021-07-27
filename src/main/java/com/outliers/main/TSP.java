package com.outliers.main;

import com.outliers.algo.Algorithm;
import com.outliers.algo.aco.AntColonyOpt;
import com.outliers.algo.bnb.BranchAndBound;
import com.outliers.algo.bnb.PrimsBasedMSTHeuristic;
import com.outliers.graph.Graph;
import com.outliers.graph.Matrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class TSP {
    private static final Logger log = LogManager.getLogger(TSP.class);
    private static final Logger csvLog = LogManager.getLogger("csvLogger");

    public static void main(String[] args) throws IOException {

        log.info("Starting...");
        if (args.length < 2) {
            String message = "Two arguments are required. Algorithm type and input file path";
            log.error(message);
            throw new IllegalArgumentException(message);
        }

        // TODO Validate algoType
        String algoType = args[0];
        logToCsv(Arrays.asList("problem", "num_of_nodes", "distinct_values", "mean", "variance", "1_mst", "2_mst"));
        if (isFolderInput()) {
            String folderPath = args[1];
            runAlgorithm(algoType, scanFolder(folderPath));
        } else {
            String fileName = args[1];
            runAlgorithm(algoType, Collections.singletonList(fileName));
        }
    }

    private static void runAlgorithm(String algoType, List<String> filePaths) throws IOException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        for (String filePath : filePaths) {
            Matrix adjMatrix = parseInput(filePath);
            Graph graph = new Graph(adjMatrix);
            List<String> csvData = getMetadataFromFilePath(filePath);
            double oneMst = PrimsBasedMSTHeuristic.getMSTLowerBound(graph, Collections.singletonList(0));
            csvData.add(String.valueOf(oneMst));
            csvData.add(String.valueOf(oneMst * 2));

            Future<Boolean> future = service.submit(() -> {
                Algorithm algorithm = getAlgorithm(algoType, graph);
                try {
                    algorithm.run();
                } catch (Exception e) {
                    log.debug("Timeout reached", e);
                }
                csvData.addAll(algorithm.getValues());
                logToCsv(csvData);
                return true;
            });
            try {
                future.get(15, TimeUnit.MINUTES);
            } catch (TimeoutException e) {
                log.debug("Timeout reached");
                future.cancel(true);
            } catch (Exception e) {
                log.error("Something went wrong.", e);
            }
        }
        service.shutdown();
    }

    private static List<String> scanFolder(String folderPath) throws IOException {
        List<String> filePaths = new ArrayList<>();
        String glob = "glob:**/*.txt";
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
            glob);

        Files.walkFileTree(Paths.get(folderPath), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path,
                                             BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path)) {
                    filePaths.add(path.toString());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
                throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return filePaths;
    }

    private static List<String> getMetadataFromFilePath(String filePath) {
        String fileName = Paths.get(filePath).getFileName().toString();
        String[] split = fileName.split("-");
        if (split.length < 6) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(fileName, split[2], split[3], split[4], split[5]));
    }

    private static Algorithm getAlgorithm(String algoType, Graph graph) {
        if (algoType.equals("BNB")) {
            log.debug("BNB searching");
            return new BranchAndBound(graph, 0);
        } else {
            log.debug("ACO searching");
            return new AntColonyOpt(graph);
        }
    }

    private static void logToCsv(List<String> values) {
        csvLog.info(String.join(",", values));
    }

    private static boolean isFolderInput() {
        return System.getProperty("folder", "false").equalsIgnoreCase("true");
    }

    private static Matrix parseInput(String filePath) throws IOException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            int N = Integer.parseInt(lines.get(0));
            Matrix adjMatrix = new Matrix(N);
            for (int i = 1; i < lines.size(); i++) {
                String[] neighbors = lines.get(i).split("\\s");
                for (int j = 0; j < neighbors.length; j++) {
                    adjMatrix.set(i - 1, j, Double.parseDouble(neighbors[j]));
                }
            }
            return adjMatrix;
        } catch (IOException e) {
            log.error("File read failed", e);
            throw e;
        }
    }
}
