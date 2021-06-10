import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Network;

import java.util.concurrent.TimeoutException;

/**
 * Hyperledger Fabric ERC20 Token Smart Contract
 *
 * @author  Zhenqi Wang <z5141545@student.unsw.edu.au>
 * @since   December 2020
 */

public class ERC20 implements SmartContract {
    private static String contractName = "token-erc20";
    private static String create = "Transfer";
    private static String check = "BalanceOf";
    private static String update = "TransferFrom";
    private static String delete = "DeleteErc"; // UNIMPLEMENTED - NOT APPLICABLE TO ERC20
    private static final String mint = "Mint";
    private static final String approve = "Approve";
    private static final String allowance = "Allowance";
    private static final String clientBalance = "ClientAccountBalance";
    private static final String clientID = "ClientAccountID";
    private static final String totalSupply = "TotalSupply";
    private final Network network;

    /**
     * Constructor
     *
     * @param network       HLF Gateway Network object
     * @param init          whether to initialise the ledger
     */ 
    public ERC20(Network network, boolean init) {
        this.network = network;
        if(init) {
            try {
                this.initLedger();
            } catch (Exception e) {
                System.out.println("Error: " + "Initialisation Failed");
            }
        }
    }

    /**
     * Constructor - with contract options passed in as string
     *
     * @param network           HLF Gateway Network object
     * @param createContract    'create' contract name
     * @param checkContract     'check' contract name
     * @param updateContract    'update' contract name
     * @param deleteContract    'delete' contract name
     */ 
    public ERC20(String contractName, String createContract, String checkContract, String updateContract, String deleteContract, Network network) {
        ERC20.contractName = contractName;
        ERC20.create= createContract;
        ERC20.check = checkContract;
        ERC20.update = updateContract;
        ERC20.delete = deleteContract;
        this.network = network;
    }

