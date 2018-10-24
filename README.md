# revolut-restapi
Test task for Revolut company

Sample RESTful API (including data model and the backing implementation) for money
transfers between accounts implemented in Java SE (not require a pre-installed
container/server).

For starting tests it is enough to download project and run package in maven.
Also there is possibility to run some tests directly from "src/test/java/com/revolut/task/" directory.
There are two test classes: 
  AccountRestApiTest.java
  TransferRestApiTest.java
  
The first one is test class for account REST API, it creates some accounts in different currency (RUR, USD).
Second one is test class for doing transfer between accounts.
