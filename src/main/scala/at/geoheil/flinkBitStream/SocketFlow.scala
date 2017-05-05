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
  import system.dispatcher

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

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
        s"""
          |{
          |"origin":"bitstamp",
          |"data": ${elem}
          |}
        """.stripMargin
      }
      .map { elem =>
        println(s"PlainSinkProducer produce: ${elem}")
        new ProducerRecord[Array[Byte], String]("topic1", elem)
      }
      .toMat(Producer.plainSink(producerSettings))(Keep.both)
      .run()

  val connected = upgradeResponse.flatMap { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      Future.successful(Done)
    } else {
      throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
    }
  }
}