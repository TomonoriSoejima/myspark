How to use it



http://localhost:4567/create/<interval>/counts
- this api will create a bulkable json file.

interval can be

s : secondly
m : minutely
h : hourly
d : daily


http://localhost:4567/bulk/<interval>/counts

- this api will push send documents to your local elasticsearch
This only works if you have disabled security in your elasticsearch.yml though.
xpack.security.enabled: false


What kind of data will it generate:
    you will get a bunch of hotel list.