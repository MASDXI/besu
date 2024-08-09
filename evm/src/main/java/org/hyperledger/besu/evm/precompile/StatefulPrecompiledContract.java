package org.hyperledger.besu.evm.precompile;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.account.Account;
import org.hyperledger.besu.evm.account.MutableAccount;
import org.hyperledger.besu.evm.frame.ExceptionalHaltReason;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;

import javax.annotation.Nonnull;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatefulPrecompiledContract extends AbstractPrecompiledContract {
    
    private static final Logger LOG = LoggerFactory.getLogger(StatefulPrecompiledContract.class);

    public StatefulPrecompiledContract(final GasCalculator gasCalculator) {
        super("StatefulPrecompiledContract", gasCalculator);
    }

    @Override
    public long gasRequirement(final Bytes input) {
        // @TODO change reasonable gas calculate.
        return 10_000L; // Define gas cost
    }

    @Nonnull
    @Override
    public PrecompileContractResult computePrecompile(final Bytes input, @Nonnull final MessageFrame messageFrame) {
        // @TODO try catch style.
        if (input.isEmpty()) {
            return PrecompileContractResult.halt(
          null, Optional.of(ExceptionalHaltReason.PRECOMPILE_ERROR));
        } else { 
            final WorldUpdater worldUpdater = messageFrame.getWorldUpdater();
            final MutableAccount mutableAccount = worldUpdater.getOrCreate(Address.fromHexString("0x0100000000000000000000000000000000000001"));
            // mutableAccount.setStorageValue(UInt256.ZERO, UInt256.fromBytes(input));
            // final Bytes32 storedValue = mutableAccount.getStorageValue(UInt256.ZERO);
            
            // ignore input, just increment balance with fixed value 1000 wei each call.
            mutableAccount.incrementBalance(Wei.of(1000));

            worldUpdater.commit();
            LOG.info("State updated successfully: {}", mutableAccount.getBalance());
            return PrecompileContractResult.success(input.copy());
        }
    }
}
