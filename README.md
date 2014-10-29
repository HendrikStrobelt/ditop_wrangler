DiTop -- Standalone Mallett Topic extraction 
==============
##### for more information on the DiTop project visit: [ditop.hs8.de](http://ditop.hs8.de)


This product can be used to create a topic model file from three text collections.

To run it, you need [maven](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html):
```
  git clone https://github.com/HendrikStrobelt/ditop_wrangler.git 
  cd ditop_wrangler
  mvn package
  java -jar target/DiTopWrangler-0.9.jar
```

Related projects are:
- [ditop_server](https://github.com/HendrikStrobelt/ditop_server)
- [ditop_client](https://github.com/HendrikStrobelt/ditop_client)


The mallett library is required: McCallum, Andrew Kachites.  "MALLET: A Machine Learning for Language Toolkit.", http://mallet.cs.umass.edu. 2002.

The software is licensed under the new BSD.
