package server

import java.io.{DataInputStream, DataOutputStream, ObjectOutputStream}
import java.net.ServerSocket

import mutual.Settings.serverPort
import mutual.Gate

object Server extends App {
  val server = new ServerSocket(serverPort)
  val gate = Gate()
  while (true) {
    val socket = server.accept()
    val objectStreamOut = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()))
    val streamIn = new DataInputStream(socket.getInputStream())
    streamIn.readUTF() match {
      case "open" => {
        println("Open request")
        gate.openByOneStep()
      }
      case "close" => {
        println("Close request")
        gate.closeByOneStep()
      }
      case "stop" => {
        println("Stop request")
        gate.stop()
      }
      case "get" => println("Get request")
      case _ => println("Unknown request")
    }
    objectStreamOut.writeObject(gate)
    objectStreamOut.flush()
    socket.close()
  }
}