import cats.data.Chain
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp, Sync, Timer}
import fs2.Stream

import scala.concurrent.duration._
import scala.util.chaining._
import java.awt.{MouseInfo, Robot}

object Main extends IOApp {
  def schedule[F[_]: Timer](interval: FiniteDuration): Stream[F, Unit] = Stream.fixedDelay[F](interval)

  def shakeMouse[F[_]](rbt: Robot)(implicit F: Sync[F]): F[Unit] = for {
    ptr     <-  F suspend Option(MouseInfo.getPointerInfo).toRight(new Error("No mouse.")).liftTo[F]
    (x, y)  =   ptr.getLocation.pipe(p => (p.x, p.y))
    _       <-  Chain(x + 1, x - 1, x).traverse(F delay rbt.mouseMove(_, y))
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = for {
    maybeInterval <-  args.headOption traverse { head =>
      IO.fromOption(head.toIntOption.map(_.minutes))(new IllegalArgumentException("Argument should be an Int."))
    }
    interval      =   maybeInterval getOrElse 30.minutes
    rbt           <-  IO(new Robot)
    _             <-  (schedule(interval) >> Stream.eval_(shakeMouse[IO](rbt))).compile.drain
  } yield ExitCode.Success
}
