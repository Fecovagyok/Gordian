package com.example.szakchat.security

class SenderKeyProvider(baseKey: MySecretKey, seqNum: Int) : MyKeyProvider(baseKey, seqNum) {
}