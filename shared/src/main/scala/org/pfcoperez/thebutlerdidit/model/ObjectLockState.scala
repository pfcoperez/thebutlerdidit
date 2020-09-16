package org.pfcoperez.thebutlerdidit.model

sealed trait ObjectLockState {
  def address: BigInt
  def className: String
}

object ObjectLockState {
  val factories: Map[String, (BigInt, String) => ObjectLockState] = Map(
    "locked" -> LockedObject.apply,
    "waiting to lock" -> LockRequest.apply,
    "waiting to re-lock in wait()" -> LockRequest.apply,
    "waiting on" -> LockRequest.apply
  )

  val representations: Seq[String] = factories.keys.toSeq

  case class LockedObject(address: BigInt, className: String) extends ObjectLockState
  case class LockRequest(address: BigInt, className: String) extends ObjectLockState
}

