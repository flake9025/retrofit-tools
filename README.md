# retrofit-tools
Retrofit tools

General Purpose

This framework offers some useful functionalities for Retrofit 2:
- Services cache : unique client and services initialization
  - see SampleClient.java : createService()
- SSL / TLS Connection with certificate file
  - see SSLFactory
- Unsecured X509 Manager to handle self-signed certificates
  - see SelfSignedX509TrustManager
- Retry policy : retries requests when response codes are 500
  - see RetryPolicy
- Retry interceptor for asynchronous calls
  - see RetryAsyncCallback
- Retry interceptor for synchronous calls
  - see RetryInterceptor
- Jackson mapper instead of Gson to integrate with an existing application 
  - see JacksonMapper

