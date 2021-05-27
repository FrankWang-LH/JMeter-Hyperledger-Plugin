This plugin adds Hyperledger Fabric blockchain & DApp testing capabilities to Apache JMeter. By using this plugin, you can perform performance testing on potentially any smart contract deployed on Hyperledger Fabric networks.

## Attribution
This work is inspired by Dr Dilum Bandara at CSIRO's data61.

## Public Release: 1.6-stable
Last updated April 2021.

## Configure Hyperledger Connection and Wallet files for blockchain testing
This plugin provides direct blockchain testing mode, with wallet integrated into the application. Before you start to use the plugin, configure your Hyperledger connection and wallet paths in the source code to use the direct mode.
Open ```HLFClientPlugin.java``` class and change the following paths to your Hyperledger credentials file location.
```java
static String pathRoot = "YOUR FABRIC CONNECTION PROFILE & WALLET DIRECTORY PATH";
static String walletName = "WALLET_NAME";
static Path walletPath = Paths.get(pathRoot + walletName);
static Path networkConfigPath = Paths.get(pathRoot + "YOUR_NETWORK_CONFIG_FILE.json");
```

## Install and Run this Plugin in JMeter
Maven is required to install this plugin.
To use this plugin in JMeter, first execute this command in the project root folder.
```
mvn assembly:assembly -DskipTests
```
The JAR file will be generated and placed in ```/target``` folder.
Next, place the JAR file in your ```JMeter_DIR/lib/ext``` folder.
Start JMeter and select and configure a Java Request sampler and you're all set.

## Plugin Structure
This plugin contains three base classes, ```HLFClientPlugin```, ```Message```, and ```SmartContract```.
### HLFClientPlugin.java
```HLFClientPlugin``` is the main class of this plugin, it inherits JMeter's ```AbstractJavaSamplerClient```.
This class handles all the argument initialisation, handling and test running. The support for extenstions to other smart contracts are discussed below in "Support for other smart contracts" section.
### SmartContract.java
```SmartContract``` is an interface for different hyperledger smart contracts. Implementation guidelines of SmartContract objects are discussed below in "Support for other smart contracts" section. Default class attributes and methods are discussed below.
```java
import java.util.concurrent.TimeoutException;

import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Network;

public interface SmartContract {
    // Name of smart contract - must match the name in your blockchain!
    String contractName = "contract_default";
    // Transaction method names - must match the name in your blockchain!
    String create = null;                     
    String check = null;
    String update = null;
    String delete = null;
    // Hyperledger Fabric gateway Network object
    Network network = null;
    // Default methods - init, invoke, read, update and delete
    // They may be left blank if such method does not exist in actual smart contract
    void initLedger() throws InterruptedException, TimeoutException, ContractException;
    Message invoke(String[] args) throws InterruptedException, TimeoutException, ContractException;
    Message read(String[] args) throws InterruptedException, TimeoutException, ContractException;
    Message update(String[] args) throws InterruptedException, TimeoutException, ContractException;
    Message delete(String[] args) throws InterruptedException, TimeoutException, ContractException;
}
```
### Message.java
```Message``` is a simple class meant for storing response messages and datas from transaction requests to be passed on to JMeter for aggregation.
This class has three attributes.
```java
    private int code;           // HTTP response code
    private String message;     // Response message
    private String data;        // Response data from transactions
```    


## Parameters
Parameters are defined in ```getDefaultParameters()``` method in ```HLFClientPlugin.java```, more parameters can be added by appending to this method, or defining them in JMeter test plan. However, as JMeter GUI currently does not support adding or removing arguments, it is recommended that you define the parameters in the source code.
The following parameters have been pre-defined in the plugin.
| Name | Usage |
| --- | ------ | 
| type | The type of transaction to be executed.Expected input values and formats can be defined per user request. |
| uid1 | User id 1 to be used in transaction. |
| uid2 | User id 2 to be used in transaction. |
| random_value | True or False, decides whether random value generation is used in transactions. |
| minval | When using random value generation,the lower bound of generated value. |
| maxval | When using random value generation,the upper bound of generated value.If random value is not used, this value will be used as parameter in transactions. |
| direct | ```True``` or ```False```, decides connection modes.When set as True, the plugin will attempt to establish direct connection to the blockchain, otherwise it will try to connect to a DApp with the url value parameter given below. |
| identity | The identity to use when connecting directly to the blockchain. |
| init | ```True``` or ```False```, decides whether to initialise the blockchain.When set as True, the plugin will call related method to create user accounts and initialise the ledger with related commodity object , e.g. erc20 tokens. |
| contract | Name of smart contract to be used. Expected input values and formats can be defined per user request. |
| http_method | The http method to be used in DApp mode. Must be capitalized letters, e.g. GET, POST. |
| url | The root URL of the DApp to be tested. e.g. http://localhost:9080/org-1-ol-blockchain-functions/System/Resources/ |

