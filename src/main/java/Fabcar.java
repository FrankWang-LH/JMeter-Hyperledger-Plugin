import com.alibaba.fastjson.JSON;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Network;

import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * fabcar template
 *
 *  {
 *     "make": "VW",
 *     "model": "Golf",
 *     "colour": "white",
 *     "owner": "jake",
 *     "key": "CAR1223"
 *  }
 *
 */

public class Fabcar implements SmartContract {
    String contractName = "fabcar";
    String create = "createCar";
    String check = "queryCar";
    String update = "changeCarOwner";
    String delete = null;
    String queryAll = "queryAllCars";
    Network network = null;

    public Fabcar(Network network, boolean init) {
        this.network = network;
        if(init) {
            try {
                this.initLedger();
            } catch (Exception e) {
                System.out.println("Error: " + "Initialisation Failed");
            }
        }
    }

    @Override
    public void initLedger() throws InterruptedException, TimeoutException, ContractException {
        // DO NOTHING NOW
    }

    @Override
    public Message invoke(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        System.out.println("Attempting to create a new car.");
        Fabcarjson car = generate_JSON();
        contract.createTransaction("createCar").submit(car.getKey(), car.getMake(), car.getModel(), car.getColour(), car.getOwner());
        String output = JSON.toJSONString(car);
        String message = "Successfully added 1 new car with key:" + car.getKey();
        return new Message(200, message ,output);
    }

    @Override
    public Message read(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        if(args.length < 1) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        String key = args[0];
        System.out.println("Querying one car in the ledger with the key.");
        byte[] result_q1 = contract.evaluateTransaction("queryCar", key);
        String output = new String(result_q1);
        String message = "Successfully queried 1 car";
        return new Message(200, message ,output);
    }

    @Override
    public Message update(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        if(args.length < 2) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        String key = args[0];
        String owner = args[1];
        System.out.println("Attempting to update a car with owner.");
        byte[] result_q1 = contract.submitTransaction("changeCarOwner", key, owner);
        String output = new String(result_q1);
        String message = "Successfully updated a car with owner:" + owner;
        return new Message(200, message ,output);
    }

    @Override
    public Message delete(String[] args) throws InterruptedException, TimeoutException, ContractException {
        return new Message(404, "UNIMPLEMENTED - DELETE NOT AVAILABLE", null);
    }

    public Message queryAll(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        System.out.println("Querying all cars in the ledger");
        byte[] result_q1 = contract.evaluateTransaction(queryAll);
        String output = new String(result_q1);
        String message = "Successfully queried all cars";
        return new Message(200, message ,output);
    }

    public Fabcarjson generate_JSON(){
        Random r = new Random();
        int i1 = r.nextInt(1000);
        int i2 = r.nextInt(1000);
        String key_str1 = Integer.toString(i1);
        String key_str2 = Integer.toString(i2);
        String model = "C260";
        String colour = "Blue";
        String owner = "Frank";
        if(i1 > 300) model = "E200L";
        if(i1 > 500) model = "S400";
        if(i2 > 200) colour = "White";
        if(i2 > 550) colour = "Silver";
        if(i2 > 800) colour = "Black";
        if(i2 > 600) owner = "Billy";
        String key = "CAR"+key_str1+key_str2;
        Fabcarjson new_car = new Fabcarjson("Mercedes-Benz",model,colour,owner,key);
        return new_car;
    }
}
