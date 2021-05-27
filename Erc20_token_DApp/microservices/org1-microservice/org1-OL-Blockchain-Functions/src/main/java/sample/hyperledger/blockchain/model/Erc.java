package sample.hyperledger.blockchain.model;

public class Erc {
    private String id;
    private String id_to;
    private String value;

    public Erc() {
			
    }

    public Erc(String id, String id_to, String value) {
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

    public void setId(String id) {
        this.id = id;
    }

    public void setId_to(String id_to) {
        this.id_to = id_to;
    }

    public void setValue(String value) {
        this.value = value;
    }

}