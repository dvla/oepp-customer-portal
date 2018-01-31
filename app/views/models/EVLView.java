package views.models;

import java.util.Map;
import utils.EVL;

public class EVLView {
    private EVL evl;
    private Map<String, String> fields;
    private String link;

    public EVLView(EVL evl, Map<String, String> fields, String link) {
        this.evl = evl;
        this.fields = fields;
        this.link = link;
    }

    public Map<String, String> getFields() { return fields; }

    public String getLink() {
        return link;
    }
}
