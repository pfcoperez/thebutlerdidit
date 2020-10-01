package org.pfcoperez.thebutlerdidit.model

case class DeadLockElement(
    blockedThreadName: String,
    objectAddr: BigInt,
    ownerThreadName: String
)
