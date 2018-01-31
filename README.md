Customer Portal
================

Project provides server component for web tier.

Component uses Play Framework (https://www.playframework.com) for serving web assets and handling web tier logic.

Look and feel provided by web application is achieved reusing following gov.uk web components (https://github.com/alphagov):

 * gov.uk template
 * gov.uk toolkit
 * gov.uk elements

Application specific assets are developed using SASS superset instead of pure CSS (even if Play Framework is supporting LESS by default). Support for SASS is provided by external plugin which requires `sass` compiler installed (http://sass-lang.com/install).

## Requirements

 * Java JDK 1.8+
 * Maven 3+
 * SBT 0.13.8+
 * sass compiler
 * Redis
 * RabbitMQ (in order to enable auditing)

## How to build it

This project, as every Play Framework project, uses SBT as main build tool. Additionally project also uses Maven as this is the only build system supported by DSP CI servers.

Maven integration is done using [play2-maven-plugin](http://nanoko-project.github.io/maven-play2-plugin/maven/release) which simply delegates all heavy lifting down to SBT which means that `sbt` command must be available from the system PATH.

Which build system is used is down to the personal preferences but we feel that using native build system makes more sense so we will continue using SBT in this documentation.

The only scenario where Maven is necessary is dependency managing. Maven copies all runtime dependencies into `lib-runtime` directory and similarly test dependencies are collected in `lib-test` directory.

To update dependencies execute following command:

```bash
  mvn clean play2:copy-dependencies
```

To build this project execute the following command:

```bash
  sbt clean stage
```

To build ZIP archive execute the following command:

```bash
  sbt clean dist
```

## How to configure it

This service is mostly configured with reasonable defaults (see: `/conf/environment.conf`) but for security reasons not everything can be shared. Because of that, some values are expected to be provided as environment variables. To do that please put following variables to your `.profile` file (or any other alternative you use for environment variables).

```
export AWS_REGION='your value here'
export AWS_ACCESS_KEY_ID='your value here'
export AWS_SECRET_ACCESS_KEY='your value here'

export DVLA_OEP_RECEIPT_SENDER='your value here'

export DVLA_OEP_FEEDBACK_SENDER='your value here'
export DVLA_OEP_FEEDBACK_RECIPIENT='your value here'
```

## How to run it

To run the web application execute the following command (you may have to change the version in this):

```bash
./target/universal/stage/bin/customer-portal
```

Alternatively web application can be run in debug mode using following command:

```bash
sbt run
```

Debug mode is particularly useful during development cycle as it supports hot-deployment which means changes are visible in the browser shortly after beeing made in the source file.

## How to use it

When web application is running open `http://localhost:9000` in the favorite browser and start page will be displayed.

## Design decisions

### Logging

Project uses Mapped Diagnostic Context (see: [http://logback.qos.ch/manual/mdc.html]) to print `case number` and `vehicle registration mark` in application logs.

The place where MDC context is initialised / cleared is a `logging.MDCInterceptor`. That interceptor is applied to all public, package-private or protected methods on classes which extend `play.mvc.Controller`.

Unfortunately due to the fact that default MDC implementation strongly relies on local threads and that a copy of the mapped diagnostic context can not always be inherited by worker threads from the initiating thread, a customised Akka dispatcher is need. For more information please see `logging.MDCPropagatingDispatcherConfigurator` class.


### Access Logs

The access log format is a standard format (Combined logging format https://httpd.apache.org/docs/1.3/logs.html#combined) and is defined in the `AccessLogFilter` class.

By default it creates a console appender, which means that it will print all access logs to the console alongside the application logs. It is possible to specify a different type of `OutputStreamAppender` to use such as a file appender:

```
<appender name="ACCESS_LOG" class="ch.qos.logback.core.FileAppender">
    <file>access.log</file>
</appender>
```

This will then be used instead of the default console appender. It's important to note that the appender name must be `ACCESS_LOG` and also that any pattern that is specified will be ignored.