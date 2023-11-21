package de.samply.fhir.cql;

import de.samply.template.ConverterTemplate;
import de.samply.template.token.TokenTemplate;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LibraryContentDataEditor {

    public static Library edit(Library library, ConverterTemplate template) {
        if (library != null) {
            library.setContent(library.getContent().stream().map(attachment -> edit(attachment, template)).toList());
        }
        return library;
    }

    private static Attachment edit(Attachment attachment, ConverterTemplate template) {
        String data = new String(attachment.getData());
        data = edit(data, template);
        attachment.setData(data.getBytes());
        return attachment;
    }

    private static String edit(String attachmentData, ConverterTemplate template) {
        AtomicReference<String> result = new AtomicReference<>(attachmentData);
        fetchTokens(template).forEach(tokenTemplate -> {
            result.set(result.get().replace(tokenTemplate.getKey(), tokenTemplate.getValue()));
        });
        return result.get();
    }

    private static List<TokenTemplate> fetchTokens(ConverterTemplate template) {
        if (template.getCqlTemplate() != null) {
            Collections.sort(template.getCqlTemplate().getTokens(), new TokenComparator());
            return template.getCqlTemplate().getTokens();
        } else {
            return new ArrayList<>();
        }
    }

    private static class TokenComparator implements Comparator<TokenTemplate> {
        @Override
        public int compare(TokenTemplate o1, TokenTemplate o2) {
            String key1 = o1.getKey();
            String key2 = o2.getKey();
            return (key1.length() == key2.length()) ? key2.compareTo(key1) : Integer.compare(key1.length(), key2.length());
        }
    }

}
