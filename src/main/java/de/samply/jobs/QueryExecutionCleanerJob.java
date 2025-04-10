package de.samply.jobs;

import de.samply.db.crud.ExporterDbService;
import de.samply.db.model.QueryExecution;
import de.samply.db.model.QueryExecutionError;
import de.samply.exporter.ExporterConst;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class QueryExecutionCleanerJob {


    private ExporterDbService exporterDbService;

    public QueryExecutionCleanerJob(ExporterDbService exporterDbService) {
        this.exporterDbService = exporterDbService;
    }

    @PostConstruct // Execute on start up
    public void cleanOnStartUp() {
        exporterDbService.fetchRunningQueryExecutions().forEach(queryExecution -> {
            exporterDbService.setQueryExecutionAsError(queryExecution.getId());
            addQueryExecutionError(queryExecution);
        });

    }

    private void addQueryExecutionError(QueryExecution queryExecution) {
        QueryExecutionError queryExecutionError = new QueryExecutionError();
        queryExecutionError.setQueryExecutionId(queryExecution.getId());
        queryExecutionError.setError(ExporterConst.ERROR_MESSAGE_INTERRUPTED_QUERY_EXECUTION);
        exporterDbService.saveQueryExecutionErrorAndGetId(queryExecutionError);
    }

}
