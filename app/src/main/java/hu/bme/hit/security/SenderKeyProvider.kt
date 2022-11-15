package hu.bme.hit.security

class SenderKeyProvider(baseKey: MySecretKey, seqNum: Int) : MyKeyProvider(baseKey, seqNum) {
}