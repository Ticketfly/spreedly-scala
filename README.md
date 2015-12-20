# spreedly-scala
Async Spreedly payment service client written in Scala.  

Spreedly domain objects are mapped to XML fields using the JAXB implementation in [spreedly-java](https://github.com/rjstanford/spreedly-java).
The Play framework [recommends JAXB](https://www.playframework.com/documentation/2.4.x/ScalaWS) as an efficient way to serialize XML over the wire.

## Test
```
sbt test
```

## Usage
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

## Description

### Client interface
*payment-core* exposes `SpreedlyClient` which is the core library for interacting with Spreedly.
It handles the RESTful communication and marshaling behind the scenes, and provides relevant errors which map to those in the Spreedly docs.

### XML serialization

The Play framework [recommends JAXB](https://www.playframework.com/documentation/2.4.x/ScalaWS) as an efficient way to serialize XML over the wire.
The mappings were largely taken from [spreedly-java](https://github.com/rjstanford/spreedly-java) (MIT license), but had to be copied in order to fix several bugs included.
The payment service can instead just depend on this external package once [my pull request](https://github.com/rjstanford/spreedly-java/pull/1) has been merged and built.


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
