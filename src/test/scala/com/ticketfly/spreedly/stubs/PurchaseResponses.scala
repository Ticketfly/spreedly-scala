package com.ticketfly.spreedly.stubs

object PurchaseResponses {
  val purchaseSuccess =
    <transaction>
      <amount type="integer">144</amount>
      <on_test_gateway type="boolean">true</on_test_gateway>
      <created_at type="datetime">2013-07-31T19:46:26Z</created_at>
      <updated_at type="datetime">2013-07-31T19:46:32Z</updated_at>
      <currency_code>USD</currency_code>
      <succeeded type="boolean">true</succeeded>
      <state>succeeded</state>
      <token>Btcyks35m4JLSNOs9ymJoNQLjeX</token>
      <transaction_type>Purchase</transaction_type>
      <order_id>187A</order_id>
      <ip nil="true"/>
      <description>4 Shardblades</description>
      <merchant_name_descriptor nil="true"/>
      <merchant_location_descriptor nil="true"/>
      <gateway_specific_fields nil="true"/>
      <gateway_specific_response_fields nil="true"/>
      <message key="messages.transaction_succeeded">Succeeded!</message>
      <gateway_token>YOaCn5a9xRaBTGgmGAWbkgWUuqv</gateway_token>
      <gateway_transaction_id>44</gateway_transaction_id>
      <shipping_address>
        <name nil="true"/>
        <address1 nil="true"/>
        <address2 nil="true"/>
        <city nil="true"/>
        <state nil="true"/>
        <zip nil="true"/>
        <country nil="true"/>
        <phone_number nil="true"/>
      </shipping_address>
      <response>
        <success type="boolean">true</success>
        <message>Successful purchase</message>
        <avs_code>22</avs_code>
        <avs_message nil="true">I will be back</avs_message>
        <cvv_code>31</cvv_code>
        <cvv_message nil="true">Rutabaga</cvv_message>
        <pending type="boolean">false</pending>
        <fraud_review type="boolean">false</fraud_review>
        <error_code>899</error_code>
        <error_detail nil="true">The eagle lives!</error_detail>
        <cancelled type="boolean">false</cancelled>
        <created_at type="datetime">2013-07-31T19:46:26Z</created_at>
        <updated_at type="datetime">2013-07-31T19:46:27Z</updated_at>
      </response>
      <payment_method>
        <token>8xXXIPGXTaPXysDA5OUpgnjTEjK</token>
        <created_at type="datetime">2013-07-31T19:46:25Z</created_at>
        <updated_at type="datetime">2013-07-31T19:46:26Z</updated_at>
        <email>perrin@wot.com</email>
        <data nil="true"/>
        <storage_state>retained</storage_state>
        <last_four_digits>4444</last_four_digits>
        <first_six_digits>411111</first_six_digits>
        <card_type>master</card_type>
        <first_name>Perrin</first_name>
        <last_name>Aybara</last_name>
        <month type="integer">1</month>
        <year type="integer">2019</year>
        <address1 nil="true"/>
        <address2 nil="true"/>
        <city nil="true"/>
        <state nil="true"/>
        <zip nil="true"/>
        <country nil="true"/>
        <phone_number nil="true"/>
        <company>Acme</company>
        <full_name>Perrin Aybara</full_name>
        <payment_method_type>credit_card</payment_method_type>
        <eligible_for_card_updater type="boolean">true</eligible_for_card_updater>
        <errors>
        </errors>
        <verification_value></verification_value>
        <number>XXXX-XXXX-XXXX-4444</number>
      </payment_method>
      <api_urls>
      </api_urls>
    </transaction>

  val purchaseFailure =
    <transaction>
      <amount type="integer">5148</amount>
      <on_test_gateway type="boolean">true</on_test_gateway>
      <created_at type="datetime">2013-07-31T19:51:57Z</created_at>
      <updated_at type="datetime">2013-07-31T19:51:57Z</updated_at>
      <currency_code>USD</currency_code>
      <succeeded type="boolean">false</succeeded>
      <state>gateway_processing_failed</state>
      <token>RxkxK78ZlvDiXRQRnyuJM5ee0Ww</token>
      <transaction_type>Purchase</transaction_type>
      <order_id nil="true"/>
      <ip nil="true"/>
      <description nil="true"/>
      <merchant_name_descriptor nil="true"/>
      <merchant_location_descriptor nil="true"/>
      <gateway_specific_fields nil="true"/>
      <gateway_specific_response_fields nil="true"/>
      <message>Unable to process the purchase transaction.</message>
      <gateway_token>Y6jMbUCm2oz6QTpavzp0xLaV9mk</gateway_token>
      <gateway_transaction_id nil="true"/>
      <response>
        <success type="boolean">false</success>
        <message>Unable to process the purchase transaction.</message>
        <avs_code nil="true"/>
        <avs_message nil="true"/>
        <cvv_code nil="true"/>
        <cvv_message nil="true"/>
        <pending type="boolean">false</pending>
        <fraud_review type="boolean">false</fraud_review>
        <error_code></error_code>
        <error_detail nil="true">The eagle is dead Jim.</error_detail>
        <cancelled type="boolean">false</cancelled>
        <created_at type="datetime">2013-07-31T19:51:57Z</created_at>
        <updated_at type="datetime">2013-07-31T19:51:57Z</updated_at>
      </response>
      <payment_method>
        <token>H0kioCnUZ8YbQ9rhqJv6zyav01Q</token>
        <created_at type="datetime">2013-07-31T19:51:57Z</created_at>
        <updated_at type="datetime">2013-07-31T19:51:57Z</updated_at>
        <email>perrin@wot.com</email>
        <data nil="true"/>
        <storage_state>retained</storage_state>
        <last_four_digits>1881</last_four_digits>
        <first_six_digits>411111</first_six_digits>
        <card_type>visa</card_type>
        <first_name>Perrin</first_name>
        <last_name>Aybara</last_name>
        <month type="integer">1</month>
        <year type="integer">2019</year>
        <address1 nil="true"/>
        <address2 nil="true"/>
        <city nil="true"/>
        <state nil="true"/>
        <zip nil="true"/>
        <country nil="true"/>
        <phone_number nil="true"/>
        <company>Acme</company>
        <full_name>Perrin Aybara</full_name>
        <payment_method_type>credit_card</payment_method_type>
        <eligible_for_card_updater type="boolean">true</eligible_for_card_updater>
        <errors>
        </errors>
        <verification_value></verification_value>
        <number>XXXX-XXXX-XXXX-1881</number>
      </payment_method>
      <api_urls>
      </api_urls>
    </transaction>
}
