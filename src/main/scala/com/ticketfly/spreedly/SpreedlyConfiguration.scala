package com.ticketfly.spreedly

case class SpreedlyConfiguration(environmentKey: String,
                                 accessSecret: String,
                                 apiUrl: String = "https://core.spreedly.com/v1",
                                 ssl: Boolean = true,
                                 requestTimeout: Int = 64000)     // set to 64 seconds per the docs https://docs.spreedly.com/reference/api/v1/timeouts/


