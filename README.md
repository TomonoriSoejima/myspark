Here's a cleaner, more polished version of your `README.md` with improved structure, clarity, grammar, and added code formatting. I’ve also included the shell script as a usage example:

---

## myspark

**myspark** is a simple tool for generating mock hotel data in JSON format, suitable for bulk insertion into your local Elasticsearch instance.  
Each document includes a timestamp, making it ideal for time-based visualizations such as histogram aggregations.

---

## Features

- Create a JSON file for bulk Elasticsearch inserts  
- Send mock data directly to Elasticsearch (if security is disabled)  
- Customizable timestamps for testing time-series queries  
- Lightweight web UI to trigger actions

---

## Requirements

- JDK 1.8.0 or higher (Oracle JDK or OpenJDK)

---

## Installation

1. Download the latest [myspark JAR](https://github.com/TomonoriSoejima/myspark/releases/download/v1.0.1/myspark.jar)
2. Run it via double-click or from the command line:

```bash
java -jar myspark.jar
```

This launches a lightweight web server and opens the UI in your default browser.

---

## Usage

### REST API

```
http://localhost:4567/<action>/<interval>/<count>
```

- **action**:  
  - `create`: generates a JSON file with mocked documents  
  - `bulk`: sends the documents directly to your local Elasticsearch

- **interval**:  
  Timestamp interval for each document  
  - `s`: seconds  
  - `m`: minutes  
  - `h`: hours  
  - `d`: days  

- **count**:  
  Number of documents to generate

---

### Examples

#### Generate 5,000 documents with 1-minute intervals:
```bash
curl http://localhost:4567/create/m/5000
```
- This creates a bulkable JSON file in your `$HOME` directory.

#### Send 3,600 daily-interval documents directly to Elasticsearch:
```bash
curl http://localhost:4567/bulk/d/3600
```

> Note: Direct bulk insertion only works if Elasticsearch security is disabled (`xpack.security.enabled: false` in `elasticsearch.yml`).

---

## Sample Startup Script

Here’s a helper script to run the tool and automatically trigger a create action:

```bash
#!/bin/bash

export JAVA_HOME="/Users/surfer/elastic/labs/7.17.4/elasticsearch/jdk.app/Contents/Home"

# Check if myspark is already running
jps | grep myspark.jar > /dev/null

if [ $? != 0 ]; then
    java -jar myspark.jar &
    sleep 2
fi

# Trigger data generation
curl http://localhost:4567/create/h/$1
```

Save this as `do_it.sh`, make it executable with `chmod +x do_it.sh`, and run:

```bash
./do_it.sh 1000
```

This example creates 1,000 documents with hourly timestamps.

---

Let me know if you'd like to add more advanced examples (like Kibana visualizations or `bulk` curl-based replays).
