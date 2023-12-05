package de.samply.converter;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

public class ConverterGraph {

    private final Map<Format, List<Converter>> inputFormatConvertersMap = new HashMap<>();
    private final Map<Format, List<Converter>> outputFormatConvertersMap = new HashMap<>();
    private final Graph<Converter, DefaultEdge> convertersGraph = new SimpleGraph<>(DefaultEdge.class);
    private final DijkstraShortestPath<Converter, DefaultEdge> dijkstraAlgorithm;
    private final Map<String, Optional<Converter>> shortPathCache = new HashMap<>();

    public ConverterGraph(List<Converter> converters) {
        fillInputAndOutputFormatConvertersMap(converters);
        generateGraph(converters);
        this.dijkstraAlgorithm = new DijkstraShortestPath<>(convertersGraph);
    }

    private void fillInputAndOutputFormatConvertersMap(List<Converter> converters) {
        converters.forEach(converter -> {
            addConverterToInputFormatConvertersMap(converter);
            addConverterToOutputFormatConvertersMap(converter);
        });
    }

    private void addConverterToInputFormatConvertersMap(Converter converter) {
        List<Converter> tempConverters = inputFormatConvertersMap.get(converter.getInputFormat());
        if (tempConverters == null) {
            tempConverters = new ArrayList<>();
            inputFormatConvertersMap.put(converter.getInputFormat(), tempConverters);
        }
        tempConverters.add(converter);
    }

    private void addConverterToOutputFormatConvertersMap(Converter converter) {
        List<Converter> tempConverters = outputFormatConvertersMap.get(converter.getOutputFormat());
        if (tempConverters == null) {
            tempConverters = new ArrayList<>();
            outputFormatConvertersMap.put(converter.getOutputFormat(), tempConverters);
        }
        tempConverters.add(converter);
    }


    private void generateGraph(List<Converter> converters) {
        converters.forEach(convertersGraph::addVertex);
        converters.forEach(converter -> {
            Optional.ofNullable(inputFormatConvertersMap.get(converter.getOutputFormat())).ifPresent(tempConverters ->
                    tempConverters.stream().filter(c2 -> areCompatible(converter, c2)).
                            forEach(converter2 -> convertersGraph.addEdge(converter, converter2)));
            Optional.ofNullable(outputFormatConvertersMap.get(converter.getInputFormat())).ifPresent(tempConverters ->
                    tempConverters.stream().filter(c2 -> areCompatible(converter, c2))
                            .forEach(converter2 -> convertersGraph.addEdge(converter2, converter)));
        });
    }

    private boolean areCompatible(Converter converter1, Converter converter2) {
        if (converter1 instanceof SourceConverter
                && converter2 instanceof SourceConverter
                && !((SourceConverter) converter1).getSourceId().equals(((SourceConverter) converter2).getSourceId())) {
            return false;
        }
        if (converter1 instanceof TargetConverter
                && converter2 instanceof TargetConverter
                && !((TargetConverter) converter1).getTargetId().equals(((TargetConverter) converter2).getTargetId())) {
            return false;
        }
        return true;
    }

    public Optional<Converter> fetchShortestPath(Format inputFormat, Format outputFormat, Optional<String> sourceId, Optional<String> targetId) {
        String key = fetchKey(inputFormat, outputFormat, sourceId, targetId);
        Optional<Converter> finalResult = shortPathCache.get(key);
        if (finalResult == null) {
            List<Converter> inputConverters = inputFormatConvertersMap.get(inputFormat).stream()
                    .filter(c -> correspondsConverterToSource(c, sourceId)).toList();
            List<Converter> outputConverters = outputFormatConvertersMap.get(outputFormat).stream()
                    .filter(c -> correspondsConverterToTarget(c, targetId)).toList();
            if (inputConverters.isEmpty() || outputConverters.isEmpty()) {
                return Optional.empty();
            }
            List<ConverterGroup> result = new ArrayList<>();
            inputConverters.forEach(inputConverter ->
                    outputConverters.forEach(outputConverter -> result.add(fetchShortestPath(inputConverter, outputConverter))));
            Collections.sort(result, Comparator.comparingInt(converterGroup -> converterGroup.getConverters().size()));
            finalResult = (result.size() > 0) ? Optional.of(result.get(0)) : Optional.empty();
            shortPathCache.put(key, finalResult);
        }
        return finalResult;
    }

    private boolean correspondsConverterToSource(Converter converter, Optional<String> sourceId) {
        return sourceId.isEmpty() ||
                ((converter instanceof SourceConverter) && ((SourceConverter) converter).getSourceId().equals(sourceId.get()));
    }

    private boolean correspondsConverterToTarget(Converter converter, Optional<String> targetId) {
        return targetId.isEmpty() ||
                ((converter instanceof TargetConverter) && ((TargetConverter) converter).getTargetId().equals(targetId.get()));
    }

    private ConverterGroup fetchShortestPath(Converter inputConverter, Converter outputConverter) {
        return new ConverterGroup(dijkstraAlgorithm.getPath(inputConverter, outputConverter).getVertexList());
    }

    private String fetchKey(Format inputFormat, Format outputFormat, Optional<String> sourceId, Optional<String> targetId) {
        return inputFormat.name() + '|' + outputFormat.name() + '|' +
                ((sourceId.isPresent()) ? sourceId.get() : "") + '|' + ((targetId.isPresent()) ? targetId.get() : "");
    }

}
