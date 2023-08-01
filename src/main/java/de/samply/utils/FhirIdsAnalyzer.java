package de.samply.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class FhirIdsAnalyzer {

    private final Path path;
    private final String idToken = "<id value=\"";
    private final String referenceToken = "<reference value=\"";
    private Set<String> ressourceIds = new HashSet<>();
    private Set<String> ressourceReferenceIds = new HashSet<>();

    public FhirIdsAnalyzer(Path path) {
        this.path = path;
    }

    public void analyzeFile() throws IOException {
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                if (line.contains(idToken)) {
                    int index = line.indexOf(idToken);
                    String id = line.substring(index + idToken.length());
                    id = id.substring(0, id.indexOf("\""));
                    ressourceIds.add(id);
                } else if (line.contains(referenceToken)) {
                    int index = line.indexOf(referenceToken);
                    String id = line.substring(index + referenceToken.length());
                    id = id.substring(id.indexOf("/") + 1, id.indexOf("\""));
                    ressourceReferenceIds.add(id);
                }
            });
        }
    }

    public void logIds() {
        System.out.println("List of ressource ids: ");
        ressourceIds.forEach(id -> System.out.println(id));
        System.out.println("------------------------------------");
        System.out.println("List of ressource reference ids: ");
        ressourceReferenceIds.forEach(id -> System.out.println(id));
        System.out.println("------------------------------------");
        System.out.println("List of ressource reference ids of ressources who are not in the file: ");
        ressourceReferenceIds.forEach(referenceId -> {
            if (!ressourceIds.contains(referenceId)) {
                System.out.println(referenceId);
            }
        });
    }

    public static void main(String[] args) throws IOException {
        if (args.length >= 1) {
            Path file = Path.of(args[0]);
            FhirIdsAnalyzer fhirIdsAnalyzer = new FhirIdsAnalyzer(file);
            fhirIdsAnalyzer.analyzeFile();
            fhirIdsAnalyzer.logIds();
        } else {
            System.out.println("Insert file path to analyze");
        }

    }

}
