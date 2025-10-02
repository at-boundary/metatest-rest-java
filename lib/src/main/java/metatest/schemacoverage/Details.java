package metatest.schemacoverage;

import lombok.Data;

import java.util.List;

@Data
public class Details {
    private List<String> headers;
    private List<String> bodyFields;
    private List<String> urlParameters;
}
