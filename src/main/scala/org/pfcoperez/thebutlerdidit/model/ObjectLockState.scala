package org.pfcoperez.thebutlerdidit.model

sealed trait ObjectLockState {
  def address: BigInt
}

object ObjectLockState {
  val factories: Map[String, BigInt => ObjectLockState] = Map(
    "locked" -> LockedObject.apply,
    "waiting to lock" -> LockRequest.apply
  )

  val representations: Seq[String] = factories.keys.toSeq

  case class LockedObject(address: BigInt) extends ObjectLockState
  case class LockRequest(address: BigInt) extends ObjectLockState
}

