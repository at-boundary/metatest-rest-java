package metatest.schemacoverage;

public class Collector {
    private static volatile CollectorData data = new CollectorData();

    public static synchronized CollectorData getData() {
        if (data == null) {
            data = new CollectorData();
        }
        return data;
    }
}