## Inbuilt support for smart contracts
This plugin comes with support for ```fabcar``` and ```token-erc20``` contracts provided in [Fabric Samples](https://github.com/hyperledger/fabric-samples).
To use fabcar, set ```contract``` parameter to ```fabcar```, to use Erc20 tokens, set ```contract``` parameter to ```erc20```.

## Support for other smart contracts
To extend this plugin for other smart contracts, do the following.
### Implement a SmartContract object for your smart contract
Implement constructors and all required methods.
```java
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Network;

import java.util.concurrent.TimeoutException;

public class CLASSNAME implements SmartContract {
    ...
    ...
    // Constructor
    public CLASSNAME(Network network, boolean init) {
        this.network = network;
        // Initialise the ledger
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
        Contract contract = network.getContract(contractName);
        byte[] result_q1 = contract.createTransaction(WHATEVER_TRANSACTION).submit(SOME_ARGS);
        ...
    }
    
    @Override
    public Message invoke(String[] args) throws InterruptedException, TimeoutException, ContractException {
        Contract contract = network.getContract(contractName);
        if(args.length < 3) return new Message(400, "BAD REQUEST MISSING ARGUMENTS","");
        ... Handle Arguments
        ... Submit Requests
        byte[] result_q1 = contract.createTransaction(WHATEVER_TRANSACTION).submit(SOME_ARGS);
        ... Form return objects and throw exceptions if needed
        return new Message(200, RETURN_MESSAGE, TRANSACTION_RESULTS);
    }
    ...
    // If a default method does not exist, you may leave it blank or produce an error message. For Example:
    @Override
    public Message delete(String[] args) throws InterruptedException, TimeoutException, ContractException {
        return new Message(404, "NO DELETE ENDPOINT FOR THIS SMART CONTRACT." , null);
    }
    ...
    Add more custom methods / endpoints as required by your smart contracts
    ...
}
```
### Implement a extended test method & add new data type in generateSC() smart contract switch
In ```HLFClientPlugin.java```, implement a testing method for extra methods like this.
```java
private Message testFabcar(Network network, String[] args, boolean init) throws InterruptedException, TimeoutException, ContractException {
        Fabcar fabcar = new Fabcar(network,init);
        switch(this.type){
            case "queryAll":
                return fabcar.queryAll(args);
            default:
                System.out.println("Response: Not a valid transaction option");
                return new Message(400, "Not a valid transaction option", null);
        }

}
```
Then, add your SmartContract class to ```generateSC``` method.
```java
private SmartContract generateSC(Network network, boolean init){
    switch(this.contract) {
        case "YOUR_CONTRACT":
            return new YOUR_SC(network,init);
        ......
    }
}
```
### Append new class to defaultTest() method
Still in ```HLFClientPlugin.java```, change ```defaultTest()``` method.
```java
private Message defaultTest(String[] args, boolean init) {
    try {
        ...
        switch (this.type) {
            case "create":
                return sc.invoke(args);
            ...
            default:
                switch(this.contract) {
                    case "YOUR_CONTRACT":
                        return YOUR_TEST_METHOD(network,args,init);
                    default:
                        ...
                }
        }
        } catch (Exception e) {...}
    }  catch (Exception e2) {...}
}
```
### Add related parameters to getDefaultParameters() and parameter handling to setupTest() and runTest()
Both methods are located in ```HLFClientPlugin.java```.

### Implement a JSON object class for DApp requests.
My suggestion of implementing this class is to use ```FastJSON``` library provided by Alibaba.
Add this to your ```pom.xml``` (if not added already)
```
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.7</version>
</dependency>
```
Implement your JSON object class like this, include all fields that may be used in transactions.
Note that you need to supply all arguments (they can be blank/default if not used) for transactions.
```
import com.alibaba.fastjson.annotation.JSONField;

public class SCjson {
    @JSONField(name = "id", ordinal = 1)
    private final String id;
    ...
    public SCjson(String id,...) {this.id = id;...}
    ...
    Getters and Setters
    ...
}
```

### Append related DApp endpoint address to getUrl() method & defaultArgs() method
```getUrl()``` method defined the endpoint address of your DApp.
```java
private String getUrl(String root, String[] params){
    if(this.contract.equals("YOUR_CONTRACT")) {
        switch(this.type){
            case "create":
                return root+"create";
            case "update":
                return root+"transferTo";
            ...
            default:
                System.out.println("Response: Not a valid transaction option");
                return "Unknown operation";
        }
    } else {...}
}
```
```defaultArgs()``` handles specified arguments for your smart contract and supplies a default value if none given during test.
```java
private String[] defaultArgs(JavaSamplerContext context) {
    String[] args = new String[0];
    switch(this.contract) {
        case "YOUR_CONTRACT":
            String arg1 = getArg(context,"arg1") != null ? getArg(context,"arg1") : "aeg1_DEFAULT";
            ...
            args = new String[]{arg1,...};
            break;
        default:
            // DO NOTHING
    }
    return args;
}
```
Your smart contract should now be supported by this plugin. Try it out!

