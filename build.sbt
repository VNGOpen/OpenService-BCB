import com.typesafe.config._

val conf       = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
val appName    = conf.getString("app.name").toLowerCase().replaceAll("\\W+", "-")
val appVersion = conf.getString("app.version")

EclipseKeys.skipParents in ThisBuild := false
EclipseKeys.preTasks                 := Seq(compile in Compile)
EclipseKeys.projectFlavor            := EclipseProjectFlavor.Scala
EclipseKeys.eclipseOutput            := Some(".target")
EclipseKeys.executionEnvironment     := Some(EclipseExecutionEnvironment.JavaSE18)
//EclipseKeys.createSrc                := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
//EclipseKeys.createSrc                := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
EclipseKeys.createSrc                  := EclipseCreateSrc.ManagedClasses + EclipseCreateSrc.ManagedResources + EclipseCreateSrc.Unmanaged + EclipseCreateSrc.Managed + EclipseCreateSrc.Source + EclipseCreateSrc.Resource

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

routesGenerator := InjectedRoutesGenerator

pipelineStages := Seq(rjs, digest, gzip)

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayScala).settings(
    name    := appName,
    version := appVersion
)

scalaVersion := "2.11.7"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"

val _springVersion           = "4.3.4.RELEASE"
val _ddthCacheAdapterVersion = "0.5.1"
val _ddthCommonsVersion      = "0.6.0.1"
val _ddthDaoVersion          = "0.6.0.3"

libraryDependencies ++= Seq(
    "org.slf4j"                  %  "log4j-over-slf4j"             % "1.7.21",

    "org.springframework"        %  "spring-beans"                 % _springVersion,
    "org.springframework"        %  "spring-expression"            % _springVersion,
    "org.springframework"        %  "spring-jdbc"                  % _springVersion,

//    "redis.clients"              %  "jedis"                        % "2.9.0",
//    "com.github.ddth"            %  "ddth-cache-adapter-core"      % _ddthCacheAdapterVersion,
//    "com.github.ddth"            %  "ddth-cache-adapter-redis"     % _ddthCacheAdapterVersion,

//    "com.github.ddth"            %  "ddth-commons-core"            % _ddthCommonsVersion,
//    "com.github.ddth"            %  "ddth-commons-serialization"   % _ddthCommonsVersion,

    "com.github.ddth"            %  "ddth-dao-core"                % _ddthDaoVersion,

    "com.github.ddth"            %  "ddth-cql-utils"               % "0.3.0",
    "org.xerial.snappy"          %  "snappy-java"                  % "1.1.2.6",
    "net.jpountz.lz4"            %  "lz4"                          % "1.3.0"
)
