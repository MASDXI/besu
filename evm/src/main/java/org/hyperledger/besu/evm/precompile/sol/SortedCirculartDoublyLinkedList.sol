// SPDX-License-Identifier: MIT
pragma solidity >=0.5.0 <0.8.0;

/// @title Sorted Circular Doubly Linked List library that call stateful precompile contract.
/// @author Kirawri Labs

library SortedCirculartDoublyLinkedList {
    struct List {
        uint256 _size;
        mapping(uint256 => mapping(bool => uint256)) _nodes;
    }

    // Change to your desirable address.
    address constant StatefulPrecompiledContract = address("0x0");

    function contains(
        List storage self,
        uint256 index
    ) internal view returns (bool) {
        (bool success, bytes memory response) = StatefulPrecompiledContract.staticcall(
            abi.encodeWithSignature("remove(bytes32,uint256)", List, index)
        );
        require(success);
        return (abi.decode(response, (bool)));
    }

    function head(List storage self) internal view returns (uint256) {
        (bool success, bytes memory response) = StatefulPrecompiledContract.staticcall(
            abi.encodeWithSignature("head(bytes32)", List)
        );
        require(success);
        return (abi.decode(response, (uint256)));
    }

    function list(List storage self) internal view returns (uint256[] memory) {
        (bool success, bytes memory response) = StatefulPrecompiledContract.staticcall(
            abi.encodeWithSignature("list(bytes32)", List)
        );
        require(success);
        return (abi.decode(response, (uint256[])));
    }

    function middle(List storage self) internal view returns (uint256) {
        (bool success, bytes memory response) = StatefulPrecompiledContract.staticcall(
            abi.encodeWithSignature("middle(bytes32)", List)
        );
        require(success);
        return (abi.decode(response, (uint256)));
    }

    function next(
        List storage self,
        uint256 index
    ) internal view returns (uint256) {
        (bool success, bytes memory response) = StatefulPrecompiledContract.staticcall(
            abi.encodeWithSignature("next(bytes32,uint256)", List, index)
        );
        require(success);
        return (abi.decode(response, (uint256)));
    }

    function previous(uint256 index) internal view returns (uint256) {
        (bool success, bytes memory response) = StatefulPrecompiledContract.staticcall(
            abi.encodeWithSignature("previous(bytes32,uint256)", List, index)
        );
        require(success);
        return (abi.decode(response, (uint256)));
    }

    function size(List storage self) internal view returns (uint256) {
        (bool success, bytes memory response) = StatefulPrecompiledContract.staticcall(
            abi.encodeWithSignature("size(bytes32)", List)
        );
        require(success);
        return (abi.decode(response, (uint256)));
    }

    function tail(List storage self) internal view returns (uint256) {
        (bool success, bytes memory response) = StatefulPrecompiledContract.staticcall(
            abi.encodeWithSignature("tail(bytes32)", List)
        );
        require(success);
        return (abi.decode(response, (uint256)));
    }

    function remove(List storage self, uint256 index) internal {
        (bool success, bytes memory response) = StatefulPrecompiledContract.call(
            abi.encodeWithSignature("remove(uint256)", List, index)
        );
        require(success);
    }

    function insert(List storage self, uint256 index, uint256 value) internal {
        (bool success, bytes memory response) = StatefulPrecompiledContract.call(
            abi.encodeWithSignature(
                "insert(bytes32,uint256,uint256)",
                List,
                index,
                value
            )
        );
        require(success);
    }

    function shrink(List storage self, uint256 index) internal {
        (bool success, bytes memory response) = StatefulPrecompiledContract.call(
            abi.encodeWithSignature("shrink(bytes32,uint256)", List, index)
        );
        require(success);
    }
}
