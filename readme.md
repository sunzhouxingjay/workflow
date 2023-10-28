# Prerequisites and setup:

* [Docker](https://www.docker.com/products/overview) - v 20.10.7

* [Docker Compose](https://docs.docker.com/compose/overview/) - v1.27.4

* **Node.js**  v8.9.4

* **docker** **images**:

  ​              hyperledger/fabric-orderer:1.4.0

  ​              hyperledger/fabric-zookeeper:x86_64-0.4.6

  ​              hyperledger/fabric-kafka:x86_64-0.4.6

# How to start

## 1.Load peer and ca images

```shell
docker load -i workflow-peer
docker load -i workflow-ca
```

## 2.Start Container

```shell
docker-compose -f workflow.yaml up
```

## 3.Create channel

```shell
cd sdk-application
#node v8.9.4
#启动服务
./runApp.sh
#another window
./createChannelAndJoin.sh
```

## 4.Start the nacos service and gateway service

```shell
curl 127.0.0.1:${host port that container 8888 port mapped}/wfEngine/startMonitor
#example
curl 127.0.0.1:7888/wfEngine/startWorkflowService
```

you can access nacos using 127.0.0.1:7848/nacos 

### note:

This command takes a few seconds to execute. It is best not to execute the command on multiple containers at the same time, as the program may be killed due to memory issues

## 5.Register web services

Firstly, it is necessary to run a service. Here, a simple service serviceTest is provided, where the  /test service returns the JSON string sent to the service directly to the user

use Maven to package the service

```shell
cd serviceTest
mvn clean package
```

run this service

```shell
cd target
java -jar sunLocalTest.jar --server.port=${your port}
#example
java -jar sunLocalTest.jar --server.port=6666
```

register this service

```shell
curl -X POST 'http://{peer container ip}:{peer port of nacos}/nacos/v1/ns/instance?port={port of web service}&healthy=true&ip={ip of web service}&weight=1.0&serviceName={serviceGroup}@@{serviceName}&encoding=GBK'
#ServiceGroup can be left blank and used the default value, directly as serviceName={serviceName}
#example
curl -X POST 'http://127.0.0.1:7848/nacos/v1/ns/instance?port=6666&healthy=true&ip=127.0.0.1&weight=1.0&serviceName=WORKFLOW@@sunLocalTest&encoding=GBK'
```

## 6.deploy the bpmn diagram

here is a sample, simpleSample.bpmn

```shell
curl --request POST \
  --url http://127.0.0.1:8999/grafana/wfRequest/deploy \
  --header 'content-type: multipart/form-data' \
  --form file=@{path of single-workflow}/simpleSample.bpmn \
  --form deploymentName=simpleSample.bpmn
```

**output:**

```shell
{
	"code": 200,
	"Oid": "simpleSample.bpmn",
	"body": "等待上链,更改状态",
	"模拟执行结果": true
}
```

Under normal circumstances, the returned result will display successful execution, waiting for writing into blockchain. The returned result contains an attribute **Oid**, which can be used to query whether the writing operation is successful or failed.

```shell
curl --request GET \
  --url http://127.0.0.1:8999/grafana/getResByOid/simpleSample.bpmn
```

After querying, the results of the writing into blockchain and workflow execution can be obtained

## 7.instance workflow process

```shell
curl --request POST \
  --url http://127.0.0.1:8999/grafana/wfRequest/instance \
  --header 'content-type: application/json' \
  --data '{
    "deploymentName":"simpleSample.bpmn",
    "businessData":"{}",
    "processData":"{}",
    "staticAllocationTable":"{\"userTask\":\"sun\"}"
}'
#StaticAllocationTable is a static allocation table for assigning performers to userTasks. If you find it troublesome, you can skip this field, which means that anyone can execute the userTask
```

**output:**

```shell
{
	"code": 200,
	"Oid": "simpleSample.bpmn@fb1907cc-7383-4e52-996f-ea05eb9664c0",
	"body": "等待上链,更改状态",
	"模拟执行结果": true
}
```

same as deploy,it need query the result

```shell
curl --request GET \
  --url http://127.0.0.1:8999/grafana/getResByOid/{Oid}
```

## 8.complete userTask

```shell
curl --request POST \
  --url http://127.0.0.1:8999/grafana/wfRequest/complete \
  --header 'content-type: application/json' \
  --data '{
    "Oid":"simpleSample.bpmn@fb1907cc-7383-4e52-996f-ea05eb9664c0",
    "taskName":"userTask",
    "businessData":"{\"test\":\"just sample\"}",
    "processData":"{}",
    "user":"sun"
}'
#BusinessData is the data used in business, while processData is the data used in workflow sequenceflow, both in JSON string format
```

**output:**

```shell
{
	"code": 200,
	"Oid": "simpleSample.bpmn@fb1907cc-7383-4e52-996f-ea05eb9664c0",
	"body": "等待上链,更改状态",
	"模拟执行结果": true
}
```

it also need query the result

```shell
curl --request GET \
  --url http://127.0.0.1:8999/grafana/getResByOid/{Oid}
```







# Sample REST APIs Requests:

## deploy bpmn of workflow

Write the bpmn diagram into the blockchain and state database

```shell
curl --request POST \
  --url http://127.0.0.1:8999/grafana/wfRequest/deploy \
  --header 'content-type: multipart/form-data' \
  --form file=@{path of bpmn file} \
  --form deploymentName={The name of the bpmn file in the database}
```

please note that the **deploymentName** must end with '.bpmn' cause activiti parsing required.  If there is no '. bpmn', our code will automatically add '. bpmn'

**OUTPUT:**

```shell
{
	"code": 200,
	"Oid": "<only id used for query the result write blockchain success or failed>",
	"body": "等待上链,更改状态<Wait write to the blockchain and change the state>",
	"模拟执行结果": true
}
```

the **Oid** is used for query the result whether the write data to blockchain successful or failed

## Create workflow instance

Create the corresponding workflow instance according to the provided **deploymentName**

```shell
curl --request POST \
  --url http://127.0.0.1:8999/grafana/wfRequest/instance \
  --header 'content-type: application/json' \
  --data '{
    "deploymentName":"<name of deployed bpmn >",
    "businessData":"<data for Workflow Business mainly includes service task use,type:json String>",
    "processData":"<Data for workflow sequenceFlow,determining which flow to execute,type:json String>",
    "staticAllocationTable":"<Static allocation table for assigning performers to userTasks>"
}'
```

**OUTPUT:**

```shell
{
	"code": 200,
	"Oid": "<only id for workflow instance>",
	"body": "等待上链,更改状态<Wait write to the blockchain and change the state>",
	"模拟执行结果": true
}
```

the **Oid** is used for query the result whether the write data to blockchain successful or failed

## complete userTask

```shell
curl --request POST \
  --url http://127.0.0.1:8999/grafana/wfRequest/complete \
  --header 'content-type: application/json' \
  --data '{
    "Oid":"<only id for workflow instance>",
    "taskName":"<the name of userTask that need complete>",
    "businessData":"<data for Workflow Business mainly includes service task use,type:json String>",
    "processData":"<Data for workflow sequenceFlow,determining which flow to execute,type:json String>",
    "user":"<the performers of the userTask>"
}'
```

If a **userTask** is not assigned a performer, then anyone can execute it, and this attribute **user** is meaningless

**OUTPUT:**

```shell
{
	"code": 200,
	"Oid": "<only id for workflow instance>",
	"body": "等待上链,更改状态<Wait write to the blockchain and change the state>",
	"模拟执行结果": true
}
```

the **Oid** is used for query the result whether the write data to blockchain successful or failed

## query the result

```shell
curl --request GET \
  --url http://127.0.0.1:8999/grafana/getResByOid/{Oid}
```

**output:**

```shell
{
	"startPutToBlockChain": 1695888525359,<the time that start to write data to blockchain>
	"isDeploy": false,<is Deploy or not>
	"flushStartTime": 1695888525537,<the time that the data has been write into blockchain and start to change the state database>
	"isEnd": true,<is the workflow instance end or not>
	"toTaskName": "[]",<the next userTask's name>
	"businessData": "{\"last\":\"%%just sample%%\"}",<the serviceTask response you want>
	"fromTaskName": "[userTask]",<the completed userTask's name>
	"deploymentName": "simpleSample.bpmn",<the deploymentName of the workflowInstance's bpmn>
	"startTime": 1695888520969,<the time that start handle the request>
	"flushEndTime": 1695888525551,<the time that state database has been changed>
	"simulationEndTime": 1695888525274,<the time that simulation has been completed>
}
```

## dynamic Bind

Dynamic binding means that the registered web service can be bound dynamically for the serviceTask in the workflow instance, or the executor can be bound for the userTask

```shell
#executor bound for the userTask
curl --request POST \
  --url http://127.0.0.1:8999/grafana/dynamicBind \
  --header 'content-type: application/json' \
  --data '{
    "oid":"<only id for the workflow instance>",
    "taskName":"name of userTask",
    "value":"name of executor"	
}'
#web Service bound for the serviceTask
curl --request POST \
  --url http://127.0.0.1:8999/grafana/dynamicBind \
  --header 'content-type: application/json' \
  --data '{
    "oid":"<only id for the workflow instance>",
    "taskName":"name of serviceTask",
    "value":"json String of web service metaData"
}'
```



# Property description of serviceTask

## serviceName

the name of the web service registration which the serviceTask will invoke

## httpMethod

the method that the serviceTask will invoke , GET,POST,DELETE,etc.

## route

the path part of the URL

## serviceGroup

the group of the service registration which the serviceTask will invoke

## input

used to indicate input for the serviceTasks, default is the previous serviceTask response

```shell
{"input field name":"<one of the pre serviceTasks name>.<the field of this pre serviceTask response>"}
```

## output

used to generate the output of user's request,default is the last serviceTask response

```shell
format same as input
```





# Problems you may meet

## 1.Due to the dns addressing pitfalls of the go language in its early years, if Orderer fails to start during the startup process and the error message is roughly as follows

```shell
023-09-16 06:46:27.475 UTC [orderer.consensus.kafka] setupTopicForChannel -> INFO 009 [channel: testchainid] Setting up the topic for this channel...
2023-09-16 06:46:27.477 UTC [orderer.consensus.kafka] setupProducerForChannel -> INFO 00a [channel: testchainid] Setting up the producer for this channel...
fatal error: unexpected signal during runtime execution
[signal SIGSEGV: segmentation violation code=0x1 addr=0x63 pc=0x7f46400a5259]

runtime stack:
runtime.throw(0xf73ab5, 0x2a)
	/opt/go/src/runtime/panic.go:608 +0x72
runtime.sigpanic()
	/opt/go/src/runtime/signal_unix.go:374 +0x2f2

goroutine 91 [syscall]:
runtime.cgocall(0xc1a530, 0xc0006c2600, 0x29)
	/opt/go/src/runtime/cgocall.go:128 +0x5e fp=0xc0006c25c8 sp=0xc0006c2590 pc=0x4039de
net._C2func_getaddrinfo(0xc0004fa2c0, 0x0, 0xc000603230, 0xc00064e078, 0x0, 0x0, 0x0)
	_cgo_gotypes.go:92 +0x55 fp=0xc0006c2600 sp=0xc0006c25c8 pc=0x537165
net.cgoLookupIPCNAME.func1(0xc0004fa2c0, 0x0, 0xc000603230, 0xc00064e078, 0x13, 0x13, 0xc0006509c0)
	/opt/go/src/net/cgo_unix.go:149 +0x131 fp=0xc0006c2648 sp=0xc0006c2600 pc=0x53c881
net.cgoLookupIPCNAME(0xc0004faf60, 0x12, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0)
	/opt/go/src/net/cgo_unix.go:149 +0x153 fp=0xc0006c2738 sp=0xc0006c2648 pc=0x538723
net.cgoIPLookup(0xc000650ba0, 0xc0004faf60, 0x12)
	/opt/go/src/net/cgo_unix.go:201 +0x4d fp=0xc0006c27c8 sp=0xc0006c2738 pc=0x538ddd
runtime.goexit()
	/opt/go/src/runtime/asm_amd64.s:1333 +0x1 fp=0xc0006c27d0 sp=0xc0006c27c8 pc=0x45d851
created by net.cgoLookupIP
	/opt/go/src/net/cgo_unix.go:211 +0xad

goroutine 1 [IO wait]:
internal/poll.runtime_pollWait(0x7f4643cc1e30, 0x72, 0x0)
	/opt/go/src/runtime/netpoll.go:173 +0x66
internal/poll.(*pollDesc).wait(0xc00014c198, 0x72, 0xc00013c100, 0x0, 0x0)
	/opt/go/src/internal/poll/fd_poll_runtime.go:85 +0x9a
internal/poll.(*pollDesc).waitRead(0xc00014c198, 0xffffffffffffff00, 0x0, 0x0)
	/opt/go/src/internal/poll/fd_poll_runtime.go:90 +0x3d
internal/poll.(*FD).Accept(0xc00014c180, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0)
	/opt/go/src/internal/poll/fd_unix.go:384 +0x1a0
net.(*netFD).accept(0xc00014c180, 0xc600000000000000, 0xc00064a4e0, 0xc6e8a9d473cbbc30)
	/opt/go/src/net/fd_unix.go:238 +0x42
net.(*TCPListener).accept(0xc000140328, 0x42bdd2, 0xc000042130, 0xc0006b3b10)
	/opt/go/src/net/tcpsock_posix.go:139 +0x2e
net.(*TCPListener).Accept(0xc000140328, 0xf951c0, 0xc000482c00, 0xc000504b60, 0x0)
	/opt/go/src/net/tcpsock.go:260 +0x47
github.com/hyperledger/fabric/vendor/google.golang.org/grpc.(*Server).Serve(0xc000482c00, 0x106b060, 0xc000140328, 0x0, 0x0)
	/opt/gopath/src/github.com/hyperledger/fabric/vendor/google.golang.org/grpc/server.go:557 +0x210
github.com/hyperledger/fabric/core/comm.(*GRPCServer).Start(0xc000162620, 0xc0006b3dc0, 0x1)
	/opt/gopath/src/github.com/hyperledger/fabric/core/comm/server.go:202 +0x41
github.com/hyperledger/fabric/orderer/common/server.Start(0xf4dfc0, 0x5, 0xc0005f4000)
	/opt/gopath/src/github.com/hyperledger/fabric/orderer/common/server/main.go:153 +0x7e9
github.com/hyperledger/fabric/orderer/common/server.Main()
	/opt/gopath/src/github.com/hyperledger/fabric/orderer/common/server/main.go:87 +0x1ce
main.main()
	/opt/gopath/src/github.com/hyperledger/fabric/orderer/main.go:15 +0x20

goroutine 18 [syscall]:
os/signal.signal_recv(0x0)
	/opt/go/src/runtime/sigqueue.go:139 +0x9c
os/signal.loop()
	/opt/go/src/os/signal/signal_unix.go:23 +0x22
created by os/signal.init.0
	/opt/go/src/os/signal/signal_unix.go:29 +0x41
```

**solution**

Please comment out the options option in **/etc/resolv.conf** on the host before starting. Please note that this file will automatically change back when the network management program restarts

## 2.Display read time out when registering the service

The **cluster.conf** configuration file ip in the nacos cluster mode cannot use aliases. For example, both K8S and docker compose can access containers through aliases. In fact, it is normally possible. This may be related to the implementation of the nacos heartbeat mechanism. After using aliases, the nacos heartbeat mechanism will think that this is an unhealthy node, which will cause it to be inaccessible. Therefore, the fixed ip method is used in the stand-alone version, If deployed on a server, cluster.conf can write to the corresponding server IP port

## 3.Jwt or token related error occurs when creating a channel

Delete the fabric-client-kv-org* file
