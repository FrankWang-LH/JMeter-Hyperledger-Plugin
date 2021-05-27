/**
* @author  Thomas Jennings
* @since   2020-03-25
*
* Modified by Zhenqi Wang to cater fot ERC20 token
* @since   2021-03-23
*/

package sample.hyperledger.blockchain.communication;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;

import sample.hyperledger.blockchain.model.*;

@javax.ws.rs.Path("Resources")


@ApplicationScoped
public class Resources {
	
	//set this for the location of the wallet directory and the connection json file
	static String pathRoot = "C:/Users/frank/.fabric-vscode/gateways/";
	
	static {
		System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "false");
	}
	
	@Timed(name = "addErcProcessingTime",
	         tags = {"method=post"},
	         absolute = true,
	         description = "Time needed to add Erc to the inventory")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@javax.ws.rs.Path("create")
	@Operation(
			summary = "Issue tokens to a user",
			description = "Requires user id and the value to be issued to this user")
	public Erc addErc(
			Erc aErc
			)
	{
		try {
			Path walletPath = Paths.get(pathRoot + "org-1-wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			System.out.println(aErc.getId()+aErc.getId_to()+aErc.getValue());
			//load a CCP
			//expecting the connect profile json file; export the Connection Profile from the
			//fabric gateway and add to the default server location 
			Path networkConfigPath = Paths.get(pathRoot + "2-Org-Local-Fabric-Org1_connection.json");
			Gateway.Builder builder = Gateway.createBuilder();
			builder.identity(wallet, "Org1 Admin").networkConfig(networkConfigPath);
			try (Gateway gateway = builder.connect()) {
				//get the network and contract
				Network network = gateway.getNetwork("mychannel");
				Contract contract = network.getContract("token-erc20");
				contract.submitTransaction("Transfer", aErc.getId(), aErc.getValue());
				return new Erc(aErc.getId(), "none", aErc.getValue());
			}
			catch (Exception e){
				System.out.println("Unable to get network/contract and execute query"); 
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (Exception e2) 
		{
			String current;
			try {
				current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("Current working dir: "+current);
			} catch (IOException e) {
				throw new javax.ws.rs.ServiceUnavailableException();
			}
			System.out.println("Unable to find config or wallet - please check the wallet directory and connection json"); 
			throw new javax.ws.rs.ServiceUnavailableException();
		}	
	}
	
	@Timed(name = "approveErcProcessingTime",
	         tags = {"method=post"},
	         absolute = true,
	         description = "Time needed to approve a spending request")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@javax.ws.rs.Path("approve")
	@Operation(
			summary = "Approve a user to spend money",
			description = "Requires user id and the value to be allowed to use by this user")
	public String approveErc(
			Erc aErc
			)
	{
		try {
			Path walletPath = Paths.get(pathRoot + "org-1-wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			System.out.println(aErc.getId()+aErc.getId_to()+aErc.getValue());
			//load a CCP
			//expecting the connect profile json file; export the Connection Profile from the
			//fabric gateway and add to the default server location 
			Path networkConfigPath = Paths.get(pathRoot + "2-Org-Local-Fabric-Org1_connection.json");
			Gateway.Builder builder = Gateway.createBuilder();
			builder.identity(wallet, "Org1 Admin").networkConfig(networkConfigPath);
			try (Gateway gateway = builder.connect()) {
				//get the network and contract
				Network network = gateway.getNetwork("mychannel");
				Contract contract = network.getContract("token-erc20");
				byte[] result_approval = contract.createTransaction("Approve").submit(aErc.getId(),aErc.getValue());
				String output = "Approval for required value: "+ new String(result_approval);
				return output;
			}
			catch (Exception e){
				System.out.println("Unable to get network/contract and execute query"); 
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (Exception e2) 
		{
			String current;
			try {
				current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("Current working dir: "+current);
			} catch (IOException e) {
				throw new javax.ws.rs.ServiceUnavailableException();
			}
			System.out.println("Unable to find config or wallet - please check the wallet directory and connection json"); 
			throw new javax.ws.rs.ServiceUnavailableException();
		}	
	}
	
	@Timed(name = "allowanceProcessingTime",
	         tags = {"method=post"},
	         absolute = true,
	         description = "Time needed to fetch allowance for a user")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@javax.ws.rs.Path("allowance")
	@Operation(
			summary = "Fetch the allowance figure of a spender",
			description = "Requires user id")
	public String allowance(
			Erc aErc
			)
	{
		try {
			Path walletPath = Paths.get(pathRoot + "org-1-wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			System.out.println(aErc.getId()+aErc.getId_to()+aErc.getValue());
			//load a CCP
			//expecting the connect profile json file; export the Connection Profile from the
			//fabric gateway and add to the default server location 
			Path networkConfigPath = Paths.get(pathRoot + "2-Org-Local-Fabric-Org1_connection.json");
			Gateway.Builder builder = Gateway.createBuilder();
			builder.identity(wallet, "Org1 Admin").networkConfig(networkConfigPath);
			try (Gateway gateway = builder.connect()) {
				//get the network and contract
				Network network = gateway.getNetwork("mychannel");
				Contract contract = network.getContract("token-erc20");
				byte[] result_allowance = contract.createTransaction("Allowance").submit(aErc.getId(),aErc.getId());
				String output = new String(result_allowance);
				return output;
			}
			catch (Exception e){
				System.out.println("Unable to get network/contract and execute query"); 
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (Exception e2) 
		{
			String current;
			try {
				current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("Current working dir: "+current);
			} catch (IOException e) {
				throw new javax.ws.rs.ServiceUnavailableException();
			}
			System.out.println("Unable to find config or wallet - please check the wallet directory and connection json"); 
			throw new javax.ws.rs.ServiceUnavailableException();
		}	
	}

	@Timed(name = "TransferToProcessingTime",
	         tags = {"method=post"},
	         absolute = true,
	         description = "Time needed to make a user transfer")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@javax.ws.rs.Path("transferTo")
	@Operation(
			summary = "Transfer from one user to another",
			description = "Requires user ids and value")
	public String updateErc(
			Erc aErc
			)
	{
		try {
			Path walletPath = Paths.get(pathRoot + "org-1-wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			
			//load a CCP
			//expecting the connect profile json file; export the Connection Profile from the
			//fabric gateway and add to the default server location 
			Path networkConfigPath = Paths.get(pathRoot + "2-Org-Local-Fabric-Org1_connection.json");
			
			Gateway.Builder builder = Gateway.createBuilder();
			
			//expecting wallet directory within the default server location
			//wallet exported from Fabric wallets Org 1
			builder.identity(wallet, "org1peer").networkConfig(networkConfigPath);
			try (Gateway gateway = builder.connect()) {
				
				//get the network and contract
				Network network = gateway.getNetwork("mychannel");
				Contract contract = network.getContract("token-erc20");
				String id = aErc.getId();
				String id_to = aErc.getId_to();
				String value = aErc.getValue();
				byte[] result_approval = contract.createTransaction("Approve").submit(id,value);
				System.out.println("Approval for required value: "+ new String(result_approval));
				byte[] result_transfer = contract.createTransaction("TransferFrom").submit(id,id_to,value);
				String output = new String(result_transfer);
				return output;
			}
			catch (Exception e){
				System.out.println("Unable to get network/contract and execute query"); 
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (Exception e2) 
		{
			String current;
			try {
				current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("Current working dir: "+current);
			} catch (IOException e) {
				throw new javax.ws.rs.ServiceUnavailableException();
			}
			System.out.println("Unable to find config or wallet - please check the wallet directory and connection json"); 
			throw new javax.ws.rs.ServiceUnavailableException();
		}	
	}
	
	@Timed(name = "QueryErcProcessingTime",
	         tags = {"method=GET"},
	         absolute = true,
	         description = "Time needed to query a erc")
	@GET
	@javax.ws.rs.Path("read")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Returns client balance by id",
			description = "Requires the id to be provided")
	public String readErc(@QueryParam("id")String id) 
	{
	
		byte[] result = null;
		String outputString = "";
		String passedOutput = "";
		
		try {
			Path walletPath = Paths.get(pathRoot + "org-1-wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			
			//load a CCP
			//expecting the connect profile json file; export the Connection Profile from the
			//fabric gateway and add to the default server location 
			Path networkConfigPath = Paths.get(pathRoot + "2-Org-Local-Fabric-Org1_connection.json");
			
			Gateway.Builder builder = Gateway.createBuilder();
			
			//expecting wallet directory within the default server location
			//wallet exported from Fabric wallets Org 1
			builder.identity(wallet, "org1peer").networkConfig(networkConfigPath);
			try (Gateway gateway = builder.connect()) {
				
				//get the network and contract
				Network network = gateway.getNetwork("mychannel");
				Contract contract = network.getContract("token-erc20");
				System.out.println("Try reading an ERC20 contract");
				result = contract.createTransaction("BalanceOf").submit(id);
				outputString = "Balance of " + id + " is " + new String(result);
				System.out.println(outputString);
				passedOutput = outputString;
				return passedOutput;
			}
			catch (Exception e){
				System.out.println("Unable to get network/contract and execute query"); 
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (Exception e2) 
		{
			String current;
			try {
				current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("Current working dir: "+current);
			} catch (IOException e) {
				throw new javax.ws.rs.ServiceUnavailableException();
			}
			System.out.println("Unable to find config or wallet - please check the wallet directory and connection json"); 
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}
	
	@Timed(name = "QueryErcProcessingTime",
	         tags = {"method=POST"},
	         absolute = true,
	         description = "Time needed to query a erc")
	@POST
	@javax.ws.rs.Path("read")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Returns client balance by id",
			description = "Requires the id to be provided")
	public String readErc_p(Erc erc) 
	{
	
		byte[] result = null;
		String outputString = "";
		String passedOutput = "";
		
		try {
			Path walletPath = Paths.get(pathRoot + "org-1-wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			
			//load a CCP
			//expecting the connect profile json file; export the Connection Profile from the
			//fabric gateway and add to the default server location 
			Path networkConfigPath = Paths.get(pathRoot + "2-Org-Local-Fabric-Org1_connection.json");
			
			Gateway.Builder builder = Gateway.createBuilder();
			
			//expecting wallet directory within the default server location
			//wallet exported from Fabric wallets Org 1
			builder.identity(wallet, "org1peer").networkConfig(networkConfigPath);
			try (Gateway gateway = builder.connect()) {
				
				//get the network and contract
				Network network = gateway.getNetwork("mychannel");
				Contract contract = network.getContract("token-erc20");
				System.out.println("Try reading an ERC20 contract");
				result = contract.createTransaction("BalanceOf").submit(erc.getId());
				outputString = "Balance of " + erc.getId() + " is " + new String(result);
				System.out.println(outputString);
				passedOutput = outputString;
				return passedOutput;
			}
			catch (Exception e){
				System.out.println("Unable to get network/contract and execute query"); 
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (Exception e2) 
		{
			String current;
			try {
				current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("Current working dir: "+current);
			} catch (IOException e) {
				throw new javax.ws.rs.ServiceUnavailableException();
			}
			System.out.println("Unable to find config or wallet - please check the wallet directory and connection json"); 
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}

	@Timed(name = "QueryErcProcessingTime",
	         tags = {"method=GET"},
	         absolute = true,
	         description = "Time needed to query a erc")
	@GET
	@javax.ws.rs.Path("clientAccountID")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Returns client id",
			description = "Requires the no parameters")
	public String clientAccountID() 
	{
	
		byte[] result = null;
		String outputString = "";
		String passedOutput = "";
		
		try {
			Path walletPath = Paths.get(pathRoot + "org-1-wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			
			//load a CCP
			//expecting the connect profile json file; export the Connection Profile from the
			//fabric gateway and add to the default server location 
			Path networkConfigPath = Paths.get(pathRoot + "2-Org-Local-Fabric-Org1_connection.json");
			
			Gateway.Builder builder = Gateway.createBuilder();
			
			//expecting wallet directory within the default server location
			//wallet exported from Fabric wallets Org 1
			builder.identity(wallet, "org1peer").networkConfig(networkConfigPath);
			try (Gateway gateway = builder.connect()) {
				
				//get the network and contract
				Network network = gateway.getNetwork("mychannel");
				Contract contract = network.getContract("token-erc20");
				System.out.println("Try getting client account ID");
				result = contract.createTransaction("ClientAccountID").submit();
				outputString = "Id of current client is " + new String(result);
				System.out.println(outputString);
				passedOutput = outputString;
				return passedOutput;
			}
			catch (Exception e){
				System.out.println("Unable to get network/contract and execute query"); 
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (Exception e2) 
		{
			String current;
			try {
				current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("Current working dir: "+current);
			} catch (IOException e) {
				throw new javax.ws.rs.ServiceUnavailableException();
			}
			System.out.println("Unable to find config or wallet - please check the wallet directory and connection json"); 
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}

	@Timed(name = "QueryErcProcessingTime",
	         tags = {"method=GET"},
	         absolute = true,
	         description = "Time needed to query a erc")
	@GET
	@javax.ws.rs.Path("clientAccountBalance")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Returns current client balance",
			description = "Requires no parameters")
	public String clientAccountBalance() 
	{
	
		byte[] result = null;
		String outputString = "";
		String passedOutput = "";
		
		try {
			Path walletPath = Paths.get(pathRoot + "org-1-wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			
			//load a CCP
			//expecting the connect profile json file; export the Connection Profile from the
			//fabric gateway and add to the default server location 
			Path networkConfigPath = Paths.get(pathRoot + "2-Org-Local-Fabric-Org1_connection.json");
			
			Gateway.Builder builder = Gateway.createBuilder();
			
			//expecting wallet directory within the default server location
			//wallet exported from Fabric wallets Org 1
			builder.identity(wallet, "org1peer").networkConfig(networkConfigPath);
			try (Gateway gateway = builder.connect()) {
				
				//get the network and contract
				Network network = gateway.getNetwork("mychannel");
				Contract contract = network.getContract("token-erc20");
				System.out.println("Try getting client account balance");
				result = contract.createTransaction("ClientAccountBalance").submit();
				outputString = "Balance of current client is " + new String(result);
				System.out.println(outputString);
				passedOutput = outputString;
				return passedOutput;
			}
			catch (Exception e){
				System.out.println("Unable to get network/contract and execute query"); 
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (Exception e2) 
		{
			String current;
			try {
				current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("Current working dir: "+current);
			} catch (IOException e) {
				throw new javax.ws.rs.ServiceUnavailableException();
			}
			System.out.println("Unable to find config or wallet - please check the wallet directory and connection json"); 
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}
}
