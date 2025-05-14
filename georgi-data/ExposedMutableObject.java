import java.util.ArrayList;
import java.util.List;

public class ExposedMutableObject {
    private List<String> items = new ArrayList<>();

    public List<String> getItems() {
        return items;
    }
}