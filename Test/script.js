import ws from "k6/ws";
import {check} from 'k6'
import {SharedArray} from 'k6/data';

const scenarioName = "websocket_load_test";
const idArrayName = 'listOf';

export const options = getOptionsObject();

const listOfVehicles = getListOfVehicles();

export default function () {
    let messageData = getMessageDataObject();

    const wsResponseBody = getWebSocketResponseObject(messageData);
    check(wsResponseBody, getCheckConditionsObject(messageData));
}

function getOptionsObject() {
    return {
        scenarios: {
            [scenarioName]: {
                executor: 'constant-arrival-rate',
                rate: __ENV.RATE, // how many users will be connected per ${__ENV.TIME_UNIT}s
                timeUnit: `${__ENV.TIME_UNIT}s`,
                duration: `${__ENV.DURATION}s`, // the duration of the test
                gracefulStop : `${__ENV.GRACEFUL_STOP}s`, // the waiting time to finish the test
                preAllocatedVUs: __ENV.VU, // the number of preAllocated VU, VU - virtual users it is essentially separate thread with scenario
                maxVUs: __ENV.VU // the max number of VU
                /*
                Example

                RATE=50
                TIME_UNIT=1
                DURATION=20
                GRACEFUL_STOP=60
                VU=1000
                VEHICLES_PER_VU=1
                MAX_MESSAGE_PER_VEHICLE=10
                TOTAL_VEHICLES=100
                MAX_LATENCY=5000

                New 50 users(RATE=50) will be connected to WebSocketAPI every 1s(TIME_UNIT=1) during 20 seconds(DURATION=20)
                The test ends 20 seconds + 300 seconds (GRACEFUL_STOP=60).
                1000 Max users for test (VU=1000)
                1 Tracked vehicle per VU (VEHICLES_PER_VU=1)
                10 Messages(MAX_MESSAGE_PER_VEHICLE=10) should be received by each VU for 60 sec (GRACEFUL_STOP=60)
                The range of generated ids for vehicles start from 0 to 100(TOTAL_VEHICLES=100)
                The maximum latency is 5000 milliseconds for data.timeStamp - line 103. That considered valid.
                The data.timeStamp is set by back-end when message is sending. User should receive that message no later than 5000 milliseconds
                */
            },
        },
    };
}

function getListOfVehicles() {
    return new SharedArray(idArrayName, function () {
        return Array.from({length: __ENV.TOTAL_VEHICLES}, (_, i) => i + 1);
    });
}

function getWebSocketResponseObject(messageData) {
    let rs = ws.connect(messageData.wssUrl, null, function (socket) {
        socket.on('open', handleSocketOpen(socket, messageData.params));
        socket.on('message', handleMessageEvent(socket, messageData));
    });

    return rs;
}

function getMessageDataObject() {
    let vehicles = generateRandomVehicleIdList(listOfVehicles, __ENV.VEHICLES_PER_VU);

    let params = {message: "subscribe", vehicles: vehicles.join(",")};
    let wssUrl = `${__ENV.WSS_URL}`;
    let maxMessages = __ENV.MAX_MESSAGE_PER_VEHICLE;
    let max_latency = __ENV.MAX_LATENCY;

    return  {
        params,
        wssUrl,
        maxMessages,
        max_latency,
        messageCount: 0,
        validDelayCount: 0,
        incrementMessageCount : function() {
            this.messageCount++;
        },
    };
}

function handleSocketOpen(socket, params) {
    return () => {
        try {
            socket.send(JSON.stringify(params))
        } catch (e) {
            console.error("WebSocket opening or HTTP transmission error")
        }
    };
}

function handleMessageEvent(socket, messageData) {
    return (msg) => {
        let data = JSON.parse(msg);

        let msgTimeStamp = new Date(data.timeStamp).getTime();
        let localTimeStamp = new Date().getTime();

        let diff = localTimeStamp - msgTimeStamp;

        if (diff <= messageData.max_latency){
            messageData.validDelayCount++;
        }

        messageData.incrementMessageCount();

        if (messageData.messageCount >= __ENV.VEHICLES_PER_VU){
            socket.close();
        }
    };
}

function getCheckConditionsObject(messageData) {
    return {
        ['ok']: () => {
            console.log(messageData.messageCount)

            let messageCountValid = messageData.messageCount == messageData.maxMessages * __ENV.VEHICLES_PER_VU;
            let validDelays = messageData.validDelayCount == messageData.messageCount;

            return messageCountValid && validDelays;
        },
    };
}

function generateRandomVehicleIdList(array, n) {
    const shuffled = array.slice().sort(() => 0.5 - Math.random());
    return shuffled.slice(0, n);
}