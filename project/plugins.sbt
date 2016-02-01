addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.1")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.9")

logLevel := Level.Warn
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

