package safety.events

import java.io.File

import org.scalatest._
import scala.reflect.runtime.universe._

import org.clapper.classutil.ClassFinder

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.testkit.TestKit

/* Attempts to work with scala annotation */
class EvilConjurerSpec(_system: ActorSystem)
    extends TestKit(_system)
        with Matchers
        with WordSpecLike
        with BeforeAndAfterAll {

  def this() = this(ActorSystem("TestActorSystem"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "A listener" should {
    "receive a chained RideStart event" in {

      val mirror = runtimeMirror(getClass.getClassLoader)

      def instanceOfActor(actorSystem: ActorSystem, actorType: Class[_]) = {
        val actor = actorType.newInstance().asInstanceOf[Actor]
        actorSystem.actorOf(Props(actor), "darth")
      }

      val a = typeTag[SubscribeTo[_]]
      val b = a.tpe
      val c = b.typeSymbol
      val d = c.owner
      val e = d.typeSignature
      val f = e.decls
      val g = f.filter(_.isPublic).filter(_.isClass).map(_.asClass)

      val pp = typeOf[Used]
      val annn = pp.typeSymbol.owner.map(e => e.info.typeSymbol)
      val classpath = List(".").map(new File(_))
      val finder = ClassFinder(classpath)
      val classes = finder.getClasses()
      val annotatedClasses = classes.filter(_.annotations.nonEmpty).toList
      val res = annotatedClasses.filter(_.annotations.size > 1).flatMap { c =>
        c.annotations.find { ann =>
          ann.descriptor.contains(classOf[SubscribeToJava].getName.replace('.', '/'))
        }.map { subscribeToAnnotation => (c, subscribeToAnnotation) }
      }

      val h = typeOf[EvilConjurer].typeSymbol.owner.info.decls
      val any = annotatedClasses

      def eventType(eventType: Type) = {
        val annotations = eventType.typeSymbol.asClass.annotations

        annotations.map { annotation =>
          //Chack filter only SubscribeTo annotation
          val annotationArgClass = annotation.tree.tpe.typeArgs.head.typeSymbol.asClass
          val clazz = mirror.runtimeClass(annotationArgClass)
          clazz
        }.headOption
      }


      //      val myAnnotatedClass: ClassSymbol = runtimeMirror(Thread.currentThread().getContextClassLoader).staticClass
      // ("MyAnnotatedClass")
      //      val annotation: Option[Annotation] = myAnnotatedClass.annotations.find(_.tree.tpe =:= typeOf[MyAnnotationClass])
      //      val result = annotation.flatMap { a =>
      //        a.tree.children.tail.collect({ case Literal(Constant(id: String)) => doSomething(id) }).headOption
      //      }

      eventType(typeOf[BlackMagicSubscriber]).map { clazz =>
        system.eventStream.subscribe(testActor, clazz)
      }

      system.eventStream.publish(BlackMagicEvent(2))

      expectMsgPF() {
        case BlackMagicEvent(power) => assert(power === 2)
      }
    }
  }


}

trait Used

class NotUsed extends Used

class UsedClass extends Used

class SubscribeTo[T <: AnyEvent](eventType: Class[T]) extends scala.annotation.ClassfileAnnotation {}

trait AnyEvent

case class BlackMagicEvent(power: Int) extends AnyEvent

@SubscribeTo(eventType = classOf[BlackMagicEvent])
class BlackMagicSubscriber(other: ActorRef) extends Actor {
  override def receive = {
    case magicConjured => other ! magicConjured
  }
}

@SubscribeToJava(t = classOf[BlackMagicEvent])
class X

class EvilConjurer {

  val mirror = runtimeMirror(getClass.getClassLoader)

  def instanceOfActor(actorSystem: ActorSystem, actorType: Class[_]) = {
    val actor = actorType.newInstance().asInstanceOf[Actor]
    actorSystem.actorOf(Props(actor), "darth")
  }

  def eventType(eventType: Type) = {
    val annotations = eventType.typeSymbol.asClass.annotations

    annotations.map { annotation =>
      val annotationArgClass = annotation.tree.tpe.typeArgs.head.typeSymbol.asClass
      val clazz = mirror.runtimeClass(annotationArgClass)
      clazz
    }
  }
}
