package client

import java.io.{DataInputStream, DataOutputStream, ObjectInputStream}
import java.net.{InetAddress, Socket}

import mutual.{Gate, GateStateNames}
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Slider, TextField}
import mutual.Settings.{serverName, serverPort}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Client extends JFXApp {
  var onOff = true
  var gate = request("get")
  stage = new JFXApp.PrimaryStage {
    title = "Automat do bramy wjazdowej"
    scene = new Scene(400,200){
      val openButton = new Button("Open")
      openButton.layoutX = 250
      openButton.layoutY = 70
      openButton.prefWidth = 100

      val closeButton = new Button("Close")
      closeButton.layoutX = 50
      closeButton.layoutY = 70
      closeButton.prefWidth = 100

      val stopButton = new Button("Stop")
      stopButton.layoutX = 150
      stopButton.layoutY = 70
      stopButton.prefWidth = 100

      val slider = new Slider(0,10,0)
      slider.layoutX = 50
      slider.layoutY = 20
      slider.prefWidth = 300
      slider.majorTickUnit = 1
      slider.showTickLabels = true
      slider.disable = true

      val textField = new TextField()
      textField.layoutX = 50
      textField.layoutY = 120
      textField.prefWidth = 300

      adjustElements(slider, openButton, closeButton, textField)

      openButton.onAction = () => {
        onOff = true
        request("open", slider, openButton, closeButton, textField)
      }

      closeButton.onAction = () => {
        onOff = true
        request("close", slider, openButton, closeButton, textField)
      }

      stopButton.onAction = () => {
        onOff = false
        request("stop", slider, openButton, closeButton, textField)
      }

      content = List(openButton, closeButton, stopButton, slider, textField)
    }
  }
  def request(action: String, slider: Slider = new Slider(), openButton: Button = new Button(), closeButton: Button = new Button(), textField: TextField = new TextField()): Gate = {
    action match {
      case "get" => {
        val socket = new Socket(InetAddress.getByName(serverName), serverPort)
        val objectStreamIn = new ObjectInputStream(new DataInputStream(socket.getInputStream()))
        val streamOut = new DataOutputStream(socket.getOutputStream())
        streamOut.writeUTF(action)
        streamOut.flush()
        val response = objectStreamIn.readObject().asInstanceOf[Gate]
        socket.close()
        response
      }
      case "open" => {
        Future {
          while(onOff && gate.stateName != GateStateNames.Open) {
            val socket = new Socket(InetAddress.getByName(serverName), serverPort)
            val objectStreamIn = new ObjectInputStream(new DataInputStream(socket.getInputStream()))
            val streamOut = new DataOutputStream(socket.getOutputStream())
            streamOut.writeUTF(action)
            streamOut.flush()
            gate = objectStreamIn.readObject().asInstanceOf[Gate]
            Platform.runLater(adjustElements(slider, openButton, closeButton, textField))
            socket.close()
            Thread.sleep (1000)
          }
        }
        gate
      }
      case "close" => {
        Future {
          while(onOff && gate.stateName != GateStateNames.Closed){
            val socket = new Socket(InetAddress.getByName(serverName), serverPort)
            val objectStreamIn = new ObjectInputStream(new DataInputStream(socket.getInputStream()))
            val streamOut = new DataOutputStream(socket.getOutputStream())
            streamOut.writeUTF(action)
            streamOut.flush()
            gate = objectStreamIn.readObject().asInstanceOf[Gate]
            Platform.runLater(adjustElements(slider, openButton, closeButton, textField))
            socket.close()
            Thread.sleep(1000)
          }
        }
        gate
      }
      case "stop" => {
        Future {
          val socket = new Socket(InetAddress.getByName(serverName), serverPort)
          val objectStreamIn = new ObjectInputStream(new DataInputStream(socket.getInputStream()))
          val streamOut = new DataOutputStream(socket.getOutputStream())
          streamOut.writeUTF(action)
          streamOut.flush()
          gate = objectStreamIn.readObject().asInstanceOf[Gate]
          Platform.runLater(adjustElements(slider, openButton, closeButton, textField))
          socket.close()
        }
        gate
      }
    }
  }

  def adjustElements(slider: Slider, openButton: Button, closeButton: Button, textField: TextField): Unit = {
    slider.value = gate.state
    gate.stateName match {
      case GateStateNames.Open => {
        openButton.disable = true
        closeButton.disable = false
      }
      case GateStateNames.Closed => {
        closeButton.disable = true
        openButton.disable = false
      }
      case GateStateNames.InProgressClose => {
        closeButton.disable = true
        openButton.disable = true
      }
      case GateStateNames.InProgressOpen => {
        closeButton.disable = true
        openButton.disable = true
      }
      case GateStateNames.Stopped => {
        openButton.disable = false
        closeButton.disable = false
      }
    }
    textField.text = gate.stateName match {
      case GateStateNames.Open => "Open"
      case GateStateNames.Closed => "Closed"
      case GateStateNames.InProgressOpen => "In progress of opening"
      case GateStateNames.InProgressClose => "In progress of closing"
      case GateStateNames.Stopped => "Stopped"
    }
  }

}
