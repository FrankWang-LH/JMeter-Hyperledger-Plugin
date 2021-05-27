import java.util.concurrent.TimeoutException;

import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Network;

/**
 * Hyperledger Fabric Smart Contract Interface for Hyperledger JMeter Plugin
 *
 * @author  Zhenqi Wang <z5141545@student.unsw.edu.au>
 * @since   December 2020
 */

public interface SmartContract {
    String contractName = "contract_default";
    String create = null;
    String check = null;
    String update = null;
    String delete = null;
    Network network = null;
    void initLedger() throws InterruptedException, TimeoutException, ContractException;
    Message invoke(String[] args) throws InterruptedException, TimeoutException, ContractException;
    Message read(String[] args) throws InterruptedException, TimeoutException, ContractException;
    Message update(String[] args) throws InterruptedException, TimeoutException, ContractException;
    Message delete(String[] args) throws InterruptedException, TimeoutException, ContractException;
}
