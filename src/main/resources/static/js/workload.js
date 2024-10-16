var backgroundColors = [
    'rgba(255, 99, 132, 0.2)',
    'rgba(255, 159, 64, 0.2)',
    'rgba(255, 205, 86, 0.2)',
    'rgba(75, 192, 192, 0.2)',
    'rgba(54, 162, 235, 0.2)',
    'rgba(153, 102, 255, 0.2)',
    'rgba(201, 203, 207, 0.2)'
];

var borderColors = [
    'rgb(255, 99, 132)',
    'rgb(255, 159, 64)',
    'rgb(255, 205, 86)',
    'rgb(75, 192, 192)',
    'rgb(54, 162, 235)',
    'rgb(153, 102, 255)',
    'rgb(201, 203, 207)'
];

const chartP99 = new Chart(document.getElementById("chart-container-p99"), {
    type: 'line',
    data: {
        labels: [],
        datasets: [],
    },
    options: {
        scales: {
            x: {
                type: 'time',
                time: {
                    unit: 'minute'
                },
                parse: false
            },
            y: {
                title: {
                    display: true,
                    text: "P99.9 Latency (ms)",
                },
            },
        },
        title: {
            display: true,
            text: 'P99.9 Latencies'
        },
        responsive: true,
    },
});

const chartTPS = new Chart(document.getElementById("chart-container-tps"), {
    type: 'line',
    data: {
        labels: [],
        datasets: [],
    },
    options: {
        scales: {
            x: {
                type: 'time',
                time: {
                    unit: 'minute'
                },
                parse: false
            },
            y: {
                title: {
                    display: true,
                    text: "Transactions per second (TpS)",
                },
            },
        },
        title: {
            display: true,
            text: 'TpS'
        },
        responsive: true,
    },
});

const AppDashboard = function (settings) {
    this.settings = settings;
    this.init();
};

AppDashboard.prototype = {
    init: function () {
        var socket = new SockJS(this.settings.endpoints.socket),
                stompClient = Stomp.over(socket),
                _this = this;
        // stompClient.log = null;
        stompClient.log = (log) => {};
        stompClient.connect({}, function (frame) {
            stompClient.subscribe(_this.settings.topics.item, function (payload) {
                var _event = JSON.parse(payload.body);
                _this.handleWorkloadItemUpdate(_event);
            });

            stompClient.subscribe(_this.settings.topics.summary, function (payload) {
                var _event = JSON.parse(payload.body);
                _this.handleMetricsSummaryUpdate(_event);
            });

            stompClient.subscribe(_this.settings.topics.chart, function () {
                _this.handleChartUpdate();
            });
        });
    },

    getElement: function (id) {
        return $('#' + id);
    },

    round: function (v) {
        return v.toFixed(1);
    },

    handleWorkloadItemUpdate: function (workload) {
        var _this = this;

        const rowElt = _this.getElement("row-" +  workload.id);
        rowElt.find(".remaining-time").text(workload.remainingTime);
        rowElt.find(".p90").text(_this.round(workload.metrics.p90));
        rowElt.find(".p99").text(_this.round(workload.metrics.p99));
        rowElt.find(".p999").text(_this.round(workload.metrics.p999));
        rowElt.find(".opsPerSec").text(_this.round(workload.metrics.opsPerSec));
        rowElt.find(".opsPerMin").text(_this.round(workload.metrics.opsPerMin));
        rowElt.find(".success").text(workload.metrics.success);
        rowElt.find(".transientFail").text(workload.metrics.transientFail);
        rowElt.find(".nonTransientFail").text(workload.metrics.nonTransientFail);
    },

    handleMetricsSummaryUpdate: function (metrics) {
        var _this = this;

        const metricElt = _this.getElement("aggregated-metrics");
        metricElt.find(".p90").text(_this.round(metrics.p90));
        metricElt.find(".p99").text(_this.round(metrics.p99));
        metricElt.find(".p999").text(_this.round(metrics.p999));
        metricElt.find(".opsPerSec").text(_this.round(metrics.opsPerSec));
        metricElt.find(".opsPerMin").text(_this.round(metrics.opsPerMin));
        metricElt.find(".success").text(metrics.success);
        metricElt.find(".transientFail").text(metrics.transientFail);
        metricElt.find(".nonTransientFail").text(metrics.nonTransientFail);
    },

    handleChartUpdate: function() {
        var _this = this;

        $.getJSON("/api/workload/data-points/p99", function(json) {
            const xValues = json[0]["data"];

            const yValues = json.filter((item, idx) => idx > 0)
                    .map(function(item) {
                        var id = item["id"];
                        var bgColor = backgroundColors[id % backgroundColors.length];
                        var ogColor = borderColors[id % borderColors.length];
                        return {
                            label: item["name"],
                            data: item["data"],
                            backgroundColor: bgColor,
                            borderColor: ogColor,
                            fill: false,
                            tension: 1.2,
                            cubicInterpolationMode: 'monotone',
                            borderWidth: 1,
                            hoverOffset: 4,
                        };
                    });

            chartP99.config.data.labels = xValues;
            chartP99.config.data.datasets = yValues;
            chartP99.update('none');
        });

        $.getJSON("/api/workload/data-points/tps", function(json) {
            const xValues = json[0]["data"];

            const yValues = json.filter((item, idx) => idx > 0)
                    .map(function(item) {
                        var id = item["id"];
                        var bgColor = backgroundColors[id % backgroundColors.length];
                        var ogColor = borderColors[id % borderColors.length];
                        return {
                            label: item["name"],
                            data: item["data"],
                            backgroundColor: bgColor,
                            borderColor: ogColor,
                            fill: false,
                            tension: 1.2,
                            cubicInterpolationMode: 'monotone',
                            borderWidth: 1,
                            hoverOffset: 4,
                        };
                    });

            chartTPS.config.data.labels = xValues;
            chartTPS.config.data.datasets = yValues;
            chartTPS.update('none');
        });
    },
};

document.addEventListener('DOMContentLoaded', function () {
    new AppDashboard({
        endpoints: {
            socket: '/whackanode-service',
        },

        topics: {
            item: '/topic/workload/item',
            summary: '/topic/workload/summary',
            chart: '/topic/workload/chart'
        },

        elements: {

        }
    });
});

