package io.openfuture.chain.smartcontract.templates

import io.openfuture.chain.smartcontract.core.annotation.ContractMethod
import io.openfuture.chain.smartcontract.core.model.SmartContract

class CalculatorContract() : SmartContract("") {

    private var result: Long = 0


    @ContractMethod
    fun add(value: Long) {
        result += value
    }

    @ContractMethod
    fun substract(value: Long) {
        result -= value
    }

    @ContractMethod
    fun multiply(value: Long) {
        result *= value
    }

    @ContractMethod
    fun divide(value: Long) {
        required(value != 0L, "Division by zero is not allowed")
        result /= value
    }

    @ContractMethod
    fun clear() {
        result = 0
    }

    @ContractMethod
    fun result(): Long = result

}