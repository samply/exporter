package de.samply.fhir.cql;

import de.samply.template.ConverterTemplate;
import de.samply.template.CqlTemplate;
import de.samply.template.token.TokenTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CqlExtractorTest {


    private final String tokenKey = "DKTK_STRAT_AGE_STRATIFIER";
    private final String tokenValue = "define AgeClass:\\nif (Patient.birthDate is null) then 'unknown' else ToString((AgeInYears() div 10) * 10)";
    private final String encodedCqlQuery = "eyJsYW5nIjoiY3FsIiwibGliIjp7InJlc291cmNlVHlwZSI6IkxpYnJhcnkiLCJ1cmwiOiJ1cm46dXVpZDpjODJkMTg0Yi1iZDU1LTRiZjUtYjFmYi0xOGRiZmJjNzcwMDMiLCJzdGF0dXMiOiJhY3RpdmUiLCJ0eXBlIjp7ImNvZGluZyI6W3sic3lzdGVtIjoiaHR0cDovL3Rlcm1pbm9sb2d5LmhsNy5vcmcvQ29kZVN5c3RlbS9saWJyYXJ5LXR5cGUiLCJjb2RlIjoibG9naWMtbGlicmFyeSJ9XX0sImNvbnRlbnQiOlt7ImNvbnRlbnRUeXBlIjoidGV4dC9jcWwiLCJkYXRhIjoiYkdsaWNtRnllU0JTWlhSeWFXVjJaUXAxYzJsdVp5QkdTRWxTSUhabGNuTnBiMjRnSnpRdU1DNHdKd3BwYm1Oc2RXUmxJRVpJU1ZKSVpXeHdaWEp6SUhabGNuTnBiMjRnSnpRdU1DNHdKd29LWTI5a1pYTjVjM1JsYlNCc2IybHVZem9nSjJoMGRIQTZMeTlzYjJsdVl5NXZjbWNuQ21OdlpHVnplWE4wWlcwZ2FXTmtNVEE2SUNkb2RIUndPaTh2Wm1ocGNpNWtaUzlEYjJSbFUzbHpkR1Z0TDJKbVlYSnRMMmxqWkMweE1DMW5iU2NLWTI5a1pYTjVjM1JsYlNCdGIzSndhRG9nSjNWeWJqcHZhV1E2TWk0eE5pNDROREF1TVM0eE1UTTRPRE11Tmk0ME15NHhKd29LWTI5dWRHVjRkQ0JRWVhScFpXNTBDZ3BFUzFSTFgxTlVVa0ZVWDBkRlRrUkZVbDlUVkZKQlZFbEdTVVZTQ2dwRVMxUkxYMU5VVWtGVVgwRkhSVjlUVkZKQlZFbEdTVVZTQ2dwRVMxUkxYMU5VVWtGVVgwUkZRMFZCVTBWRVgxTlVVa0ZVU1VaSlJWSUtDa1JMVkV0ZlUxUlNRVlJmUkVsQlIwNVBVMGxUWDFOVVVrRlVTVVpKUlZJS0NrUkxWRXRmVTFSU1FWUmZVMUJGUTBsTlJVNWZVMVJTUVZSSlJrbEZVZ29LUkV0VVMxOVRWRkpCVkY5UVVrOURSVVJWVWtWZlUxUlNRVlJKUmtsRlVnb0tSRXRVUzE5VFZGSkJWRjlOUlVSSlEwRlVTVTlPWDFOVVVrRlVTVVpKUlZJS1JFdFVTMTlUVkZKQlZGOUVSVVpmU1U1ZlNVNUpWRWxCVEY5UVQxQlZURUZVU1U5T0NpaFFZWFJwWlc1MExtZGxibVJsY2lBOUlDZHRZV3hsSnlrZ1lXNWtDaWdvS0dWNGFYTjBjeUJiUTI5dVpHbDBhVzl1T2lCRGIyUmxJQ2RFTkRNdU1TY2dabkp2YlNCcFkyUXhNRjBwSUc5eUNpaGxlR2x6ZEhNZ1cwTnZibVJwZEdsdmJqb2dRMjlrWlNBblJEUXpMakluSUdaeWIyMGdhV05rTVRCZEtTQnZjZ29vWlhocGMzUnpJRnREYjI1a2FYUnBiMjQ2SUVOdlpHVWdKMFEwTXk0d0p5Qm1jbTl0SUdsalpERXdYU2tnYjNJS0tHVjRhWE4wY3lCYlEyOXVaR2wwYVc5dU9pQkRiMlJsSUNkRU5ETXVNeWNnWm5KdmJTQnBZMlF4TUYwcElHOXlDaWhsZUdsemRITWdXME52Ym1ScGRHbHZiam9nUTI5a1pTQW5SRFF6TGpRbklHWnliMjBnYVdOa01UQmRLU0J2Y2dvb1pYaHBjM1J6SUZ0RGIyNWthWFJwYjI0NklFTnZaR1VnSjBRME15NDNKeUJtY205dElHbGpaREV3WFNrZ2IzSUtLR1Y0YVhOMGN5QmJRMjl1WkdsMGFXOXVPaUJEYjJSbElDZEVORE11T1NjZ1puSnZiU0JwWTJReE1GMHBLU0JoYm1RS0tDaGxlR2x6ZEhNZ1puSnZiU0JiVDJKelpYSjJZWFJwYjI0NklFTnZaR1VnSnpVNU9EUTNMVFFuSUdaeWIyMGdiRzlwYm1OZElFOEtkMmhsY21VZ1R5NTJZV3gxWlM1amIyUnBibWN1WTI5a1pTQmpiMjUwWVdsdWN5QW5PVE00TXk4eEp5a2diM0lLS0dWNGFYTjBjeUJtY205dElGdFBZbk5sY25aaGRHbHZiam9nUTI5a1pTQW5OVGs0TkRjdE5DY2dabkp2YlNCc2IybHVZMTBnVHdwM2FHVnlaU0JQTG5aaGJIVmxMbU52WkdsdVp5NWpiMlJsSUdOdmJuUmhhVzV6SUNjNU16ZzBMekVuS1NCdmNnb29aWGhwYzNSeklHWnliMjBnVzA5aWMyVnlkbUYwYVc5dU9pQkRiMlJsSUNjMU9UZzBOeTAwSnlCbWNtOXRJR3h2YVc1alhTQlBDbmRvWlhKbElFOHVkbUZzZFdVdVkyOWthVzVuTG1OdlpHVWdZMjl1ZEdGcGJuTWdKemt6T1RRdk1TY3BJRzl5Q2lobGVHbHpkSE1nWm5KdmJTQmJUMkp6WlhKMllYUnBiMjQ2SUVOdlpHVWdKelU1T0RRM0xUUW5JR1p5YjIwZ2JHOXBibU5kSUU4S2QyaGxjbVVnVHk1MllXeDFaUzVqYjJScGJtY3VZMjlrWlNCamIyNTBZV2x1Y3lBbk9UUXlNUzh4SnlrcEtRPT0ifV19LCJtZWFzdXJlIjp7InJlc291cmNlVHlwZSI6Ik1lYXN1cmUiLCJ1cmwiOiJ1cm46dXVpZDozMzY2MThhMS1hNjE4LTQ4M2ItYjk3OS04OWFlMjRkY2RkMTciLCJzdGF0dXMiOiJhY3RpdmUiLCJzdWJqZWN0Q29kZWFibGVDb25jZXB0Ijp7ImNvZGluZyI6W3sic3lzdGVtIjoiaHR0cDovL2hsNy5vcmcvZmhpci9yZXNvdXJjZS10eXBlcyIsImNvZGUiOiJQYXRpZW50In1dfSwibGlicmFyeSI6InVybjp1dWlkOmM4MmQxODRiLWJkNTUtNGJmNS1iMWZiLTE4ZGJmYmM3NzAwMyIsInNjb3JpbmciOnsiY29kaW5nIjpbeyJzeXN0ZW0iOiJodHRwOi8vdGVybWlub2xvZ3kuaGw3Lm9yZy9Db2RlU3lzdGVtL21lYXN1cmUtc2NvcmluZyIsImNvZGUiOiJjb2hvcnQifV19LCJncm91cCI6W3siY29kZSI6eyJ0ZXh0IjoicGF0aWVudHMifSwicG9wdWxhdGlvbiI6W3siY29kZSI6eyJjb2RpbmciOlt7InN5c3RlbSI6Imh0dHA6Ly90ZXJtaW5vbG9neS5obDcub3JnL0NvZGVTeXN0ZW0vbWVhc3VyZS1wb3B1bGF0aW9uIiwiY29kZSI6ImluaXRpYWwtcG9wdWxhdGlvbiJ9XX0sImNyaXRlcmlhIjp7Imxhbmd1YWdlIjoidGV4dC9jcWwtaWRlbnRpZmllciIsImV4cHJlc3Npb24iOiJJbkluaXRpYWxQb3B1bGF0aW9uIn19XSwic3RyYXRpZmllciI6W3siY29kZSI6eyJ0ZXh0IjoiR2VuZGVyIn0sImNyaXRlcmlhIjp7Imxhbmd1YWdlIjoidGV4dC9jcWwiLCJleHByZXNzaW9uIjoiR2VuZGVyIn19LHsiY29kZSI6eyJ0ZXh0IjoiNzUxODYtNyJ9LCJjcml0ZXJpYSI6eyJsYW5ndWFnZSI6InRleHQvY3FsIiwiZXhwcmVzc2lvbiI6IkRlY2Vhc2VkIn19LHsiY29kZSI6eyJ0ZXh0IjoiQWdlIn0sImNyaXRlcmlhIjp7Imxhbmd1YWdlIjoidGV4dC9jcWwiLCJleHByZXNzaW9uIjoiQWdlQ2xhc3MifX1dfSx7ImNvZGUiOnsidGV4dCI6ImRpYWdub3NpcyJ9LCJleHRlbnNpb24iOlt7InVybCI6Imh0dHA6Ly9obDcub3JnL2ZoaXIvdXMvY3FmbWVhc3VyZXMvU3RydWN0dXJlRGVmaW5pdGlvbi9jcWZtLXBvcHVsYXRpb25CYXNpcyIsInZhbHVlQ29kZSI6IkNvbmRpdGlvbiJ9XSwicG9wdWxhdGlvbiI6W3siY29kZSI6eyJjb2RpbmciOlt7InN5c3RlbSI6Imh0dHA6Ly90ZXJtaW5vbG9neS5obDcub3JnL0NvZGVTeXN0ZW0vbWVhc3VyZS1wb3B1bGF0aW9uIiwiY29kZSI6ImluaXRpYWwtcG9wdWxhdGlvbiJ9XX0sImNyaXRlcmlhIjp7Imxhbmd1YWdlIjoidGV4dC9jcWwtaWRlbnRpZmllciIsImV4cHJlc3Npb24iOiJEaWFnbm9zaXMifX1dLCJzdHJhdGlmaWVyIjpbeyJjb2RlIjp7InRleHQiOiJkaWFnbm9zaXMifSwiY3JpdGVyaWEiOnsibGFuZ3VhZ2UiOiJ0ZXh0L2NxbC1pZGVudGlmaWVyIiwiZXhwcmVzc2lvbiI6IkRpYWdub3Npc0NvZGUifX1dfSx7ImNvZGUiOnsidGV4dCI6InNwZWNpbWVuIn0sImV4dGVuc2lvbiI6W3sidXJsIjoiaHR0cDovL2hsNy5vcmcvZmhpci91cy9jcWZtZWFzdXJlcy9TdHJ1Y3R1cmVEZWZpbml0aW9uL2NxZm0tcG9wdWxhdGlvbkJhc2lzIiwidmFsdWVDb2RlIjoiU3BlY2ltZW4ifV0sInBvcHVsYXRpb24iOlt7ImNvZGUiOnsiY29kaW5nIjpbeyJzeXN0ZW0iOiJodHRwOi8vdGVybWlub2xvZ3kuaGw3Lm9yZy9Db2RlU3lzdGVtL21lYXN1cmUtcG9wdWxhdGlvbiIsImNvZGUiOiJpbml0aWFsLXBvcHVsYXRpb24ifV19LCJjcml0ZXJpYSI6eyJsYW5ndWFnZSI6InRleHQvY3FsLWlkZW50aWZpZXIiLCJleHByZXNzaW9uIjoiU3BlY2ltZW4ifX1dLCJzdHJhdGlmaWVyIjpbeyJjb2RlIjp7InRleHQiOiJzYW1wbGVfa2luZCJ9LCJjcml0ZXJpYSI6eyJsYW5ndWFnZSI6InRleHQvY3FsIiwiZXhwcmVzc2lvbiI6IlNhbXBsZVR5cGUifX1dfSx7ImNvZGUiOnsidGV4dCI6InByb2NlZHVyZXMifSwiZXh0ZW5zaW9uIjpbeyJ1cmwiOiJodHRwOi8vaGw3Lm9yZy9maGlyL3VzL2NxZm1lYXN1cmVzL1N0cnVjdHVyZURlZmluaXRpb24vY3FmbS1wb3B1bGF0aW9uQmFzaXMiLCJ2YWx1ZUNvZGUiOiJQcm9jZWR1cmUifV0sInBvcHVsYXRpb24iOlt7ImNvZGUiOnsiY29kaW5nIjpbeyJzeXN0ZW0iOiJodHRwOi8vdGVybWlub2xvZ3kuaGw3Lm9yZy9Db2RlU3lzdGVtL21lYXN1cmUtcG9wdWxhdGlvbiIsImNvZGUiOiJpbml0aWFsLXBvcHVsYXRpb24ifV19LCJjcml0ZXJpYSI6eyJsYW5ndWFnZSI6InRleHQvY3FsLWlkZW50aWZpZXIiLCJleHByZXNzaW9uIjoiUHJvY2VkdXJlIn19XSwic3RyYXRpZmllciI6W3siY29kZSI6eyJ0ZXh0IjoiUHJvY2VkdXJlVHlwZSJ9LCJjcml0ZXJpYSI6eyJsYW5ndWFnZSI6InRleHQvY3FsIiwiZXhwcmVzc2lvbiI6IlByb2NlZHVyZVR5cGUifX1dfSx7ImNvZGUiOnsidGV4dCI6Im1lZGljYXRpb25TdGF0ZW1lbnRzIn0sImV4dGVuc2lvbiI6W3sidXJsIjoiaHR0cDovL2hsNy5vcmcvZmhpci91cy9jcWZtZWFzdXJlcy9TdHJ1Y3R1cmVEZWZpbml0aW9uL2NxZm0tcG9wdWxhdGlvbkJhc2lzIiwidmFsdWVDb2RlIjoiTWVkaWNhdGlvblN0YXRlbWVudCJ9XSwicG9wdWxhdGlvbiI6W3siY29kZSI6eyJjb2RpbmciOlt7InN5c3RlbSI6Imh0dHA6Ly90ZXJtaW5vbG9neS5obDcub3JnL0NvZGVTeXN0ZW0vbWVhc3VyZS1wb3B1bGF0aW9uIiwiY29kZSI6ImluaXRpYWwtcG9wdWxhdGlvbiJ9XX0sImNyaXRlcmlhIjp7Imxhbmd1YWdlIjoidGV4dC9jcWwtaWRlbnRpZmllciIsImV4cHJlc3Npb24iOiJNZWRpY2F0aW9uU3RhdGVtZW50In19XSwic3RyYXRpZmllciI6W3siY29kZSI6eyJ0ZXh0IjoiTWVkaWNhdGlvblR5cGUifSwiY3JpdGVyaWEiOnsibGFuZ3VhZ2UiOiJ0ZXh0L2NxbCIsImV4cHJlc3Npb24iOiJQcm9jZWR1cmVUeXBlIn19XX1dfX0=";
    private CqlExtractor cqlExtractor = new CqlExtractor();
    private ConverterTemplate converterTemplate;

    @BeforeEach
    void setUp() {
        converterTemplate = new ConverterTemplate();
        CqlTemplate cqlTemplate = new CqlTemplate();
        converterTemplate.setCqlTemplate(cqlTemplate);
        List<TokenTemplate> tokens = new ArrayList<>();
        TokenTemplate token = new TokenTemplate();
        token.setKey(tokenKey);
        token.setValue(tokenValue);
        tokens.add(token);
        cqlTemplate.setTokens(tokens);
    }

    @Test
    void extract() throws CqlExtractorException {
        CqlQuery cqlQuery = cqlExtractor.extract(encodedCqlQuery, converterTemplate);
        assertTrue(new String(cqlQuery.library().getContent().get(0).getData()).contains(tokenValue));
    }


}