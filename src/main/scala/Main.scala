import cats.Id
import cats.data.{Chain, OptionT}
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp, Sync, Timer}
import fs2.Stream

import scala.concurrent.duration._
import java.awt.{MouseInfo, Point, Robot}

object Main extends IOApp {
  def schedule[F[_]: Timer](interval: FiniteDuration): Stream[F, Unit] = Stream.fixedDelay[F](interval)

  def shakeMouse[F[_]](rbt: Robot)(implicit F: Sync[F]): F[Unit] = for {
    ptr     <-  F suspend Option(MouseInfo.getPointerInfo).toRight(new Error("No mouse.")).liftTo[F]
    (x, y)  =   (ptr.getLocation: Id[Point]).map(p => (p.x, p.y))
    _       <-  Chain(x + 1, x - 1, x).traverse(F delay rbt.mouseMove(_, y))
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = for {
    maybeInterval <-  (for {
      s <-  args.headOption.toOptionT[IO]
      i <-  OptionT liftF IO.fromOption(s.toIntOption)(new IllegalArgumentException("Argument should be an Int."))
    } yield i.minutes).value
    interval      =   maybeInterval getOrElse 30.minutes
    rbt           <-  IO(new Robot)
    _             <-  (schedule(interval) >> Stream.eval_(shakeMouse[IO](rbt))).compile.drain
  } yield ExitCode.Success
}
