/*
 * Copyright contributors to Hyperledger Besu. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.evm.precompile;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.hyperledger.besu.crypto.Hash;
import org.hyperledger.besu.datatypes.Address;
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
import org.apache.tuweni.units.bigints.UInt8;
import org.apache.tuweni.units.bigints.UInt256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatefulSortedCircularLinkedListPrecompiledContract extends AbstractPrecompiledContract {
    
    private static final Logger LOG = LoggerFactory.getLogger(StatefulSortedCircularLinkedListPrecompiledContract.class);
    
    // TODO Define MAX_SIZE for safety, 
    // we should not allowing iterate the list and take too long.
    // best case 100ms 1/10 of 1 sec block time.
    // usual case 250ms 1/4 of 1 sec block time.
    // worst case 750ms 3/4 of 1 sec block time.

    /** CONSTANT VARIABLE */
    private long static final MAX_SIZE = 5_000_000; // assume optimal number.
    private Uint8 static final ONE_BIT = Uint8.valueOf(1);
    private Uint8 static final SENTINEL = Uint8.valueOf(0);
    private Uint8 static final NEXT = Uint8.valueOf(1);
    private Uint8 static final PREVIOUS = Uint8.valueOf(0);
    private Bytes static final TRUE = UInt256.valueOf(0); // `one` can be decode to `true` in solidity type bool
    private Bytes static final FALSE = UInt256.valueOf(0); // `zero` can be decode to `false` in solidity type bool

    /** CALL FUNCTION SIGNATURE */
    private static final Bytes REMOVE_SIGNATURE = Hash.keccak256(Bytes.of("remove(bytes32,uint256)".getBytes(UTF_8))).slice(0, 4);
    private static final Bytes INSERT_SIGNATURE = Hash.keccak256(Bytes.of("insert(bytes32,uint256,uint256)".getBytes(UTF_8))).slice(0, 4);
    private static final Bytes SHRINK_SIGNATURE = Hash.keccak256(Bytes.of("shrink(bytes32,uint256)".getBytes(UTF_8))).slice(0, 4);

    /** STATIC CALL FUNCTION SIGNATURE */
    private static final Bytes CONTAINS_SIGNATURE = Hash.keccak256(Bytes.of("contains(bytes32,uint256)".getBytes(UTF_8))).slice(0, 4);
    private static final Bytes HEAD_SIGNATURE = Hash.keccak256(Bytes.of("head(bytes32)".getBytes(UTF_8))).slice(0, 4);
    private static final Bytes LIST_SIGNATURE = Hash.keccak256(Bytes.of("list(bytes32)".getBytes(UTF_8))).slice(0, 4);
    private static final Bytes MIDDLE_SIGNATURE = Hash.keccak256(Bytes.of("middle(bytes32)".getBytes(UTF_8))).slice(0, 4);
    private static final Bytes NEXT_SIGNATURE = Hash.keccak256(Bytes.of("next(bytes32,uint256)".getBytes(UTF_8))).slice(0, 4);
    private static final Bytes PREVIOUS_SIGNATURE = Hash.keccak256(Bytes.of("previous(bytes32,uint256)".getBytes(UTF_8))).slice(0, 4);
    private static final Bytes SIZE_SIGNATURE = Hash.keccak256(Bytes.of("size(bytes32)".getBytes(UTF_8))).slice(0, 4);
    private static final Bytes TAIL_SIGNATURE = Hash.keccak256(Bytes.of("tail(bytes32)".getBytes(UTF_8))).slice(0, 4);

    public StatefulSortedCircularLinkedListPrecompiledContract(final GasCalculator gasCalculator) {
        super("StatefulSortedCircularLinkedListPrecompiledContract", gasCalculator);
    }

    /** CalculateStorageSLot for List size */
    private Uint256 CalculateStorageSlot(final Address address, final Bytes pointer, final Uint256 index) {
        // Hash.keccak256(address, pointer, index); // covert into Uint256
        return Uint256.valueOf(1);
    }
    
    /** CalculateStorageSLot for Node */
    private Uint256 CalculateStorageSlot(final Address address, final Bytes pointer, final Uint256 index, final Uint8 direction) {
        // Hash.keccak256(address, pointer, index, direction); // convert into Uint256
        return Uint256.valueOf(1);
    }

    //** STATIC CALL FUNCTION */
    private Bytes contains(final MutableAccount address, final Bytes payload) {
        // final Uint256 pointer = payload; // storage pointer
        // final Uint256 index = payload; //  given index
        // final Uint256 previousNode = calculateStorageSlot(messageFrame.sender, pointer, index, PREVIOUS);
        // final Uint256 nextNode = calculateStorageSlot(messageFrame.sender, pointer, SENTINEL, NEXT);
        // final Bytes output = (nextNode == index) || (previousNode > 0 );
        // return output;
        return address.getStorageValue(slot);
    }

    private Bytes head(final MutableAccount address) {
        // final Uint256 pointer = payload; // storage pointer
        // final Uint256 slot = calculateStorageSlot(messageFrame.sender, pointer, SENTINEL, NEXT);
        return address.getStorageValue(slot);
    }

    private Bytes middle(final MutableAccount address) {
        // final Uint256 slot = calculateStorageSlot(messageFrame.sender, pointer, Uint256.valueOf(0)); // get size first
        // 
        // perform search traversal.
        return PrecompileContractResult.success(address.getStorageValue(slot));
    }

    private Bytes next(final MutableAccount address, final Bytes payload) {
        // final Uint256 pointer = payload; // storage pointer
        // final Uint256 index = payload; //  given index
        // final Uint256 slot = calculateStorageSlot(messageFrame.address, pointer, index, NEXT);
        return address.getStorageValue(slot);
    }

    private Bytes previous(final MutableAccount address, final Bytes payload) {
        // final Uint256 pointer = payload; // storage pointer
        // final Uint256 index = payload; //  given index
        // final Uint256 slot = calculateStorageSlot(messageFrame.address, pointer, index, PERVIOUS);
        return address.getStorageValue(slot);
    }

    private Bytes size(final MutableAccount address) {
        // final Uint256 pointer = payload; // storage pointer
        // final Uint256 slot = calculateStorageSlot(messageFrame.address, pointer, Uint256.valueOf(0));
        return address.getStorageValue(slot);
    }

    private Bytes tail(final MutableAccount address) {
        // final Uint256 pointer = payload; // storage pointer
        // final Uint256 slot = calculateStorageSlot(address, pointer, SENTINEL, PREVIOUS);
        return address.getStorageValue(slot);
    }

    //** CALL FUNCTION */
    private Bytes insert(final MutableAccount address, final Bytes payload) {
        final boolean exist = contain(address, payload, messageFrame);
        final Uint256 s = size(address, payload, messageFrame);
        if exist {
            // do nothing
            return FALSE;
        } else if (s.add(1) > MAX_SIZE) {
            // do nothing
            return FALSE;
        } else {
            final Uint256 h = head(address, payload, messageFrame);
            final Uint256 t = tail(address, payload, messageFrame);
            if (s == SENTINEL) {
                // insert first node
                final Uint256 previousSentinel = calculateStorageSlot(address, pointer, SENTINEL, PREVIOUS);
                final Uint256 nextSentinel = calculateStorageSlot(address, pointer, SENTINEL, NEXT);
                final Uint256 previousNode = calculateStorageSlot(address, pointer, index, PREVIOUS);
                final Uint256 nextNode = calculateStorageSlot(address, pointer, index, NEXT);
                address.setStorageValue(previousSentinel, index);
                address.setStorageVaule(nextSentinel, index);
                address.setStorageValue(previousNode, SENTINEL);
                address.setStorageVaule(nextNode, SENTINEL);
            } else if (index < h) {
                // insert head
                final Uint256 nextSentinel = calculateStorageSlot(address, pointer, SENTINEL, NEXT);
                final Uint256 head = calculateStorageSlot(address, pointer, SENTINEL, NEXT);
                final Uint256 previousNode = calculateStorageSlot(address, pointer, index, PREVIOUS);
                final Uint256 nextNode = calculateStorageSlot(address, pointer, index, PREVIOUS);
                address.setStorageValue(nextSentinel, index);
                address.setStorageVaule(head, index);
                address.setStorageValue(previousNode, SENTINEL);
                address.setStorageVaule(nextNode, h);
            } else if (index > t) {
                // insert tail
                final Uint256 previousSentinel = calculateStorageSlot(address, pointer, SENTINEL, NEXT);
                final Uint256 tail = calculateStorageSlot(address, pointer, SENTINEL, NEXT);
                final Uint256 previousNode = calculateStorageSlot(address, pointer, index, PREVIOUS);
                final Uint256 nextNode = calculateStorageSlot(address, pointer, index, PREVIOUS);
                address.setStorageValue(previousSentinel, index);
                address.setStorageVaule(tail, index);
                address.setStorageValue(previousNode, t);
                address.setStorageVaule(nextNode, SENTINEL);
            } else {
                // insert to right position
                Uint256 tmpCurr;
                if (index - h <= t - index) {
                    tmpCurr = h;
                    while (index > tmpCurr) {
                        tmpCurr = next(address, tmpCurr, messageFrame);
                    }
                } else {
                    tmpCurr = t;
                    while (index < tmpCurr) {
                        tmpCurr = previous(address, tmpCurr, massageFrame);
                    }
                }
                Uint256 tmpPrev = calculateStorageSlot(address, pointer, tmpCurr, NEXT);
                address.setStorageValue(tmpPrev, index);
                address.setStorageVaule(tmpCurr, index);
                address.setStorageValue(previousNode, tmpPrev);
                address.setStorageVaule(nextNode, tmpCurr);
            }
            return TRUE;
        }
    }

    private Bytes remove(final MutableAccount address, final Bytes payload, @Nonnull final MessageFrame messageFrame) {
        final boolean exist = contain(address, payload, messageFrame);
        if exist {
            // remove
            return TRUE;
        } else {
            // do nothing
            return FALSE;
        }
    }

    @Override
    public long gasRequirement(final Bytes input) {
        final Bytes function = input.slice(0, 4);
        if (function.equals(REMOVE_SIGNATURE) ||
            function.equals(INSERT_SIGNATURE) ||
            function.equals(SHRINK_SIGNATURE)) {
            // for call function should be high
            return 5000;
        } else {
            // for static call function should be low
            return 100;
        }
    }

    @Nonnull
    @Override
    public PrecompileContractResult computePrecompile(final Bytes input, @Nonnull final MessageFrame messageFrame) {
        if (input.isEmpty()) {
            return PrecompileContractResult.halt(null, Optional.of(ExceptionalHaltReason.PRECOMPILE_ERROR));
        } else {
            // function signature selector.
            final Bytes function = input.slice(0, 4);
            final WorldUpdater worldUpdater = messageFrame.getWorldUpdater();
            final MutableAccount sender = worldUpdater.getOrCreate(messageFrame..getSenderAddress());
            if (function.equals(CONTAINS_SIGNATURE)) {
                return PrecompileContractResult.success(
                    contains(sender, payload, messageFrame)
                    );
            } 
            else if (function.equals(HEAD_SIGNATURE)) {
                return PrecompileContractResult.success(
                    head(sender, payload, messageFrame)
                    );
            }
            else if (function.equals(LIST_SIGNATURE)) {
                return PrecompileContractResult.success(
                    list(sender, payload, messageFrame)
                    );
            }
            else if (function.equals(MIDDLE_SIGNATURE)) {
                return PrecompileContractResult.success(
                    middle(sender, payload, messageFrame)
                    );
            }
            else if (function.equals(NEXT_SIGNATURE)) {
                return PrecompileContractResult.success(
                    next(sender, payload, messageFrame)
                    );
            }
            else if (function.equals(PREVIOUS_SIGNATURE)) {
                return PrecompileContractResult.success(
                    previous(sender, payload, messageFrame));
            }
            else if (function.equals(SIZE_SIGNATURE)) {
                return PrecompileContractResult.success(
                    size(sender, payload, messageFrame)
                    );
            }
            else if (function.equals(TAIL_SIGNATURE)) {
                return PrecompileContractResult.success(
                    tail(sender, payload, messageFrame)
                    );
            }
            else if (function.equals(REMOVE_SIGNATURE)) {
                return PrecompileContractResult.success(
                    remove(sender, payload, messageFrame)
                    );
            }
            else if (function.equals(INSERT_SIGNATURE)) {
                return PrecompileContractResult.success(
                    insert(sender, payload, messageFrame)
                    );
            }
            else if (function.equals(SHRINK_SIGNATURE)) {
                return PrecompileContractResult.success(
                    shrink(sender, payload, messageFrame)
                    );
            } else {
                LOG.info("Failed interface not found");
                return PrecompileContractResult.halt(null, Optional.of(ExceptionalHaltReason.PRECOMPILE_ERROR));
            }
        }
    }
}
