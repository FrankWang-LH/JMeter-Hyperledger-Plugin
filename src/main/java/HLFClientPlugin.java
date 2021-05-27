import com.alibaba.fastjson.JSON;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.hyperledger.fabric.gateway.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Hyperledger Blockchain Testing Plugin for JMeter
 *
 *  Installation command: mvn assembly:assembly -DskipTests
 *
 * @author  Zhenqi Wang <z5141545@student.unsw.edu.au>
 * @since   June 2020
 */

public class HLFClientPlugin extends AbstractJavaSamplerClient {

    private HttpURLConnection connection = null;
    static String pathRoot = "C:/Users/frank/.fabric-vscode/gateways/";
    static String walletName = "org-1-wallet";
    static Path walletPath = Paths.get(pathRoot + walletName);
    static Path walletPath_2 = Paths.get(pathRoot + "org-2-wallet");
    static Path networkConfigPath = Paths.get(pathRoot + "2-Org-Local-Fabric-Org1_connection.json");
    static Path networkConfigPath_2 = Paths.get(pathRoot + "2-Org-Local-Fabric-Org2_connection.json");
    private String contract = "erc20";
    private String type = "update";
    private String org = "org1";
    private String identity = "Org1 Admin";
    //private String identity = "org1peer";
    private String request_method = "GET";
    HashMap<String,String> params= new HashMap<String,String>();

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "false"); // For minifabric
    }

    public HLFClientPlugin() {
        System.out.println("Plugin Initialised.");
    }

    /**
     * Open an HTTPURLConnection to designated address
     *
     * @param root      Root address of DApp
     */
    private void openConn(String root){
        try{
            System.out.println(root);
            URL url = new URL(root);
            this.connection = (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the HTTPURLConnection created above
     *
     */
    private void closeConn(){
        try{
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a random value given lower bound and upper bound for a transaction.
     * If random_value option not set, use the upper bound as the return value.
     *
     * @param context       JMeter input context
     * @return              String value of an integer between lower bound and upper bound
     */
    private String getValue(JavaSamplerContext context) {
        String ercval = "100";
        if(context.containsParameter("random_value")){
            if(context.getParameter("random_value").equals("True")) {
                Integer min = Integer.parseInt(context.getParameter("minval"));
                Integer max = Integer.parseInt(context.getParameter("maxval"));
                ercval = String.valueOf((int)(Math.random() * (max - min)) + min);
            } else {
                ercval = getArg(context, "maxval");
            }
        }
        return ercval;
    }

    /**
     * Fetch an argument from JMeter input context
     *
     * @param context       JMeter input context
     * @param arg           Name of argument
     * @return              The value of the argument, if exists
     */
    private String getArg(JavaSamplerContext context, String arg) {
        if(context.containsParameter(arg)) return context.getParameter(arg);
        return null;
    }

    /**
     * Get the default arguments for specified contract, if not given or run in raw test mode, gives default value
     *
     * @param context       JMeter input context
     * @return              List of arguments
     */
    private String[] defaultArgs(JavaSamplerContext context) {
        String[] args = new String[0];
        switch(this.contract) {
            case "erc20":
                String uid1 = getArg(context,"uid1") != null ? getArg(context,"uid1") : "x509::/OU=admin/CN=Org1 Admin::/CN=Org1 CA";
                String uid2 = getArg(context,"uid2") != null ? getArg(context,"uid2") : "x509::/OU=admin/CN=Org2 Admin::/CN=Org2 CA";
                String value = getValue(context);
                args = new String[]{uid1, uid2, value};
                break;
            case "fabcar":
                String key = getArg(context,"key") != null ? getArg(context,"key") : "key12345";
                String owner = getArg(context,"owner") != null ? getArg(context,"owner") : "fw190";
                args = new String[]{key,owner};
                break;
            default:
                // DO NOTHING
        }
        return args;
    }

    /**
     * Form a JSON body for POST requests given arguments and parameters
     *
     * @param context       JMeter input context
     * @param params        Input parameters
     * @return
     */
    private String getJSONInput(JavaSamplerContext context, String[] params) {
        if(this.contract.equals("erc20")){
            ERC20 newErc20 = new ERC20(null,false);
            String id = params[0];
            String id_to = params[1];
            String value = params[2];
            return JSON.toJSONString(newErc20.generate_JSON(id,id_to,value));
            //return JSON.toJSONString(newErc20.generate_JSON("x509::/OU=client/CN=org1peer::/CN=Org1 CA","x509::/OU=client/CN=org2peer::/CN=Org2 CA","100"));
        } else {
            return JSON.toJSONString(new Fabcarjson("Mercedes-Benz","c200","white","frank","KEY133254"));
        }
    }

    /**
     * Form end urls to DApp API endpoints given tranaction type & params
     *
     * @param root          Root address of the DApp
     * @param params        Parameters to be passed to the DApp
     * @return
     */
    private String getUrl(String root, String[] params){
        if(this.contract.equals("erc20")) {
            switch(this.type){
                case "create":
                    return root+"create";
                case "update":
                    return root+"transferTo";
                case "mint":
                    return root+"mint";
                case "allowance":
                    return root+"allowance";
                case "read":
                    /*String id = params[0];
                    return root+"read?="+id.replaceAll(" ","%20")
                                            .replaceAll(":","%3A")
                                            .replaceAll("/","%2F");*/
                    return root+"read";
                case "approval":
                    return root+"approve";
                case "id":
                    return root+"clientAccountID";
                case "balance_current":
                    return root+"clientAccountBalance";
                default:
                    System.out.println("Response: Not a valid transaction option");
                    return "Unknown operation";
            }
        } else {
            return root+"Car";
        }
    }

    /**
     * Form and commit an HTTP request given parameters and operation types, form responses into a Message object
     *
     * @param url               Root address of the DApp
     * @param params            List of parameters
     * @param jsonInputString   JSON string for POST requests
     * @return
     */
    private Message commitRequest(String url, String[] params, String jsonInputString){
        try{
            /**
             * Start test content
             */
            //this.type = "allowance";
            //this.request_method = "POST";
            /**
             * End test content
             */
            url = getUrl(url,params);
            openConn(url);
            connection.setRequestMethod(this.request_method);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            if(this.request_method.equals("POST")) {
                System.out.println(jsonInputString);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }
            }
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
                return new Message(200,"Success, request body: "+jsonInputString,response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(400,"Bad Request",null);
        }
    }

    /**
     * Generate a SmartContract object given the smart contract to be used
     *
     * @param network       Hyperledger Fabric network
     * @param init          Whether to initialise the ledger
     * @return              A SmartContract object as requested
     */
    private SmartContract generateSC(Network network, boolean init){
        switch(this.contract) {
            case "erc20":
                return new ERC20(network,init);
            default:
                return new Fabcar(network,init);
        }
    }

    /**
     * Test the default methods as given in the SmartContract Interface
     * Extra methods can be tested via implementing a extended test method and append to related section in this method.
     *
     * @param args          List of arguments
     * @param init          Whether to initialise the ledger
     * @return              Message object containing responses from the transaction
     */
    private Message defaultTest(String[] args, boolean init) {
        try {
            Gateway.Builder builder = Gateway.createBuilder();
            if(org.equals("org1")) {
                Wallet wallet = Wallets.newFileSystemWallet(walletPath);
                builder.identity(wallet, identity).networkConfig(networkConfigPath);
            } else {
                Wallet wallet = Wallets.newFileSystemWallet(walletPath_2);
                builder.identity(wallet, identity).networkConfig(networkConfigPath_2);
            }
            System.out.println("Try connect as "+ identity);
            try (Gateway gateway = builder.connect()) {
                Network network = gateway.getNetwork("mychannel");
                SmartContract sc = generateSC(network,init);
                //this.type = "allowance";
                switch (this.type) {
                    case "create":
                        return sc.invoke(args);
                    case "update":
                        return sc.update(args);
                    case "read":
                        return sc.read(args);
                    case "delete":
                        return sc.delete(args);
                    default:
                        switch(this.contract) {
                            case "erc20":
                                return testERC20token(network,args,init);
                            default:
                                return testFabcar(network,args,init);
                        }
                }
            } catch (InterruptedException e) {
                System.out.println("Response: Execution Interrupted");
                return new Message(400, "Response: Execution Interrupted", null);
            } catch (TimeoutException e1) {
                System.out.println("Response: Transaction Timed out");
                return new Message(400, "Response: Transaction Timed out", null);
            } catch (ContractException e2) {
                System.out.println("Response: Unable to Get Network / Contract");
                return new Message(400, "Response: Unable to Get Network / Contract", null);
            } catch (Exception e3) {
                System.out.println("Response: Transaction Failed");
                return new Message(400, "Response: Runtime Exception, Transaction Failed", null);
            }
        }  catch (Exception e4) {
            String current;
            try {
                current = new File(".").getCanonicalPath();
                System.out.println("Current working dir: " + current);
            } catch (IOException e) {
                System.out.println("IO Exception");
                return new Message(400, "IO Exception", null);
            }
            System.out.println("Unable to find config or wallet - please check the wallet directory and connection json");
            return new Message(503, "Unable to connect to network, check connection files.", null);
        }

    }

    /**
     * Extended testing method for fabcar
     *
     * @param network       Hyperledger Fabric gateway network
     * @param args          List of arguments
     * @param init          Whether to initialise the ledger
     * @return              Message object containing responses from the transaction
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ContractException
     */
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

    /**
     * Extended testing method for Erc20 token
     *
     * @param network       Hyperledger Fabric gateway network
     * @param args          List of arguments
     * @param init          Whether to initialise the ledger
     * @return              Message object containing responses from the transaction
     */
    private Message testERC20token(Network network, String[] args, boolean init) {
        ERC20 erc20 = new ERC20(network,init);
        switch(this.type) {
            case "mint":
                return erc20.mint(args);
            case "allowance":
                return erc20.allowance(args);
            case "approval":
                return erc20.approve(args);
            case "uid":
                String id = erc20.clientAccountID();
                System.out.println(id);
                return new Message(200, id, id);
            case "dump":
                erc20.dump(args);
                return new Message(200, "dumped tokens to dump", "No data");
            case "burn":
                erc20.burn(args);
                return new Message(200, "Burned tokens", "No data");
            default:
                System.out.println("Response: Not a valid transaction option");
                return new Message(400, "Not a valid transaction option", "NO DATAs");
        }
    }

    /**
     * Set default parameters and default values
     *
     * @return              JMeter Arguments
     */
    @Override
    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        // default values
        params.addArgument("type", "read");
        params.addArgument("contract", "erc20");
        params.addArgument("uid1", "x509::/OU=admin/CN=Org1 Admin::/CN=Org1 CA");
        params.addArgument("uid2", "x509::/OU=admin/CN=Org2 Admin::/CN=Org2 CA");
        params.addArgument("random_value", "False");
        params.addArgument("minval", "0");
        params.addArgument("maxval", "100");
        params.addArgument("direct", "True");
        params.addArgument("organisation", "org1");
        params.addArgument("identity", "Org1 Admin");
        params.addArgument("init", "False");
        params.addArgument("http_method", "POST");
        params.addArgument("url", "http://localhost:9080/org-1-ol-blockchain-functions/System/Resources/");

        return params;
    }

    /**
     * Setup test and argument handling
     *
     * @param context       JMeter input context
     */
    @Override
    public void setupTest(JavaSamplerContext context) {
        if(context.containsParameter("type")){
            this.type = context.getParameter("type");
        }
        if(context.containsParameter("contract")){
            switch(context.getParameter("contract")) {
                case "erc20":
                    this.contract = "erc20";
                    break;
                case "fabcar":
                    this.contract = "fabcar";
                    break;
                default:
                    // do nothing
            }
        }
        if(context.containsParameter("http_method")){
            this.request_method = context.getParameter("http_method");
        }
        if(context.containsParameter("organisation")) {
            this.org = context.getParameter("organisation");
        }
        if(context.containsParameter("identity")) {
            this.identity = context.getParameter("identity");
        }
        super.setupTest(context);
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
    }

    /**
     * Conduct tests as requested and format results to JMeter format
     *
     * @param context           JMeter input context
     * @return                  JMeter Sample Result
     */
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        // Setup
        SampleResult result = new SampleResult();
        result.sampleStart();
        Message res = null;
        // Parameters - this is a workaround as JMeter GUI is being stupid
        boolean API_MODE = false;
        String url = getArg(context,"url") != null ? getArg(context,"url") : "http://localhost:9080/org-1-ol-blockchain-functions/System/Resources/";
        boolean init = false;
        if(context.containsParameter("direct")) {
            if(context.getParameter("direct").equals("False")) API_MODE=true;
        }
        if(context.containsParameter("init")) {
            if(context.getParameter("init").equals("True")) init = true;
        }
        String[] args = defaultArgs(context);
        // Start queries
        //API_MODE=true;
        try {
            if(API_MODE) {
                res = commitRequest(url, args, getJSONInput(context,args));
                closeConn();
            } else {
                System.out.println("Direct Mode");
                res = defaultTest(args,init);
            }
        } catch (Exception e) {
            getNewLogger().error("runTest exception", e);
        } finally {
            result.sampleEnd();
        }

        // Handle results
        if (res.getCode() != 200) {
            result.setSuccessful(false);
            result.setResponseCode(Integer.toString(res.getCode()));
            result.setResponseMessage(res.getMessage());
            result.setResponseData((res.getData() != null ? res.getData() : "Failed Test"), "UTF-8");
        } else {
            result.setSuccessful(true);
            result.setResponseCode("200");
            result.setResponseMessage(res.getMessage());
            result.setResponseData(res.getData(), "UTF-8");
        }

        return result;
    }

    public static void main(String[] args) {
        JavaSamplerContext context = new JavaSamplerContext(new Arguments());
        HLFClientPlugin request = new HLFClientPlugin();
        request.setupTest(context);
        request.runTest(context);
        request.teardownTest(context);
    }
}