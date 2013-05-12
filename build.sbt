organization  := "com.hahasolutions"

version       := "0.1"

scalaVersion  := "2.10.0"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
  "spray repo" at "http://repo.spray.io/"
)

libraryDependencies ++= Seq(
  "io.spray"            %   "spray-can"     % "1.1-M7",
  "io.spray"            %   "spray-routing" % "1.1-M7",
  "io.spray"            %   "spray-testkit" % "1.1-M7",
  "io.spray"            %%  "spray-json"    % "1.2.3",
  "org.slf4j"           %   "slf4j-nop"     % "1.6.4",
  "com.typesafe.akka"   %%  "akka-actor"    % "2.1.0",
  "com.typesafe.slick"  %%  "slick"         % "1.0.0",
  "com.h2database"      %   "h2"            % "1.3.170",
  "postgresql"          %   "postgresql"    % "9.1-901.jdbc4",
  "org.specs2"          %%  "specs2"        % "1.13" % "test"
)

seq(Revolver.settings: _*)
