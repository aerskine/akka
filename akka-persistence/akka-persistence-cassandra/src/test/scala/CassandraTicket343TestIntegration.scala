/**
 * Copyright (C) 2009-2010 Scalable Solutions AB <http://scalablesolutions.se>
 */

package se.scalablesolutions.akka.persistence.cassandra


import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import se.scalablesolutions.akka.persistence.common._

@RunWith(classOf[JUnitRunner])
class CassandraTicket343TestIntegration extends Ticket343Test  {
  def dropMapsAndVectors: Unit = {
    //
  }

  def getVector: (String) => PersistentVector[Array[Byte]] = CassandraStorage.getVector

  def getMap: (String) => PersistentMap[Array[Byte], Array[Byte]] = CassandraStorage.getMap

}