## myspark

This tool creats a json file that you can use for bulk inserts to your local elasitcsearch.
The data will come with timestamp that you can use for building histogram aggregation with different intervals you like.
The data is comprised of mocked hotels.


## Run Requirements
* JDK - Oracle or OpenJDK, 1.8.0 or higher.

## Installation


You can download this [jar](https://github.com/TomonoriSoejima/myspark/releases/download/v1.0.1/myspark.jar).
Then you can start it simply by double-clicking the jar or `java -jar myspark.jar` from your terminal.
That would fire up your default browser and open the web UI.

## Usage - Simplest Case

Just hit `http://localhost:4567/<action>/<interval>/<counts>`

It takes only 3 parameters

**1. action -  `create` or `bulk`**

**2. interval - interval you want.**


* s : secondly
* m : minutely
* h : hourly
* d : daily


**3. count - how many doc you want.**

#### Basic Usage Examples

`http://localhost:4567/create/m/5000`
* this api will create a bulkable json file with 5000 documents.



`http://localhost:4567/bulk/d/3600`

* this api will send documents straight to your elasticsearch.

This only works if you have disabled security in your elasticsearch.yml though.
`xpack.security.enabled: false`

* You can also consume them from curl if you like.

`curl http://localhost:4567/bulk/h/10`








