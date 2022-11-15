package hu.bme.hit.hu.mcold.gordian.security

class SenderKeyProvider(baseKey: MySecretKey, seqNum: Int) : MyKeyProvider(baseKey, seqNum) {
}