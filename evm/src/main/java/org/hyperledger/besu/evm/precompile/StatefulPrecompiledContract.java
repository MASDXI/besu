package org.hyperledger.besu.evm.precompile;

import static java.nio.charset.StandardCharsets.UTF_8;


import org.hyperledger.besu.crypto.Hash;
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

    private static final UInt256 STORAGE_SLOT_0 = UInt256.ZERO;
    private static final Bytes GET_SIGNATURE = Hash.keccak256(Bytes.of("get()".getBytes(UTF_8))).slice(0, 4);
    private static final Bytes SET_SIGNATURE = Hash.keccak256(Bytes.of("set(uint256)".getBytes(UTF_8))).slice(0, 4);
    private static final Address CONTRACT_ADDRESS = Address.fromHexString("0x0100000000000000000000000000000000000001");

    private static final Bytes ZERO_VALUE =
      Bytes.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000000");

    public StatefulPrecompiledContract(final GasCalculator gasCalculator) {
        super("StatefulPrecompiledContract", gasCalculator);
    }

    @Override
    public long gasRequirement(final Bytes input) {
        final Bytes function = input.slice(0, 4);
        if (function.equals(SET_SIGNATURE)) {
            return 2000;
        } else {
            return 1000;
        }
    }

    @Nonnull
    @Override
    public PrecompileContractResult computePrecompile(final Bytes input, @Nonnull final MessageFrame messageFrame) {
        if (input.isEmpty()) {
            return PrecompileContractResult.halt(null, Optional.of(ExceptionalHaltReason.PRECOMPILE_ERROR));
        } else {
            final Bytes function = input.slice(0, 4);
            final Bytes payload = input.slice(4);
            final WorldUpdater worldUpdater = messageFrame.getWorldUpdater();
            final MutableAccount mutableAccount = worldUpdater.getOrCreate(CONTRACT_ADDRESS);
            if (function.equals(GET_SIGNATURE)) {
                // LOG.info("LOG");
                return PrecompileContractResult.success(mutableAccount.getStorageValue(STORAGE_SLOT_0));
            } else if (function.equals(SET_SIGNATURE)) {
                final UInt256 payloadAsUInt256 = UInt256.fromBytes(Bytes32.leftPad(payload));
                mutableAccount.setStorageValue(UInt256.ZERO, payloadAsUInt256);

                // @TODO should fix now use tricky to save state.
                if (mutableAccount.getBalance().compareTo(Wei.ZERO) == 0) {
                    mutableAccount.incrementBalance(Wei.of(1));
                } else {
                    mutableAccount.decrementBalance(Wei.of(1));
                }

                worldUpdater.commit();
                messageFrame.storageWasUpdated(UInt256.ZERO, payloadAsUInt256);
                // LOG.info("LOG");
                return PrecompileContractResult.success(ZERO_VALUE);
            } else {
                LOG.info("Failed interface not found");
                return PrecompileContractResult.halt(null, Optional.of(ExceptionalHaltReason.PRECOMPILE_ERROR));
            }
        }
    }
}
