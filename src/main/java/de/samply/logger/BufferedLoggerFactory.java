package de.samply.logger;

import com.google.common.collect.EvictingQueue;
import de.samply.exporter.ExporterConst;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BufferedLoggerFactory {

    private static EvictingQueue<String> evictingQueue = EvictingQueue.create(ExporterConst.BUFFERED_LOGGER_SIZE);
    ;

    public static Logger getLogger(Class clazz) {
        org.slf4j.Logger logger = LoggerFactory.getLogger(clazz);
        return new Logger(evictingQueue, logger);
    }

    public static String[] getLastLoggerLines(int numberOfLines, String lastLine) {
        List<String> result = new ArrayList<>();
        if (numberOfLines > 0) {
            ArrayList<String> lines = new ArrayList<>(evictingQueue);
            for (int i = lines.size() - 1; i >= 0 && i >= lines.size() - numberOfLines; i--) {
                String currentLine = lines.get(i);
                if (lastLine != null && currentLine.equals(lastLine)) {
                    break;
                } else {
                    result.add(currentLine);
                }
            }
        }
        Collections.reverse(result);
        return result.toArray(new String[0]);
    }


}