    /**
     * Initialise the ledger with desired operations
     *
     */ 
    @Override
    public void initLedger() throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        System.out.println("Initialise the ledger - ERC20");
        System.out.println("Mint - issue 50000 tokens to minter");
        byte[] result_q1 = contract.createTransaction(mint).submit("50000");
        System.out.println("Issued total supply 50000 tokens: "+ new String(result_q1));
        //byte[] result_q2 = contract.createTransaction(create).submit("x509::/OU=admin/CN=Org1 Admin::/CN=Org1 CA","20000");
        //System.out.println("Issued 20000 tokens to peer 1: "+ new String(result_q2));
        byte[] result_q3 = contract.createTransaction(create).submit("x509::/OU=admin/CN=Org2 Admin::/CN=Org2 CA","20000");
        System.out.println("Issued 20000 tokens to admin 2 "+ new String(result_q3));
    }

    /**
     * {@inheritDoc}
     * An "Invoke" type contract, in ERC-20 being transfer from minter to client
     * 
     * @param args          List of arguments
     * @return              A Message object conatining all relevant information from the transaction
     */ 
    @Override
    public Message invoke(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        if(args.length < 3) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        String id = args[0];
        String value = args[2];
        // Get old balance
        System.out.println("Try creating an ERC20 contract, issuing " + value + " tokens to client with id: " + id);
        String oldBalance = "0";
        try {
            byte[] result_oBalance = contract.createTransaction(check).submit(id);
            oldBalance = new String(result_oBalance);
        } catch (Exception e) {
            System.out.println("** Client account has not been initialised before. **");
        }
        System.out.println("Balance before issue: "+oldBalance);
        // Transfer to target user
        byte[] result = contract.createTransaction(create).submit(id,value);
        String output = new String(result);
        // Check balance after transfer
        byte[] result_nBalance = contract.createTransaction(check).submit(id);
        String newBalance = new String(result_nBalance);
        System.out.println("Check Client Balance after transfer: "+newBalance);
        if(!sumAssertion(Integer.parseInt(oldBalance),Integer.parseInt(value),Integer.parseInt(newBalance))) {
            String message = "Failed sum assertion, the transfer may not have been processed by the Blockchain Properly";
            return new Message(500, message, "Transfer failed, balance calculation error - old balance: " + oldBalance + ", value transferred out: " + value + ", final value: " + newBalance);
        }
        String message = "Success, balance of " + id + " is now " + newBalance;
        System.out.println("Response: " + message);
        return new Message(200, "Transfer to client successful" ,message);
    }

    /**
     * {@inheritDoc}
     * A "Read" type contract, in ERC-20 being check client balance given client ID
     * 
     * @param args          List of arguments
     * @return              A Message object conatining all relevant information from the transaction
     */ 
    @Override
    public Message read(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        // Access arguments
        if(args.length < 1) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        String id = args[0];
        // Check account balance
        System.out.println("Try reading an ERC20 contract");
        byte[] result = contract.createTransaction(check).submit(id);
        String output = new String(result);
        String message = "Read operation successful.";
        System.out.println("Balance of account " + id + ": " + output);
        return new Message(200, message ,"Balance of account " + id + ": " + output);
    }

    /**
     * {@inheritDoc}
     * An "Update" type contract, in ERC-20 being transfer from client to client
     * 
     * @param args          List of arguments
     * @return              A Message object conatining all relevant information from the transaction
     */ 
    @Override
    public Message update(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        // Access arguments
        if(args.length < 3) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        String id_from = args[0];
        String id_to = args[1];
        String value = args[2];
        String output = null;
        String message = "No Message";
        // Get id and old balances
        String oldBalance_to = "0";
        String oldBalance = new String(contract.createTransaction(clientBalance).submit());
        if(Integer.parseInt(value) > Integer.parseInt(oldBalance)) return new Message(500,"Error: Insufficient funds at client account", "Insufficient fund");
        try {
            byte[] result = contract.createTransaction(check).submit(id_to);
            oldBalance_to = new String(result);
        } catch (Exception e) {
            System.out.println("** Target client account have not been initialised before. **");
        }
        System.out.println("Try updating an ERC20 contract, transfer from " + id_from +", to " + id_to + " with value of " + value);
        String id = clientAccountID();
        System.out.println("Getting client id: "+id);
        byte[] result_approval = contract.createTransaction(approve).submit(id,value);
        System.out.println("Approval for required value: "+ new String(result_approval));
        // Ask for allowance & transfer
        byte[] result_allowance = contract.createTransaction("Allowance").submit(id,id);
        System.out.println("Client Balance: " + oldBalance + ", Account allowance: "+new String(result_allowance));
        byte[] result_transfer = contract.createTransaction(update).submit(id,id_to,value);
        output = new String(result_transfer);
        // Check account balances after transfer
        byte[] result_nBalance = contract.createTransaction(check).submit(id_from);
        String newBalance = new String(result_nBalance);
        byte[] result_nBalance_to = contract.createTransaction(check).submit(id_to);
        String newBalance_to = new String(result_nBalance_to);
        System.out.println("Check Client Balance after transfer: "+newBalance);
        if(sumAssertion(Integer.parseInt(oldBalance),Integer.parseInt(value),Integer.parseInt(newBalance)) && sumAssertion(Integer.parseInt(newBalance_to),Integer.parseInt(value),Integer.parseInt(oldBalance_to))/*true*/) {
            message = "Transfer successful, amount: " + value + ", account balance of user " + id_from + " is now " + newBalance;
        } else {
            message = "Failed assertion, the transfer may not have been processed by the blockchain properly";
            return new Message(500, message, "Transfer failed, balance calculation error - expected: old balance: " + oldBalance + ", value transferred out: " + value + ", final value: " + newBalance);
        }
        String response_message = "Transfer operation successful? "+output;
        System.out.println(response_message);
        return new Message(200, response_message ,message);
    }

    /**
     * {@inheritDoc}
     * An "Delete" type contract, not used in ERC-20
     * 
     * @param args          List of arguments
     * @return              A Message object conatining all relevant information from the transaction
     */ 
    @Override
    public Message delete(String[] args) throws InterruptedException, TimeoutException, ContractException {
        return new Message(404, "NO DELETE ENDPOINT FOR THIS SMART CONTRACT." , null);
    }


    private boolean sumAssertion(int a, int b, int c){
        return Math.abs(a - b - c) < 10 * b + 10000;
    }

    /**
     * Return clientAccountID as a string
     * 
     * @return              String object of clientAccountID
     */ 
    public String clientAccountID() {
        try {
            Contract contract = network.getContract(contractName);
            return new String(contract.createTransaction(clientID).submit());
        } catch (Exception e) {
            System.out.println("Error - Client does not exist");
            return null;
        }
    }

    /**
     * Mint a given number of tokens into the ledger
     * 
     * @param args          List of arguments
     * @return              A Message object conatining all relevant information from the transaction
     */ 
    public Message mint(String args[]) {
        if(args.length < 3) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        String value = args[2];
        Contract contract = network.getContract(contractName);
        System.out.println("Mint: issue " + value + " tokens to minter");
        try {
            byte[] result = contract.createTransaction(mint).submit(value);
            return new Message(200, "Mint successful: " + value, new String(result));
        } catch (Exception e) {
            return new Message(403, "Mint failed", "Invalid access, please use MINTER identity to issue new tokens");
        }
    }

    /**
     * Return the number of tokens allowed to spend given user id
     * 
     * @param args          List of arguments
     * @return              A Message object conatining all relevant information from the transaction
     */ 
    public Message allowance(String args[]) {
        if(args.length < 2) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        String owner = args[0];
        String spender = args[0];
        Contract contract = network.getContract(contractName);
        System.out.println("Allowance check: owner " + owner + ", spender " + spender);
        try {
            byte[] result = contract.createTransaction(allowance).submit(owner,spender);
            System.out.println("Allowance amount: " + new String(result));
            return new Message(200, "Allowance check successful", "Allowance amount: " + new String(result));
        } catch (Exception e) {
            return new Message(403, "Allowance check failed", null);
        }
    }

    /**
     * Seek approval to spend a number of tokens given user id and amount of tokens
     * 
     * @param args          List of arguments
     * @return              A Message object conatining all relevant information from the transaction
     */ 
    public Message approve(String args[]) {
        if(args.length < 3) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        String spender = args[0];
        String value = args[2];
        Contract contract = network.getContract(contractName);
        System.out.println("Approval request: spender " + spender + ", value " + value);
        try {
            byte[] result = contract.createTransaction(approve).submit(spender,value);
            System.out.println("Approved? " + new String(result));
            return new Message(200, "Approved requested value", "Approval Status of amount " + value + " : " + new String(result));
        } catch (Exception e) {
            return new Message(403, "Approval failed" , "Params: spender " + spender + ", value " + value);
        }
    }

    /**
     * Return the total amount of tokens in the current client's account
     * 
     * @return              client balance as an Integer
     */ 
    public Integer clientAccountBalance() {
        try {
            Contract contract = network.getContract(contractName);
            return Integer.parseInt(new String(contract.createTransaction(clientBalance).submit()));
        } catch (Exception e) {
            System.out.println("Error - Client does not exist");
            return 0;
        }
    }

    /**
     * Return the total amount of tokens in the ledger.
     * 
     * @return              TotalSupply as an Integer
     */ 
    public Integer totalSupply() {
        try {
            Contract contract = network.getContract(contractName);
            return Integer.parseInt(new String(contract.createTransaction(totalSupply).submit()));
        } catch (Exception e) {
            System.out.println("Error - Could not query total supply.");
            return 0;
        }
    }

    /**
     * It's a cheat for testing, dump all tokens that is owned by users involved in testing, to a "dump" account
     * 
     */
    public void dump(String[] args){
        try {
            Contract contract = network.getContract(contractName);
            String id = args[0];
            // Get old balance
            String oldBalance = "0";
            try {
                byte[] result_oBalance = contract.createTransaction(check).submit(id);
                oldBalance = new String(result_oBalance);
            } catch (Exception e) {
                System.out.println("** Client account has no balance. **");
                return;
            }
            System.out.println("Balance before dump: "+oldBalance);
            // Transfer to target user
            byte[] result_approval = contract.createTransaction(approve).submit(id,oldBalance);
            System.out.println("Approval for required value: "+ new String(result_approval));
            byte[] result = contract.createTransaction(update).submit(id,"dump",oldBalance);
            // Check balance after transfer
            byte[] result_nBalance = contract.createTransaction(check).submit(id);
            String newBalance = new String(result_nBalance);
            System.out.println("Check Client Balance after transfer: "+newBalance);
        } catch (InterruptedException e) {
            System.out.println("Response: Execution Interrupted");
        } catch (TimeoutException e1) {
            System.out.println("Response: Transaction Timed out");
        } catch (ContractException e2) {
            System.out.println("Response: Unable to Get Network / Contract");
        } catch (Exception e3) {
            System.out.println("Response: Transaction Failed");
        }
    }

    /**
     * Burn a number of tokens given amount (minter ID only)
     * 
     */ 
    public void burn(String[] args){
        try {
            Contract contract = network.getContract(contractName);
            String id = args[0];
            // Get old balance
            String oldBalance = "0";
            try {
                byte[] result_oBalance = contract.createTransaction(check).submit(id);
                oldBalance = new String(result_oBalance);
            } catch (Exception e) {
                System.out.println("** Client account has no balance. **");
                return;
            }
            System.out.println("Balance before burn: "+oldBalance);
            // Burn everything
            byte[] result = contract.createTransaction("Burn").submit(oldBalance);
            byte[] result_nBalance = contract.createTransaction(check).submit(id);
            String newBalance = new String(result_nBalance);
            System.out.println("Check Client Balance after transfer: "+newBalance);
        } catch (InterruptedException e) {
            System.out.println("Response: Execution Interrupted");
        } catch (TimeoutException e1) {
            System.out.println("Response: Transaction Timed out");
        } catch (ContractException e2) {
            System.out.println("Response: Unable to Get Network / Contract");
        } catch (Exception e3) {
            System.out.println("Response: Transaction Failed");
        }
    }

    public ERCjson generate_JSON(String id, String id_to,String value) {
        ERCjson newJson = new ERCjson(id,id_to,value);
        return newJson;
    }

}
