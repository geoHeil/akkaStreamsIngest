// Copyright (C) 2017 Georg Heiler
package at.geoheil.flinkBitStream

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.{ Done, NotUsed }
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer

import scala.concurrent.Future

object WebSocketClientFlow extends App {
  //  import scala.concurrent.ExecutionContext.Implicits.global // TODO bad
  import system.dispatcher

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  //  val A: Outlet[Tex] = builder.add(Source.fromIterator(() => TextMessage("{\"op\":\"unconfirmed_sub\"}"))).out
  //  val outgoing2 = Source.single(TextMessage("{\"op\":\"unconfirmed_sub\"}")).concatMat(Source.maybe)(Keep.right)

  val incoming: Flow[Message, String, NotUsed] =
    Flow[Message].flatMapConcat {
      case message: TextMessage =>
        message.textStream
      case message: BinaryMessage =>
        message.dataStream.runWith(Sink.ignore)
        Source.empty
    }

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val outgoing = Source.single(TextMessage("{\"op\":\"unconfirmed_sub\"}")).concatMat(Source.maybe)(Keep.right)

  // flow to use (note: not re-usable!)
  val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest("wss://ws.blockchain.info/inv"))

  val ((completionPromise, upgradeResponse), closed) =
    outgoing
      .viaMat(webSocketFlow)(Keep.both)
      .via(incoming)
      .map { elem =>
        println(s"PlainSinkProducer produce: ${elem}")
        new ProducerRecord[Array[Byte], String]("topic1", elem)
      }
      .toMat(Producer.plainSink(producerSettings))(Keep.both)
      .run()
  //  val incoming: Sink[Message, Future[Done]] =
  //    Flow[Message].mapAsync(4) {
  //      case message: TextMessage.Strict =>
  //        //        println(message.text)
  //        println("#######" + message.text)
  //        Future.successful(Done)
  //      case message: TextMessage.Streamed =>
  //        message.textStream.runForeach(println)
  //      case message: BinaryMessage =>
  //        message.dataStream.runWith(Sink.ignore)
  //    }.toMat(Sink.last)(Keep.right)

  //  val kafkaProducer = producerSettings.createKafkaProducer()

  //  val ((completionPromise, upgradeResponse), closed) =
  //    outgoing
  //      .viaMat(webSocketFlow)(Keep.both)
  //      .toMat(incoming)(Keep.both)
  //      // .map(_.toString)
  //      //    .map { elem =>
  //      //      println(s"PlainSinkProducer produce: ${elem}")
  //      //      new ProducerRecord[Array[Byte], String]("topic1", elem)
  //      //    }
  //      //    .runWith(Producer.plainSink(producerSettings))
  //      .run()

  // just like a regular http request we can access response status which is available via upgrade.response.status
  // status code 101 (Switching Protocols) indicates that server support WebSockets
  val connected = upgradeResponse.flatMap { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      Future.successful(Done)
    } else {
      throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
    }
  }

  // in a real application you would not side effect here
  //  connected.onComplete(println)
  //  closed.foreach(_ => {
  //    println("closed")
  //    system.terminate
  //  })

  //  val done1 = Source(1 to 100)
  //    .map(_.toString)
  //    .map { elem =>
  //      println(s"PlainSinkProducer produce: ${elem}")
  //      new ProducerRecord[Array[Byte], String]("topic1", elem)
  //    }
  //    .runWith(Producer.plainSink(producerSettings))
}