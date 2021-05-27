import com.alibaba.fastjson.JSON;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Network;

import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * A template smart contract class for new users
 *
 *  @author Zhenqi Wang <z5141545@student.unsw.edu.au>
 *  @since  Apr 2021
 */

public class SmartContractTemplate implements SmartContract {
    // TODO - SET THESE ATTRIBUTES ACCORDING TO YOUR SMART CONTRACT
    String contractName = "template";
    String create = "create";
    String check = "query";
    String update = "update";
    String delete = "delete";
    Network network = null;

    // A SIMPLE CONSTRUCTOR
    public SmartContractTemplate(Network network, boolean init) {
        this.network = network;
        // INITIALISE?
        if(init) {
            try {
                this.initLedger();
            } catch (Exception e) {
                System.out.println("Error: " + "Initialisation Failed");
            }
        }
    }

    // CLASS METHODS FROM HERE
    @Override
    public void initLedger() throws InterruptedException, TimeoutException, ContractException {
        // TO-DO - DO YOUR LEDGER INITIALISATION HERE
        // MAYBE CREATE A FEW TRANSACTIONS / ISSUE TOKENS, ETC.
    }

    @Override
    public Message invoke(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        // HANDLE PARAMETERS
        if(args.length < 1) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        // TODO
        ... HANDLE PARAMETERS HERE
        // POST A QUERY
        byte[] result = contract.createTransaction(create).submit(/* INSERT ARGS HERE */);
        // GET OUTPUT, FORM RESPONSE
        String output = new String(result);
        String message = "Successful.";
        return new Message(200, message ,output);
    }

    @Override
    public Message read(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        // HANDLE PARAMETERS
        if(args.length < 1) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        // TODO
        ... HANDLE PARAMETERS HERE
        // POST A QUERY
        byte[] result = contract.createTransaction(check).submit(/* INSERT ARGS HERE */);
        // GET OUTPUT, FORM RESPONSE
        String output = new String(result);
        String message = "Successful.";
        return new Message(200, message ,output);
    }

    @Override
    public Message update(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        // HANDLE PARAMETERS
        if(args.length < 1) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        // TODO
        ... HANDLE PARAMETERS HERE
        // POST A QUERY
        byte[] result = contract.createTransaction(check).submit(/* INSERT ARGS HERE */);
        // GET OUTPUT, FORM RESPONSE
        String output = new String(result);
        String message = "Successful.";
        return new Message(200, message ,output);
    }

    @Override
    public Message delete(String[] args) throws InterruptedException, TimeoutException, ContractException {
        // IF A METHOD IS NOT IMPLEMENTED IN YOUR SMART CONTRACT, YOU CAN DO THIS FOR THIS ENDPOINT
        return new Message(404, "UNIMPLEMENTED - DELETE NOT AVAILABLE", null);
    }

    // ADD MORE METHODS IF YOU WISH
}
