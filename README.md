spreedly-scala [![Build Status](https://travis-ci.org/Ticketfly/spreedly-scala.png)](https://travis-ci.org/Ticketfly/spreedly-scala) [![Coverage Status](https://img.shields.io/coveralls/Ticketfly/spreedly-scala.svg)](https://coveralls.io/r/Ticketfly/spreedly-scala?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ticketfly/spreedly-client_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ticketfly/spreedly-client_2.11)
==========


Async Spreedly payment service client written in Scala, wrapping all third-party requests in Futures.

Spreedly domain objects are mapped to XML fields using the JAXB implementation in [spreedly-java](https://github.com/rjstanford/spreedly-java).


## Usage
Include as a dependency
```scala
libraryDependencies += "com.ticketfly.spreedly" %% "spreedly-client" % "1.0.0"
```

```scala
val config = SpreedlyConfiguration("environmentToken", "accessSecret")
val spreedlyClient = new SpreedlyClient(config)

// Make a purchase of $1 using a gateway token and payment method token
spreedlyClient.purchase("gaToken", "pmToken", 100, "USD").map(response => {
    if (response.succeeded) log.info(s"Successfully completed purchase with token ${response.getToken}")
    else log.error(s"Could not complete purchase, response errors: ${response.errors.toString}")
}) recover {
    case e: SpreedlyException => log.error(s"Error contacting Spreedly service: ${e.getMessage}")
}
```


## Test
```
sbt test
```

## Description

### Client interface
This package exposes `SpreedlyClient` which is the core library for interacting with Spreedly.
It handles the RESTful communication and marshaling behind the scenes, and provides relevant errors which map to those in the Spreedly docs.

### XML serialization
The Play framework [recommends JAXB](https://www.playframework.com/documentation/2.4.x/ScalaWS) as an efficient way to serialize XML over the wire.
The mappings were largely taken from [spreedly-java](https://github.com/rjstanford/spreedly-java) (MIT license).


## Terms
#### Authorize & Capture
An *authorize* call will contact the gateway to verify whether a transaction can take place and funds can be transferred.
If able to proceed, a *capture* is then required to perform the fund transfer.

#### Purchase
Immediately transfer funds through payment gateway if transaction is approved.

#### Redact
In most cases, Spreedly will not permit you to delete resources (gateway accounts, payments, etc) since their history is permanent.
Instead, they allow you to *redact* these resources, making them inactive and scrubbing them of sensitive information.
Spreedly will not charge storage fees for redacted resources.

#### Retain
By default, payment methods are created and their tokens are short-lived, expiring after a few minutes.
Gateway accounts, however, are automatically *retained* by default when called via normal, authenticated channel.
Spreedly allows you to *retain* customer payment methods by an explicit API call or by passing a `retain_on_success` to purchase, verify, or authorize calls.

#### Recache
Certain sensitive data such as credit card CVV cannot be retained by Spreedly.
Instead, they can be *recached* for a period of time in order to maintain the original payment method and its token.

#### Credit
A transaction *credit* is the same as a refund.

#### Void
Cancel a charge that hasn't yet taken place by issuing a *void*.


## License
The MIT License (MIT)

Copyright (c) 2015-2016 Ticketfly, Inc

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

