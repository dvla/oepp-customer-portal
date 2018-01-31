resolvers += Resolver.url("sass-sbt-plugin-releases", url("https://dl.bintray.com/jcage/generic"))(Resolver.ivyStylePatterns)
resolvers += Resolver.url("simple-url-update-sbt-plugin-releases", url("https://dl.bintray.com/geek-soft/sbt-plugins"))(Resolver.ivyStylePatterns)

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.3")

// Source plugins
addSbtPlugin("default" % "sbt-sass" % "0.1.9")

// Web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")
addSbtPlugin("org.neolin.sbt" % "sbt-simple-url-update" % "1.0.1")
addSbtPlugin("net.ground5hark.sbt" % "sbt-css-compress" % "0.1.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "1.0.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")
