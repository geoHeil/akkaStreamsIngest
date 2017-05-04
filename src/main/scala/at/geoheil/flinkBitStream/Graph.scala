//// Copyright (C) 2017 Georg Heiler
//package at.geoheil.flinkBitStream
//
//
//import akka.actor.ActorSystem
//import akka.stream._
//import akka.stream.scaladsl._
//
//object Graph extends App {
//
//  implicit val system = ActorSystem("Sys")
//  implicit val materializer = ActorMaterializer()
//
//  val g: RunnableGraph[_] = RunnableGraph.fromGraph(GraphDSL.create() {
//    implicit builder =>
//
//      // Source
//      val A: Outlet[String] = builder.add(Source.fromIterator(() => flightDelayLines)).out
//      val B: FlowShape[String, FlightEvent] = builder.add(csvToFlightEvent)
//      val C: Inlet[Any] = builder.add(Sink.ignore).in
//
//      import GraphDSL.Implicits._ // allows us to build our graph using ~> combinators
//
//      // Graph
//      A ~> B ~> C
//
//      ClosedShape // defines this as a "closed" graph, not exposing any inlets or outlets
//  })
//
//  g.run() // materializes and executes the blueprint
//
//  val csvToFlightEvent = Flow[String]
//    .map(_.split(",").map(_.trim)) // we now have our columns split by ","
//    .map(stringArrayToFlightEvent) // we convert an array of columns to a FlightEvent
//  val flightDelayLines: Iterator[String] = io.Source.fromFile("src/main/resources/2008.csv", "utf-8").getLines()
//
//  // string array to FlightEvent
//  def stringArrayToFlightEvent(cols: Array[String]) = new FlightEvent(cols(0), cols(1), cols(2), cols(3), cols(4), cols(5), cols(6), cols(7), cols(8), cols(9), cols(10), cols(11), cols(12), cols(13), cols(14), cols(15), cols(16), cols(17), cols(18), cols(19), cols(20), cols(21), cols(22), cols(23), cols(24), cols(25), cols(26), cols(27), cols(28))
//
//  case class FlightEvent(
//                          year: String,
//                          month: String,
//                          dayOfMonth: String,
//                          dayOfWeek: String,
//                          depTime: String,
//                          scheduledDepTime: String,
//                          arrTime: String,
//                          scheduledArrTime: String,
//                          uniqueCarrier: String,
//                          flightNum: String,
//                          tailNum: String,
//                          actualElapsedMins: String,
//                          crsElapsedMins: String,
//                          airMins: String,
//                          arrDelayMins: String,
//                          depDelayMins: String,
//                          originAirportCode: String,
//                          destinationAirportCode: String,
//                          distanceInMiles: String,
//                          taxiInTimeMins: String,
//                          taxiOutTimeMins: String,
//                          flightCancelled: String,
//                          cancellationCode: String, // (A = carrier, B = weather, C = NAS, D = security)
//                          diverted: String, // 1 = yes, 0 = no
//                          carrierDelayMins: String,
//                          weatherDelayMins: String,
//                          nasDelayMins: String,
//                          securityDelayMins: String,
//                          lateAircraftDelayMins: String)
//
//  case class FlightDelayRecord(
//                                year: String,
//                                month: String,
//                                dayOfMonth: String,
//                                flightNum: String,
//                                uniqueCarrier: String,
//                                arrDelayMins: String) {
//    override def toString = s"${year}/${month}/${dayOfMonth} - ${uniqueCarrier} ${flightNum} - ${arrDelayMins}"
//  }
//
//}
