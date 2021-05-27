import com.alibaba.fastjson.annotation.JSONField;

public class ERCjson {
    @JSONField(name = "id", ordinal = 1)
    private final String id;
    @JSONField(name = "id_to", ordinal = 2)
    private final String id_to;
    @JSONField(name = "value", ordinal = 3)
    private final String value;

    public ERCjson(String id, String id_to, String value) {
        this.id = id;
        this.id_to = id_to;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getId_to() {
        return id_to;
    }

    public String getValue() {
        return value;
    }


}
