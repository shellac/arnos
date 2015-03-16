

# Sequential requests #
For these tests, the following command was used:
```
ab -c 1 -n 1000 http://localhost:8084/resrev/grants
```

## Results In Brief (SR) ##
Mean requests per second (#/sec):

  * Setup A (with cache): 2.57
  * Setup B (without cache): 0.37
  * Setup C (direct query): 0.59

## Results In Full (SR) ##
### Results for Setup A ###
```
Document Path:          /resrev/grants
Document Length:        9294 bytes

Concurrency Level:      1
Time taken for tests:   388.389 seconds
Complete requests:      1000
Failed requests:        0
Write errors:           0
Total transferred:      9530000 bytes
HTML transferred:       9294000 bytes
Requests per second:    2.57 [#/sec] (mean)
Time per request:       388.389 [ms] (mean)
Time per request:       388.389 [ms] (mean, across all concurrent requests)
Transfer rate:          23.96 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.0      0       0
Processing:   347  388  30.4    384     896
Waiting:      338  375  29.1    372     884
Total:        347  388  30.4    384     896

Percentage of the requests served within a certain time (ms)
  50%    384
  66%    390
  75%    394
  80%    398
  90%    408
  95%    416
  98%    436
  99%    451
 100%    896 (longest request)
```

### Results for Setup B ###
```
Concurrency Level:      1
Time taken for tests:   2721.354 seconds
Complete requests:      1000
Failed requests:        0
Write errors:           0
Total transferred:      9530000 bytes
HTML transferred:       9294000 bytes
Requests per second:    0.37 [#/sec] (mean)
Time per request:       2721.354 [ms] (mean)
Time per request:       2721.354 [ms] (mean, across all concurrent requests)
Transfer rate:          3.42 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.0      0       0
Processing:  2478 2721 300.3   2615    4462
Waiting:     2473 2714 300.3   2608    4455
Total:       2478 2721 300.3   2615    4462

Percentage of the requests served within a certain time (ms)
  50%   2615
  66%   2631
  75%   2647
  80%   2671
  90%   3018
  95%   3635
  98%   3662
  99%   3714
 100%   4462 (longest request)
```

### Results for Setup C ###
```
Concurrency Level:      1
Time taken for tests:   1706.073 seconds
Complete requests:      1000
Failed requests:        0
Write errors:           0
Total transferred:      9530000 bytes
HTML transferred:       9294000 bytes
Requests per second:    0.59 [#/sec] (mean)
Time per request:       1706.073 [ms] (mean)
Time per request:       1706.073 [ms] (mean, across all concurrent requests)
Transfer rate:          5.46 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.0      0       0
Processing:  1406 1706 174.9   1683    5600
Waiting:     1386 1685 174.7   1661    5579
Total:       1406 1706 174.9   1683    5600

Percentage of the requests served within a certain time (ms)
  50%   1683
  66%   1693
  75%   1699
  80%   1706
  90%   1733
  95%   1800
  98%   2001
  99%   2259
 100%   5600 (longest request)
```

# Concurrent requests #
For these tests, the following command was used:
```
ab -c 10 -n 1000 http://localhost:8084/resrev/grants
```

## Results In Brief (CR) ##
Mean requests per second (#/sec):
  * Setup A (with cache):  3.86
  * Setup B (without cache): 0.67
  * Setup C (direct query): 1.08

## Results In Full (CR) ##
### Results for Setup A ###
```
Concurrency Level:      10
Time taken for tests:   259.214 seconds
Complete requests:      1000
Failed requests:        997
   (Connect: 0, Receive: 0, Length: 997, Exceptions: 0)
Write errors:           0
Non-2xx responses:      7
Total transferred:      9563101 bytes
HTML transferred:       9327633 bytes
Requests per second:    3.86 [#/sec] (mean)
Time per request:       2592.139 [ms] (mean)
Time per request:       259.214 [ms] (mean, across all concurrent requests)
Transfer rate:          36.03 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.4      0       7
Processing:    73 2589 952.7   2462    8142
Waiting:       73 2540 957.7   2417    8128
Total:         73 2589 952.7   2462    8142

Percentage of the requests served within a certain time (ms)
  50%   2462
  66%   2833
  75%   3123
  80%   3284
  90%   3767
  95%   4404
  98%   5069
  99%   5481
 100%   8142 (longest request)
```

### Results for Setup B ###
```
Concurrency Level:      10
Time taken for tests:   1482.705 seconds
Complete requests:      1000
Failed requests:        0
Write errors:           0
Total transferred:      9530000 bytes
HTML transferred:       9294000 bytes
Requests per second:    0.67 [#/sec] (mean)
Time per request:       14827.047 [ms] (mean)
Time per request:       1482.705 [ms] (mean, across all concurrent requests)
Transfer rate:          6.28 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.1      0       1
Processing:  5511 14799 2907.3  14197   30516
Waiting:     5483 14776 2907.3  14176   30495
Total:       5511 14799 2907.3  14197   30516

Percentage of the requests served within a certain time (ms)
  50%  14197
  66%  15250
  75%  16204
  80%  16781
  90%  18834
  95%  20601
  98%  22449
  99%  24201
 100%  30516 (longest request)
```

### Results for Setup C ###
```
Concurrency Level:      10
Time taken for tests:   926.215 seconds
Complete requests:      1000
Failed requests:        0
Write errors:           0
Total transferred:      9530000 bytes
HTML transferred:       9294000 bytes
Requests per second:    1.08 [#/sec] (mean)
Time per request:       9262.154 [ms] (mean)
Time per request:       926.215 [ms] (mean, across all concurrent requests)
Transfer rate:          10.05 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.2      0       4
Processing:  4247 9243 1078.4   9227   12500
Waiting:     4218 9210 1077.7   9192   12497
Total:       4247 9243 1078.4   9228   12501

Percentage of the requests served within a certain time (ms)
  50%   9228
  66%   9569
  75%   9793
  80%   9982
  90%  10488
  95%  11010
  98%  11465
  99%  11919
 100%  12501 (longest request)
```

# Configuration #
Arnos & a single SPARQL endpoint are running on the same local networked server.

The client application used in this test is the [Research Revealed](http://researchrevealed.ilrt.bris.ac.uk/) browser demo running on the local machine with logging disabled.

The page being accessed is the default Grant view page in ResearchRevealed. This particular page is issuing:
  * CONSTRUCT x 2
  * DESCRIBE x 1
  * SELECT X 1
  * SELECT (count) x 1


## Configuration for Setup A ##
Arnos configured with a single endpoint.
Cache enabled. Cache settings:
```
    <cache name="resultsCache"
        maxElementsInMemory="500"
        eternal="false"
        overflowToDisk="true"
        diskPersistent="false"
        memoryStoreEvictionPolicy="LFU" />
```
Sequential requests
Logging set to WARN messages only.
Client & Server warmed up with 200 prior requests

## Configuration for Setup B ##
Arnos configured with a single endpoint.
Cache disabled.
Logging set to WARN messages only.
Client & Server warmed up with 200 prior requests

## Configuration for Setup C ##
Arnos bypassed, endpoint is called directly by the client application.
Client & Server warmed up with 200 prior requests