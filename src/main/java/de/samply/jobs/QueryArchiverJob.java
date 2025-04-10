package de.samply.jobs;

import de.samply.db.crud.ExporterDbService;
import de.samply.db.model.Query;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class QueryArchiverJob {

    private ExporterDbService exporterDbService;

    public QueryArchiverJob(ExporterDbService exporterDbService) {
        this.exporterDbService = exporterDbService;
    }

    @PostConstruct // Execute also on start up
    public void archiveQueries() {
        exporterDbService.fetchAllQueries().stream().filter(this::isQueryExpired).forEach(query -> {
            exporterDbService.archiveQuery(query.getId());
        });
    }

    private boolean isQueryExpired(Query query) {
        return query.getExpirationDate() != null && query.getExpirationDate().isBefore(LocalDate.now());
    }

}
