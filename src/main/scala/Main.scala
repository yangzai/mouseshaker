import cats.data.{Chain, OptionT}
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp, Sync, Timer}
import fs2.Stream

import scala.concurrent.duration._
import scala.util.chaining._
import java.awt.{MouseInfo, PointerInfo, Robot}
import java.awt.event.KeyEvent

object Main extends IOApp {
  def schedule[F[_]: Timer](interval: FiniteDuration): Stream[F, Unit] = Stream.fixedDelay[F](interval)

  def safeGetPointerInfo[F[_]](implicit F: Sync[F]): OptionT[F, PointerInfo] = OptionT(F delay Option(MouseInfo.getPointerInfo))

  def shakeMouseOrF15[F[_]](rbt: Robot)(implicit F: Sync[F]): F[Unit] = safeGetPointerInfo.handleError() match {
    case Some(ptr) =>
      val (x, y) = ptr.getLocation.pipe(p => (p.x, p.y))
      Chain(x + 1, x - 1, x).traverse(F delay rbt.mouseMove(_, y)) *> F.delay(println(x, y))
    case _ =>
      (F delay rbt.keyPress(KeyEvent.VK_F15)) *> F.delay(println("key event"))
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    maybeInterval <-  args.headOption traverse { head =>
      IO.fromOption(head.toIntOption.map(_.minutes))(new IllegalArgumentException("Argument should be an Int."))
    }
    interval      =   maybeInterval getOrElse 30.minutes
    rbt           <-  IO(new Robot)
    _             <-  (schedule(interval) >> Stream.eval_(shakeMouseOrF15[IO](rbt))).compile.drain
  } yield ExitCode.Success
}
