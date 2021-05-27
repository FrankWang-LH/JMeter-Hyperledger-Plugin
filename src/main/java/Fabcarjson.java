
import com.alibaba.fastjson.annotation.JSONField;

public class Fabcarjson {
    @JSONField(name = "make", ordinal = 1)
    private final String make;
    @JSONField(name = "model", ordinal = 2)
    private final String model;
    @JSONField(name = "colour", ordinal = 3)
    private final String colour;
    @JSONField(name = "owner", ordinal = 4)
    private final String owner;
    @JSONField(name = "key", ordinal = 5)
    private String key;

    public Fabcarjson(String make, String model, String colour, String owner, String key) {
        this.make = make;
        this.model = model;
        this.colour = colour;
        this.owner = owner;
        this.key = key;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getColour() {
        return colour;
    }

    public String getOwner() {
        return owner;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
