package de.sciss.cupola.relais

import java.awt.EventQueue
import de.sciss.net._
import java.net.{InetSocketAddress, SocketAddress}

object CupolaRelais extends OSCListener {
   val protocol1  = OSCChannel.TCP
   val port1      = 1201
   val loopback1  = false

//   val protocol2  = OSCChannel.UDP
//   val port2      = 0x6375
//   val loopback2  = true

   val s          = OSCServer.newUsing( protocol1, port1, loopback1 )
//   val r          = OSCReceiver.newUsing( protocol2, port2, loopback2 )
   var notified   = Set.empty[ InetSocketAddress ]
   val monitor    = new AnyRef

   def main( args: Array[ String ]) {
      log( "Starting server at " + s.getLocalAddress() + " (" + protocol1 + ")" )
      s.start
//      s.dumpOSC(1,System.out)
      s.addOSCListener( this )
      Runtime.getRuntime().addShutdownHook( new Thread { override def run = shutDown })
      // XXX how to avoid the VM from instantaneous quit?
      (new Thread { override def run { monitor.synchronized { monitor.wait }}}).start
   }

   def messageReceived( msg: OSCMessage, addr: SocketAddress, time: Long ) {
      msg getName match {
//         case "/t" => relay( msg )
         case "/dumpOSC" => msg.getArg( 0 ) match {
            case i: java.lang.Integer => dumpOSC( i.intValue )
            case x => log( "Oooops : " + msg + " --> " + x.getClass )
         }
         case "/notify" => msg.getArg( 0 ) match {
            case i: java.lang.Integer => notify( addr.asInstanceOf[ InetSocketAddress ], i.intValue == 1 )
            case x => log( "Oooops : " + msg + " --> " + x.getClass )
         }
//         case _ => log( "Ignoring unknown message : " + msg )
         case _ => relay( msg )
      }
   }

   def dumpOSC( mode: Int ) {
      s.dumpIncomingOSC( mode, System.out )
   }

   def notify( addr: InetSocketAddress, onOff: Boolean ) {
println( "NOTIFY " + addr + " : " + onOff )
      if( onOff ) {
         notified += addr
      } else {
         notified -= addr
      }
   }

   def relay( msg: OSCMessage ) {
      notified foreach { addr =>
         try {
            s.send( msg, addr )
         }
         catch {
            case e => {
               log( "Error sending to " + addr + " : " + e.getClass + " -> disconnecting!" )
               notify( addr, false )
            }
         }
      }
   }

   def shutDown {
      log( "Shutting down server..." )
      s.dispose
   }

   def log( msg: String ) {
      println( msg )
   }
}